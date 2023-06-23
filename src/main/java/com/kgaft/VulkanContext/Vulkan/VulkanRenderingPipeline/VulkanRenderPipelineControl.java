package com.kgaft.VulkanContext.Vulkan.VulkanRenderingPipeline;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanSync.VulkanSyncManager;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;

public class VulkanRenderPipelineControl {

    private VulkanSyncManager syncManager;
    private VulkanDevice device;
    private VulkanRenderPass renderPass;
    private List<VkCommandBuffer> commandBuffers = new ArrayList();
    private int currentCmd;

    public VulkanRenderPipelineControl(VulkanSyncManager syncManager, VulkanDevice device, VulkanRenderPass renderPass) {
        this.syncManager = syncManager;
        this.device = device;
        this.renderPass = renderPass;
        createCommandBuffer(syncManager.getCurrentMode());
    }
    
    public void beginRender(VkCommandBuffer[] cmd, long[] frameBuffer){
        currentCmd = syncManager.prepareFrame();
        VkCommandBuffer tCmd = commandBuffers.get(currentCmd);
        syncManager.beginCommandBuffer(tCmd);
        cmd[0] = tCmd;
        frameBuffer[0] = renderPass.getFrameBuffers().get(currentCmd);
    }
    
    public void endRender(){
        VK13.vkEndCommandBuffer(commandBuffers.get(currentCmd));
        syncManager.endRender(commandBuffers.get(currentCmd));
    }

    public int getCurrentCmd() {
        return currentCmd;
    }
    
    
    
    private void createCommandBuffer(int amount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType$Default();
            allocInfo.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(device.getCommandPool());
            allocInfo.commandBufferCount(amount);
            PointerBuffer res = stack.callocPointer(amount);
            if (VK13.vkAllocateCommandBuffers(device.getDevice(), allocInfo, res)
                    != VK13.VK_SUCCESS) {
                throw new RuntimeException("failed to allocate command buffers!");
            }
            while(res.hasRemaining()){
                commandBuffers.add(new VkCommandBuffer(res.get(), device.getDevice()));
            }
        }
    }

}
