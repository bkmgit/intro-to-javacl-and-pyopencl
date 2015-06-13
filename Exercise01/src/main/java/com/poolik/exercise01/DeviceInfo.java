package com.poolik.exercise01;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;

import java.util.Arrays;
import java.util.List;

public class DeviceInfo {
  public static void main(String[] args) {
    List<CLPlatform> clPlatforms = Arrays.asList(JavaCL.listPlatforms());
    System.out.println("\nNumber of OpenCL platforms: " + clPlatforms.size());
    System.out.println("\n-------------------------");
    clPlatforms.forEach((platform) -> {
      System.out.println("Platform: " + platform.getName());
      System.out.println("Vendor: " + platform.getVendor());
      System.out.println("Version: " + platform.getVersion());
      List<CLDevice> devices = Arrays.asList(platform.listAllDevices(true));
      System.out.println("Number of devices: " + devices.size());
      devices.forEach((device) -> {
        System.out.println("\t-------------------------");
        System.out.println("\t\tName: " + device.getName());
        System.out.println("\t\tVersion: " + device.getVersion());
        System.out.println("\t\tMax. Compute Units: " + device.getMaxComputeUnits());
        System.out.println("\t\tLocal Memory Size: " + (device.getLocalMemSize() / 1024) + " KB");
        System.out.println("\t\tGlobal Memory Size: " + (device.getGlobalMemSize() / (1024 * 1024)) + " MB");
        System.out.println("\t\tMax Alloc Size: " + (device.getMaxMemAllocSize() / (1024 * 1024)) + " MB");
        System.out.println("\t\tMax Work-group Total Size: " + device.getMaxWorkGroupSize());
        System.out.println("\t\tMax Work-group Dims: " + Arrays.toString(device.getMaxWorkItemSizes()));
        System.out.println("\t-------------------------");
      });
    });
    System.out.println("\n-------------------------");
  }
}