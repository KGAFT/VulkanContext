package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDeviceBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.util.List;

import static com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanQueue.PRESENT_QUEUE;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

public class DeviceSuitability {


    public static boolean isDeviceSuitable(MemoryStack stack, VkPhysicalDevice device, VulkanDeviceBuilder builder, DeviceSuitabilityResults results) {
        if(!checkDeviceExtensions(stack, device, builder.getRequiredExtensions())){
            return false;
        }

        int[] queueFamilyCount = new int[1];
        vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);
        VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.calloc(queueFamilyCount[0], stack);
        vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);
        int count = 0;
        for (VkQueueFamilyProperties queueFamily : queueFamilies) {
            if (queueFamily.queueCount() > 0) {
                for (Integer requiredQueue : builder.getRequiredQueues()) {
                    if ((queueFamily.queueFlags() & requiredQueue) != 0) {
                        results.getRequiredQueues().put(requiredQueue, count);
                    }
                }
            }
            if (builder.isNeedPresentationSupport()) {
                int[] isSupport = new int[1];
                vkGetPhysicalDeviceSurfaceSupportKHR(device, count, builder.getSurface(), isSupport);
                if (queueFamily.queueCount() > 0 && isSupport[0] > 0) {
                    results.getRequiredQueues().put(PRESENT_QUEUE, count);
                }
            }
        }
        if(!checkResults(results, builder)){
            return false;
        }
        if(builder.isNeedPresentationSupport()){
            gatherRequiredPresentationInfo(device, stack, builder.getSurface(), results);
        }
        VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc();
        vkGetPhysicalDeviceProperties(device, properties);
        results.setProperties(properties);
        results.setBaseDevice(device);
        results.setRequiredExtensions(builder.getRequiredExtensions());
        results.setEnaledFeatures(builder.getDeviceFeatures());
        return true;
    }

    private static boolean checkDeviceExtensions(MemoryStack stack, VkPhysicalDevice device, List<String> requiredExtensions) {
        int[] extensionCount = new int[1];
        vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, extensionCount, null);
        VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.calloc(extensionCount[0], stack);
        vkEnumerateDeviceExtensionProperties(
                device,
                (ByteBuffer) null,
                extensionCount,
                availableExtensions);
        for (String requiredExtension : requiredExtensions) {
            boolean found = false;
            for (VkExtensionProperties availableExtension : availableExtensions) {
                if(availableExtension.extensionNameString().equals(requiredExtension)){
                    found = true;
                    break;
                }

            }
            if(!found){
                return false;
            }
        }
        return true;
    }

    private static void gatherRequiredPresentationInfo(VkPhysicalDevice device, MemoryStack stack, long surface, DeviceSuitabilityResults results){
        VkSurfaceCapabilitiesKHR surfaceCapabilitiesKHR = VkSurfaceCapabilitiesKHR.calloc();
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, surfaceCapabilitiesKHR);
        results.setCapabilitiesKHR(surfaceCapabilitiesKHR);
        int[] formatCount = new int[1];
        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, formatCount, null);
        if(formatCount[0]!=0){
            VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(formatCount[0]);
            results.setSurfaceFormats(surfaceFormats);
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, formatCount, results.getSurfaceFormats());
        }
        int[] presentModeCount = new int[1];
        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, presentModeCount, null);
        if(presentModeCount[0]!=0){
            int[] presentModes = new int[presentModeCount[0]];
            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, presentModeCount, presentModes);
            results.setPresentModes(presentModes);
        }
    }

    private static boolean checkResults(DeviceSuitabilityResults results, VulkanDeviceBuilder builder) {
        for (Integer requiredQueue : builder.getRequiredQueues()) {
            if(!results.getRequiredQueues().containsKey(requiredQueue)){
                return false;
            }
        }
        if(builder.isNeedPresentationSupport()){
            if(!results.getRequiredQueues().containsKey(PRESENT_QUEUE)){
                return false;
            }
        }
        return true;
    }
}
