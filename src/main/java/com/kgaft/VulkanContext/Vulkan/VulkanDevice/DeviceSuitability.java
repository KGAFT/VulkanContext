package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK13.*;

class SwapChainSupportDetails{
    public VkSurfaceCapabilitiesKHR capabilities;
    public ArrayList<VkSurfaceFormatKHR> formats = new ArrayList<>();
    public ArrayList<Integer> presentModes = new ArrayList<>();
}

class QueueFamilyIndices{
    public long graphicsFamily;
    public long presentFamily;
    public boolean graphicsFamilyHasValue = false;
    public boolean presentFamilyHasValue = false;

    boolean isComplete(){
        return graphicsFamilyHasValue && presentFamilyHasValue;
    }
}

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
        SwapChainSupportDetails supportDetails = new SwapChainSupportDetails();
        supportDetails.capabilities = VkSurfaceCapabilitiesKHR.calloc();
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, supportDetails.capabilities);
        int[] formatsCount = new int[1];
        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, formatsCount, null);
        VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(formatsCount[0]);
        while(surfaceFormats.remaining()>0){
            supportDetails.formats.add(surfaceFormats.get());
        }
        surfaceFormats.free();
        int[] presentModeCount = new int[1];
        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, presentModeCount, null);
        int[] presentModes = new int[presentModeCount[0]];
        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, presentModeCount, presentModes);
        for (int presentMode : presentModes) {
            supportDetails.presentModes.add(presentMode);
        }
        return supportDetails;
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
}
