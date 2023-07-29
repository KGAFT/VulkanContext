package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity;

import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.util.HashMap;
import java.util.List;

public class DeviceSuitabilityResults extends DestroyableObject {
    private HashMap<Integer, Integer> requiredQueues = new HashMap<>();
    private VkSurfaceFormatKHR.Buffer surfaceFormats = null;
    private int[] presentModes;
    private VkSurfaceCapabilitiesKHR capabilitiesKHR = null;
    private VkPhysicalDeviceProperties properties = null;
    private VkPhysicalDevice base;
    private List<String> requiredExtensions;
    private VkPhysicalDeviceFeatures enaledFeatures;
    protected void setRequiredQueues(HashMap<Integer, Integer> requiredQueues) {
        this.requiredQueues = requiredQueues;
    }

    protected void setSurfaceFormats(VkSurfaceFormatKHR.Buffer surfaceFormats) {
        this.surfaceFormats = surfaceFormats;
    }

    protected void setPresentModes(int[] presentModes) {
        this.presentModes = presentModes;
    }

    
    protected void setEnaledFeatures(VkPhysicalDeviceFeatures enaledFeatures) {
      this.enaledFeatures = enaledFeatures;
    }

    protected void setCapabilitiesKHR(VkSurfaceCapabilitiesKHR capabilitiesKHR) {
        this.capabilitiesKHR = capabilitiesKHR;
    }
    
    protected void setRequiredExtensions(List<String> requiredExtensions) {
      this.requiredExtensions = requiredExtensions;
    }

    protected void setBaseDevice(VkPhysicalDevice base) {
        this.base = base;
    }

    public VkPhysicalDevice getBaseDevice() {
        return base;
    }

    

    public VkPhysicalDeviceFeatures getEnaledFeatures() {
        return enaledFeatures;
    }

    /**
     * Key - queue type
     * Value - queue index
     */

    public HashMap<Integer, Integer> getRequiredQueues() {
        return requiredQueues;
    }

    public VkSurfaceFormatKHR.Buffer getSurfaceFormats() {
        return surfaceFormats;
    }

    public int[] getPresentModes() {
        return presentModes;
    }

    public VkSurfaceCapabilitiesKHR getCapabilitiesKHR() {
        return capabilitiesKHR;
    }

    public VkPhysicalDeviceProperties getProperties() {
        return properties;
    }

    protected void setProperties(VkPhysicalDeviceProperties properties) {
        this.properties = properties;
    }

    

    public List<String> getRequiredExtensions() {
      return requiredExtensions;
    }

    @Override
    public void destroy() {
        if (surfaceFormats != null) {
            surfaceFormats.free();
        }
        if (capabilitiesKHR != null) {
            capabilitiesKHR.free();
        }
        properties.free();
        super.destroy();
    }
}
