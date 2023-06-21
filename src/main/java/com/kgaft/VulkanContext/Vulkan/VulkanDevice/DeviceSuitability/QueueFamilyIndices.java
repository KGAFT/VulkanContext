package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability;

public class QueueFamilyIndices {
    public int graphicsFamily;
    public int presentFamily;
    public boolean graphicsFamilyHasValue = false;
    public boolean presentFamilyHasValue = false;

    boolean isComplete(){
        return graphicsFamilyHasValue && presentFamilyHasValue;
    }
}
