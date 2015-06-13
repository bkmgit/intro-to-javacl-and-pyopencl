package com.poolik.exercise02;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;
import org.bridj.Pointer;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Random;

import static org.bridj.Pointer.allocateFloats;

public class VectorAdd {

  public static final int LENGTH = 1024;
  public static final float TOLERANCE = 0.001f;
  private static final Random random = new Random();

  public static void main(String[] args) throws IOException {
    CLContext context = JavaCL.createBestContext(CLPlatform.DeviceFeature.CPU);
    printDeviceInfo(context.getDevices()[0]);
    CLQueue queue = context.createDefaultQueue();

    String src = IOUtils.readText(VectorAdd.class.getResource("/vadd.cl"));
    CLProgram program = context.createProgram(src);
    ByteOrder byteOrder = context.getByteOrder();

    // Create a and b vectors and fill with random float values
    Pointer<Float> aPtr = allocateFloats(LENGTH).order(byteOrder);
    Pointer<Float> bPtr = allocateFloats(LENGTH).order(byteOrder);
    for (int i = 0; i < LENGTH; i++) {
      aPtr.set(i, i * random.nextFloat());
      bPtr.set(i, i * random.nextFloat());
    }

    // Create OpenCL input buffers (using the native memory pointers aPtr and bPtr) :
    CLBuffer<Float> a = context.createFloatBuffer(CLMem.Usage.Input, aPtr);
    CLBuffer<Float> b = context.createFloatBuffer(CLMem.Usage.Input, bPtr);
    CLBuffer<Float> out = context.createFloatBuffer(CLMem.Usage.Output, LENGTH);

    long start = System.currentTimeMillis();
    CLKernel addVectors = program.createKernel("vadd");
    addVectors.setArgs(a, b, out, LENGTH);
    int[] globalSizes = new int[]{LENGTH};
    CLEvent addEvt = addVectors.enqueueNDRange(queue, globalSizes);

    Pointer<Float> c = out.read(queue, addEvt); // blocks until vadd finishes
    System.out.printf("The kernel ran in %f s%n", (System.currentTimeMillis() - start) / 1000.0f);
    checkResults(aPtr, bPtr, c);
  }

  private static void checkResults(Pointer<Float> aPtr, Pointer<Float> bPtr, Pointer<Float> c) {
    int correct = 0;
    for (int i = 0; i < LENGTH; i++) {
      float tmp = aPtr.get(i) + bPtr.get(i); // assign element i of a+b to tmp
      tmp -= c.get(i); // compute the deviation of expected and output result
      if (tmp * tmp < TOLERANCE) correct++; // correct if square deviation is less than tolerance squared
      else System.out.printf("tmp=%f, a=%f, b=%f, c=%f", tmp, aPtr.get(i), bPtr.get(i), c.get(i));
    }
    System.out.printf("C = A+B: %d out of %d results were correct.%n", correct, LENGTH);
  }

  private static void printDeviceInfo(CLDevice clDevice) {
    System.out.printf("Device is %s ", clDevice.getName());
    if (clDevice.getType().contains(CLDevice.Type.GPU))
      System.out.print("GPU from");
    else if (clDevice.getType().contains(CLDevice.Type.CPU))
      System.out.print("CPU from");
    else
      System.out.print("non CPU of GPU processor from");
    System.out.printf(" %s with a max of %d compute units%n", clDevice.getVendor(), clDevice.getMaxComputeUnits());
  }
}
