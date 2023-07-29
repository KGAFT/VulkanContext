package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

import java.util.ArrayList;
import java.util.List;

public class VulkanDeviceBuilder extends DestroyableObject {
    private List<Integer> requiredQueues = new ArrayList<>();
    private List<String> requiredExtensions = new ArrayList<>();
    private boolean needPresentationSupport = false;
    private VkPhysicalDeviceFeatures deviceFeatures;
    private long surface;
    public VulkanDeviceBuilder(){
        deviceFeatures = VkPhysicalDeviceFeatures.calloc();
    }
    /**
     * @param extension specify any vulkan extension, like VK_KHR_RAY_TRACING_PIPELINE_EXTENSION_NAME or VK_KHR_SWAPCHAIN_EXTENSION_NAME
     */
    public void addExtension(String extension){
        requiredExtensions.add(extension);
    }

    /**
     * @param queue specify any vulkan queue, like VK_QUEUE_GRAPHICS_BIT
     */
    public void addRequiredQueue(int queue){
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

    public List<String> getRequiredExtensions() {
        return requiredExtensions;
    }

    public boolean isNeedPresentationSupport() {
        return needPresentationSupport;
    }

    public VkPhysicalDeviceFeatures getDeviceFeatures() {
        return deviceFeatures;
    }


    public long getSurface() {
        return surface;
    }
    /**
     * This method is required to call if you want presentation support
     * @param surface your rendering surface
     */
    public void setSurface(long surface) {
        this.surface = surface;
    }

    protected void clear(){
        deviceFeatures.clear();
        requiredQueues.clear();
        requiredExtensions.clear();
        needPresentationSupport = false;
        surface = 0;
    }

    @Override
    public void destroy() {
        deviceFeatures.free();
        super.destroy();
    }
}
