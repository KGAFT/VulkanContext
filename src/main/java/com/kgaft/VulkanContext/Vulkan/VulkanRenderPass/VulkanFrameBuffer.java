package com.kgaft.VulkanContext.Vulkan.VulkanRenderPass;

import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanImage.VulkanImageView;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanFrameBuffer extends DestroyableObject {
    private List<VulkanImageView> attachments;
    private VulkanDevice device;
    private long renderPass;
    private long frameBuffer;
    protected VulkanFrameBuffer(MemoryStack stack, List<VulkanImageView> attachments, long renderPass) {
        this.attachments = attachments;
        this.renderPass = renderPass;
        create(stack);
    }

    private void create(MemoryStack stack){
        int lastWidth = 0;
        int lastHeight = 0;
        LongBuffer attachmentsBuf = stack.callocLong(attachments.size());
        for (VulkanImageView attachment : attachments) {
            if(lastWidth==0 || lastHeight==0){
                lastWidth = attachment.getImage().getWidth();
                lastHeight = attachment.getImage().getHeight();
            }
            else if(lastWidth!=attachment.getImage().getWidth() || lastHeight!=attachment.getImage().getHeight()){
                throw new RuntimeException("Error all images in frame buffer must have the same resolution");
            }
            else{
                attachmentsBuf.put(attachment.getHandle());
            }
        }
        attachmentsBuf.rewind();
        VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
        framebufferInfo.sType$Default();
        framebufferInfo.pAttachments(attachmentsBuf);
        framebufferInfo.renderPass(this.renderPass);
        framebufferInfo.width(lastWidth);
        framebufferInfo.height(lastHeight);
        framebufferInfo.layers(1);

        long[] res = new long[1];

        if (vkCreateFramebuffer(device.getDevice(), framebufferInfo, null, res) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create framebuffer!");
        }
        this.frameBuffer = res[0];
    }

    public void recreate(MemoryStack stack, List<VulkanImageView> attachments, long renderPass){
        this.attachments = attachments;
        this.renderPass = renderPass;
        destroy();
        super.destroyed = false;
        create(stack);
    }

    @Override
    public void destroy() {
        vkDestroyFramebuffer(device.getDevice(), frameBuffer, null);
        super.destroy();
    }
}
