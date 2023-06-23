package com.kgaft.VulkanContext.Vulkan.VulkanSync;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanSwapChain;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.*;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanSyncManager extends DestroyableObject{

    private VulkanOneFrameSync oneFrameSync;
    private VulkanThreeFrameSync threeFrameSync;
    private VulkanSwapChain swapChain;
    private int currentImage;

    public VulkanSyncManager(VulkanDevice device, VulkanSwapChain swapChain) {
        this.swapChain = swapChain;
        if (swapChain != null) {
            threeFrameSync = new VulkanThreeFrameSync(device);
        } else {
            oneFrameSync = new VulkanOneFrameSync(device);
        }
    }

    public int prepareFrame() {
        if (threeFrameSync != null) {
            currentImage = threeFrameSync.prepareForNextImage(swapChain.getSwapChain());
            return currentImage;
        } else {
            return 0;
        }
    }

    public int getCurrentImage() {
        return currentImage;
    }

    public void beginCommandBuffer(VkCommandBuffer cmd) {
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc();
        beginInfo.sType$Default();
        if (vkBeginCommandBuffer(cmd, beginInfo) != VK13.VK_SUCCESS) {
            throw new RuntimeException("Failed to begin commandBuffer");
        }
    }
    
    public void endRender(VkCommandBuffer cmd){
        if(threeFrameSync!=null){
            currentImage = threeFrameSync.submitCommandBuffer(cmd, swapChain.getSwapChain(), currentImage);
        }
        else{
            oneFrameSync.submitCommandBuffer(cmd);
        }
    }

    public VulkanSwapChain getSwapChain() {
        return swapChain;
    }

    @Override
    public void destroy() {
        if(threeFrameSync==null){
            oneFrameSync.destroy();
        }
        else{
            threeFrameSync.destroy();
        }
        super.destroy();
    }
    
    
    
    public int getCurrentMode(){
        return threeFrameSync!=null?3:1;
    }
    
}
