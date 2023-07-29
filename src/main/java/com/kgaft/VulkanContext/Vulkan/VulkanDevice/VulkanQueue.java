package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import org.lwjgl.vulkan.VkQueue;

public class VulkanQueue {
    private int queueType;
    private VkQueue base;

    

    protected VulkanQueue(int queueType, VkQueue base) {
      this.queueType = queueType;
      this.base = base;
    }

    public int getQueueType() {
        return queueType;
    }

    public VkQueue getBase() {
        return base;
    }

}
