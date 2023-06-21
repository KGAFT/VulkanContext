package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK13.*;


public class DeviceSuitability {
    private static ArrayList<String> requiredDeviceExtensions = new ArrayList<>();

    static{
        populateRequiredExtensions();
    }
    private static void populateRequiredExtensions(){
        requiredDeviceExtensions.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
    }
    public static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, long surface){
        try(MemoryStack stack = MemoryStack.stackPush()){
            QueueFamilyIndices result = new QueueFamilyIndices();
            int[] queueFamilyCount = new int[1];
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);
            VkQueueFamilyProperties.Buffer properties = VkQueueFamilyProperties.calloc(queueFamilyCount[0], stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, properties);
            for(int i = 0; i<properties.capacity(); i++){
                VkQueueFamilyProperties queueFamily = properties.get(i);
                if (queueFamily.queueCount() > 0 && (queueFamily.queueFlags() & VK_QUEUE_GRAPHICS_BIT)!=0)
                {
                    result.graphicsFamily = i;
                    result.graphicsFamilyHasValue = true;
                }
                int[] presentSupport = new int[1];
                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);
                if(queueFamily.queueCount()>0 && presentSupport[0]>0){
                    result.presentFamily = i;
                    result.presentFamilyHasValue = true;
                }
                if(result.isComplete()){
                    break;
                }
            }
            return result;
        }
    }
    public static boolean checkDeviceExtensions(VkPhysicalDevice device){
        try(MemoryStack stack = MemoryStack.stackPush()){
            int[] extensionCount = new int[1];
            vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, extensionCount, null);
            VkExtensionProperties.Buffer deviceExtensions = VkExtensionProperties.calloc(extensionCount[0], stack);
            vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, extensionCount, deviceExtensions);
            while(deviceExtensions.remaining()>0){
                VkExtensionProperties property = deviceExtensions.get();
                requiredDeviceExtensions.remove(property.extensionNameString());
            }
            boolean result = requiredDeviceExtensions.isEmpty();
            populateRequiredExtensions();
            return result;
        }
    }

    

    public static SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, long surface){
            SwapChainSupportDetails details = new SwapChainSupportDetails();

            details.capabilities = VkSurfaceCapabilitiesKHR.calloc();
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);
            int[] count = new int[1];
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);
            if(count[0] != 0) {
                VkSurfaceFormatKHR.Buffer buffer = VkSurfaceFormatKHR.calloc(count[0]);
                vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, buffer);
                while(buffer.hasRemaining()){
                    details.formats.add(buffer.get());
                }
            }

            vkGetPhysicalDeviceSurfacePresentModesKHR(device,surface, count, null);

            if(count[0] != 0) {
                int[] presentModes = new int[count[0]];
                vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, presentModes);
                for(int i : presentModes){
                    details.presentModes.add(i);
                }
            }
           
            return details;
    }

    public static boolean isDeviceSuitable(VkPhysicalDevice device, long surface){
        QueueFamilyIndices indices = findQueueFamilies(device, surface);
        boolean deviceExtensionsSupport = checkDeviceExtensions(device);
        boolean swapChainAdequate = false;
        if (deviceExtensionsSupport)
        {
            SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device, surface);
            swapChainAdequate = !swapChainSupport.formats.isEmpty() && !swapChainSupport.presentModes.isEmpty();
        }
        VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.calloc();
        vkGetPhysicalDeviceFeatures(device, supportedFeatures);
        boolean result =  indices.isComplete() && deviceExtensionsSupport && swapChainAdequate &&
                supportedFeatures.samplerAnisotropy();
        supportedFeatures.free();
        return result;
    }
    public static ArrayList<String> getRequiredDeviceExtensions() {
        return requiredDeviceExtensions;
    }
}
