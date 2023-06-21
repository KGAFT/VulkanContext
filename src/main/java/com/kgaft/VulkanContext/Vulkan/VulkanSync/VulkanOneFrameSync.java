package com.kgaft.VulkanContext.Vulkan.VulkanSync;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanOneFrameSync {
    private long availableSemaphore;
    private long waitSemaphore;
    private VulkanDevice device;
    private long fence;
    private boolean firstFrame;

    public VulkanOneFrameSync(VulkanDevice device) {
        this.device = device;
    }
    
    private void createSyncObjects(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            VkSemaphoreCreateInfo createInfo = VkSemaphoreCreateInfo.calloc(stack);
            createInfo.sType$Default();
            long[] output = new long[1];
            vkCreateSemaphore(device.getDevice(), createInfo, null, output);
            this.availableSemaphore = output[0];
            vkCreateSemaphore(device.getDevice(), createInfo, null, output);
            this.waitSemaphore = output[0];
            
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            fenceInfo.sType$Default();
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);
            vkCreateFence(device.getDevice(), fenceInfo, null, output);
            this.fence = output[0];
        }
    }
    
    private void destroy(){
        vkDestroySemaphore(device.getDevice(), availableSemaphore, null);
        vkDestroySemaphore(device.getDevice(), waitSemaphore, null);
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
    
    
}
