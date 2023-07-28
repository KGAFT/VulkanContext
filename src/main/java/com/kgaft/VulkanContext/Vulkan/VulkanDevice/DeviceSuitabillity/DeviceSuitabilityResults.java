package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity;

import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceSuitabilityResults {
    private HashMap<Integer, Integer> requiredQueues = new HashMap<>();
    private VkSurfaceFormatKHR.Buffer surfaceFormats;
    private List<Integer> presentModes = new ArrayList<>();
    private VkSurfaceCapabilitiesKHR capabilitiesKHR;

    protected void setRequiredQueues(HashMap<Integer, Integer> requiredQueues) {
        this.requiredQueues = requiredQueues;
    }

    protected void setSurfaceFormats(VkSurfaceFormatKHR.Buffer surfaceFormats) {
        this.surfaceFormats = surfaceFormats;
    }

    protected void setPresentModes(List<Integer> presentModes) {
        this.presentModes = presentModes;
    }

    protected void setCapabilitiesKHR(VkSurfaceCapabilitiesKHR capabilitiesKHR) {
        this.capabilitiesKHR = capabilitiesKHR;
    }

    public HashMap<Integer, Integer> getRequiredQueues() {
        return requiredQueues;
    }

    public VkSurfaceFormatKHR.Buffer getSurfaceFormats() {
        return surfaceFormats;
    }

    public List<Integer> getPresentModes() {
        return presentModes;
    }

    public VkSurfaceCapabilitiesKHR getCapabilitiesKHR() {
        return capabilitiesKHR;
    }
}
