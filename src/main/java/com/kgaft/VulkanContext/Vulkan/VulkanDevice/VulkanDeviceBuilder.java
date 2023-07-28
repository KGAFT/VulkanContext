package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import com.kgaft.VulkanContext.Vulkan.VulkanInstanceBuilder;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import java.util.ArrayList;
import java.util.List;

public class VulkanDeviceBuilder {
    private List<Integer> requiredQueues = new ArrayList<>();
    private List<Integer> requiredExtensions = new ArrayList<>();
    private boolean needPresentationSupport = false;
    private VkPhysicalDeviceFeatures deviceFeatures;

    protected VulkanDeviceBuilder(){
        deviceFeatures = VkPhysicalDeviceFeatures.calloc();
    }
    /**
     * @param extension specify any vulkan extension, like VK_KHR_RAY_TRACING_PIPELINE_EXTENSION_NAME or VK_KHR_SWAPCHAIN_EXTENSION_NAME
     */
    public void addExtension(int extension){
        requiredExtensions.add(extension);
    }

    /**
     * @param queue specify any vulkan queue, like VK_QUEUE_GRAPHICS_BIT
     */
    public void adRequiredQueue(int queue){
        requiredQueues.add(queue);
    }

    public void enablePresentationSupport(){
        this.needPresentationSupport = true;
    }

    /**
     * @return device features setup struct, enable required features
     */
    public VkPhysicalDeviceFeatures setupRequiredDeviceFeatures(){
        return deviceFeatures;
    }

    public List<Integer> getRequiredQueues() {
        return requiredQueues;
    }

    public List<Integer> getRequiredExtensions() {
        return requiredExtensions;
    }

    public boolean isNeedPresentationSupport() {
        return needPresentationSupport;
    }

    public VkPhysicalDeviceFeatures getDeviceFeatures() {
        return deviceFeatures;
    }
}
