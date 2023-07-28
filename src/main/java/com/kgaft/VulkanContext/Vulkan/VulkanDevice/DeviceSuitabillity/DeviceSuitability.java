package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDeviceBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

public class DeviceSuitability {
    public static boolean isDeviceSuitable(MemoryStack stack, VkPhysicalDevice device, VulkanDeviceBuilder builder, DeviceSuitabilityResults results){
        int[] queueFamilyCount = new int[1];
        vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);
        VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.calloc(queueFamilyCount[0], stack);
        vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);
        int count = 0;
        for (VkQueueFamilyProperties queueFamily : queueFamilies) {
            if(queueFamily.queueCount()>0){
                for (Integer requiredQueue : builder.getRequiredQueues()) {
                    if((queueFamily.queueFlags() & requiredQueue)!=0){
                        results.getRequiredQueues().put(requiredQueue, count);
                    }
                }
            }
            if(builder.isNeedPresentationSupport()){

            }
        }
    }
}
