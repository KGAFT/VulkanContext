package com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability;

import java.util.ArrayList;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class SwapChainSupportDetails {
    public VkSurfaceCapabilitiesKHR capabilities;
    public ArrayList<VkSurfaceFormatKHR> formats = new ArrayList<>();
    public ArrayList<Integer> presentModes = new ArrayList<>();
}
