package org.devcloud.ap.utils;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class OperatingSystemInfo {

    private static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public static String getCPUUsage() {
        return String.format("%.2f", getRawCPUUsage()) + "%";
    }

    public static String getMemoryUsage() {
        return String.format("%d MB", getRawMemoryUsage());
    }

    public static double getRawCPUUsage() {
        // Get CPU usage
        return operatingSystemMXBean.getCpuLoad() * 100;
    }

    public static long getRawMemoryUsage() {
        // Siehe https://stackoverflow.com/questions/3571203/what-are-runtime-getruntime-totalmemory-and-freememory für mehr Infos
        // Bytes
        final long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        // Bytes to MB
        return usedMem / 1024 / 1024;
    }
}
