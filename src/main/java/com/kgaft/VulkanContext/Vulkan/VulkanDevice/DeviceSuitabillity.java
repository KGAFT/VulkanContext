package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.util.ArrayList;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK13.*;

class SwapChainSupportDetails{
    public VkSurfaceCapabilitiesKHR capabilities;
    public ArrayList<VkSurfaceFormatKHR> formats = new ArrayList<>();
    public ArrayList<Long> presentModes = new ArrayList<>();
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

class DeviceSuitability {
    QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, long surface){
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

}
