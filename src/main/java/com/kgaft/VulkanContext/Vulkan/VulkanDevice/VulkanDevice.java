package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import com.kgaft.VulkanContext.MemoryUtils.MemoryStackUtils;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity.DeviceSuitability;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity.DeviceSuitabilityResults;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;

import static org.lwjgl.vulkan.VK13.*;

import java.util.HashMap;

public class VulkanDevice {
    private static VulkanDeviceBuilder deviceBuilderInstance = new VulkanDeviceBuilder();
    public static HashMap<VkPhysicalDevice, DeviceSuitabilityResults>
    enumerateSupportedDevices(VulkanInstance instance, VulkanDeviceBuilder deviceBuilder) {
        HashMap<VkPhysicalDevice, DeviceSuitabilityResults> results = new HashMap<>();
        MemoryStack stack = MemoryStackUtils.acquireStack();
        int[] physicalDeviceCount = new int[1];
        vkEnumeratePhysicalDevices(instance.getInstance(), physicalDeviceCount, null);
        PointerBuffer pb = stack.callocPointer(physicalDeviceCount[0]);
        vkEnumeratePhysicalDevices(instance.getInstance(), physicalDeviceCount, pb);

        while (pb.hasRemaining()) {
            VkPhysicalDevice device = new VkPhysicalDevice(pb.get(), instance.getInstance());
            DeviceSuitabilityResults suitabilityResults = new DeviceSuitabilityResults();
            if (DeviceSuitability.isDeviceSuitable(stack, device, deviceBuilder, suitabilityResults)) {
                results.put(device, suitabilityResults);
            }
        }
        MemoryStackUtils.freeStack(stack);
        return results;
    }

    public static VulkanDeviceBuilder getDeviceBuilderInstance() {
        return deviceBuilderInstance;
    }

    public static VulkanDevice buildDevice(){
        
        deviceBuilderInstance.clear();
    }
}
