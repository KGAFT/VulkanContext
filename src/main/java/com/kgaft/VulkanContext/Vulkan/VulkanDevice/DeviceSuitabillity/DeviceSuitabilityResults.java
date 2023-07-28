package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity;

import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceSuitabilityResults extends DestroyableObject {
    private HashMap<Integer, Integer> requiredQueues = new HashMap<>();
    private VkSurfaceFormatKHR.Buffer surfaceFormats = null;
    private int[] presentModes;
    private VkSurfaceCapabilitiesKHR capabilitiesKHR = null;

    protected void setRequiredQueues(HashMap<Integer, Integer> requiredQueues) {
        this.requiredQueues = requiredQueues;
    }

    protected void setSurfaceFormats(VkSurfaceFormatKHR.Buffer surfaceFormats) {
        this.surfaceFormats = surfaceFormats;
    }

    protected void setPresentModes(int[] presentModes) {
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

    public int[] getPresentModes() {
        return presentModes;
    }

    public VkSurfaceCapabilitiesKHR getCapabilitiesKHR() {
        return capabilitiesKHR;
    }

    @Override
    public void destroy() {
        if(surfaceFormats!=null){
            surfaceFormats.free();
        }
        if(capabilitiesKHR!=null){
            capabilitiesKHR.free();
        }
        super.destroy();
    }
}
