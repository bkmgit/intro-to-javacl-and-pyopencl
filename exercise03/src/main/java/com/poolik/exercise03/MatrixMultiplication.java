package com.poolik.exercise03;

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

import static org.bridj.Pointer.allocateFloats;

public class MatrixMultiplication {
  public static final int N = 1024;
  public static final int SIZE = N * N;
  public static final float AVAL = 3.0f;
  public static final float BVAL = 5.0f;
  public static final float TOLERANCE = 0.001f;

  public static void main(String[] args) throws IOException {
    CLContext context = JavaCL.createBestContext(CLPlatform.DeviceFeature.CPU);
    printDeviceInfo(context.getDevices()[0]);
    CLQueue queue = context.createDefaultQueue();

    String src = IOUtils.readText(MatrixMultiplication.class.getResource("/matmul.cl"));
    CLProgram program = context.createProgram(src);
    ByteOrder byteOrder = context.getByteOrder();

    // Create a and b vectors and fill with random float values
    Pointer<Float> aPtr = allocateFloats(SIZE).order(byteOrder);
    Pointer<Float> bPtr = allocateFloats(SIZE).order(byteOrder);
    for (int i = 0; i < SIZE; i++) {
      aPtr.set(i, AVAL);
      bPtr.set(i, BVAL);
    }

    // Create OpenCL input buffers (using the native memory pointers aPtr and bPtr) :
    CLBuffer<Float> a = context.createFloatBuffer(CLMem.Usage.Input, aPtr);
    CLBuffer<Float> b = context.createFloatBuffer(CLMem.Usage.Input, bPtr);
    CLBuffer<Float> out = context.createFloatBuffer(CLMem.Usage.Output, SIZE);

    long start = System.currentTimeMillis();
    CLKernel addVectors = program.createKernel("mmul");
    addVectors.setArgs(N, a, b, out);
    int[] globalSizes = new int[]{N, N};
    CLEvent addEvt = addVectors.enqueueNDRange(queue, globalSizes);

    Pointer<Float> c = out.read(queue, addEvt); // blocks until mmul finished
    checkResults(c, (System.currentTimeMillis() - start) / 1000.0);
  }

  private static void checkResults(Pointer<Float> c, double runTime) {
    double mflops = 2.0 * N * N * N / (1000000.0 * runTime);
    System.out.printf("%f seconds at %f MFLOPS%n", runTime, mflops);
    float expectedValue = N * AVAL * BVAL;
    float errors = 0.0f;
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) {
        float err = c.get(i * N + j) - expectedValue;
        errors += err * err;
      }
    }
    if (errors > TOLERANCE) System.out.printf("Errors in multiplication: %f%n", errors);
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
