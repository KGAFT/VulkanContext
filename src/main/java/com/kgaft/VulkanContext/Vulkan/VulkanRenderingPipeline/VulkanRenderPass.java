package com.kgaft.VulkanContext.Vulkan.VulkanRenderingPipeline;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanRenderPass extends DestroyableObject {

    private VulkanDevice device;
    private long renderPass;
    private List<Long> frameBuffers = new ArrayList<>();
    private List<Long> depthImages = new ArrayList<>();
    private List<Long> depthImageMemories = new ArrayList<>();
    private List<Long> depthImageViews = new ArrayList<>();
    private int depthFormat;
    private boolean isOutput = false;
    private int attachmentCount;

    public VulkanRenderPass(VulkanDevice device, List<Long> images, int width, int height, int imagePerStepAmount, int imageFormat, boolean output) {
        this.attachmentCount = imagePerStepAmount;
        this.isOutput = output;
        this.device = device;
        createRenderPass(imageFormat, imagePerStepAmount, output);
        createDepthResources(width, height, images.size() / imagePerStepAmount);
        createFrameBuffers(images.size() / imagePerStepAmount, width, height, images, imagePerStepAmount);
    }

    @Override
    public void destroy() {
        for (long item : frameBuffers) {
            vkDestroyFramebuffer(device.getDevice(), item, null);
        }
        for (long item : depthImageViews) {
            vkDestroyImageView(device.getDevice(), item, null);
        }
        for (long item : depthImages) {
            vkDestroyImage(device.getDevice(), item, null);
        }
        for (long item : depthImageMemories) {
            vkFreeMemory(device.getDevice(), item, null);
        }
        vkDestroyRenderPass(device.getDevice(), renderPass, null);
        depthImages.clear();
        frameBuffers.clear();
        depthImageViews.clear();
        depthImageMemories.clear();
        super.destroy();
    }

    public List<Long> getFrameBuffers() {
        return frameBuffers;
    }

    public List<Long> getDepthImages() {
        return depthImages;
    }

    public List<Long> getDepthImageViews() {
        return depthImageViews;
    }
    
    
    
    public void recreate(List<Long> images, int width, int height, int imagePerStepAmount, int imageFormat) {
        this.attachmentCount = imagePerStepAmount;
        vkDeviceWaitIdle(device.getDevice());
        destroy();
        super.destroyed = false;
        createRenderPass(imageFormat, imagePerStepAmount, isOutput);
        createDepthResources(width, height, images.size() / imagePerStepAmount);
        createFrameBuffers(images.size() / imagePerStepAmount, width, height, images, imagePerStepAmount);
    }

    private void createRenderPass(int format, int attachImagesAmount, boolean output) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkAttachmentDescription depthAttachment = VkAttachmentDescription.calloc(stack);
            depthAttachment.format(findDepthFormat());
            depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_GENERAL);

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(attachImagesAmount + 1, stack);
            prepareColorAttachmentDescription(attachments, format, attachImagesAmount, output);
            VkAttachmentReference.Buffer references = VkAttachmentReference.calloc(attachImagesAmount, stack);
            prepareColorReference(references, attachImagesAmount);
            references.rewind();

            VkAttachmentReference depthAttachmentRef = VkAttachmentReference.calloc(stack);
            depthAttachmentRef.attachment(attachments.capacity() - 1);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_GENERAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.pColorAttachments(references);
            subpass.pDepthStencilAttachment(depthAttachmentRef);

            subpass.pInputAttachments(null);
            subpass.pResolveAttachments(null);

            VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
            dependency.dstSubpass(0);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.srcAccessMask(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT);

            attachments.put(depthAttachment);
            attachments.rewind();
            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
            renderPassInfo.sType$Default();
            renderPassInfo.pAttachments(attachments);
            renderPassInfo.pSubpasses(subpass);
            renderPassInfo.pDependencies(dependency);

            long[] res = new long[1];
            if (vkCreateRenderPass(device.getDevice(), renderPassInfo, null, res) != VK_SUCCESS) {
                throw new RuntimeException("failed to create render pass!");
            }
            this.renderPass = res[0];
        }

    }

    private void createFrameBuffers(int amount, int width, int height, List<Long> imagesToAttach, int imagePerStepAmount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (int i = 0; i < amount; i++) {
                LongBuffer attachments = stack.callocLong(imagePerStepAmount + 1);
                for (int si = i * imagePerStepAmount; si < i * imagePerStepAmount + imagePerStepAmount; ++si) {
                    attachments.put(imagesToAttach.get(si));
                }
                attachments.put(depthImageViews.get(i));
                attachments.rewind();
                VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
                framebufferInfo.sType$Default();
                framebufferInfo.renderPass(renderPass);
                 
                framebufferInfo.pAttachments(attachments);
                framebufferInfo.width(width);
                framebufferInfo.height(height);
                framebufferInfo.layers(1);
                long[] res = new long[1];
                if (vkCreateFramebuffer(device.getDevice(), framebufferInfo, null, res) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create framebuffer!");
                }
                frameBuffers.add(res[0]);
            }
        }

    }

    private void createDepthResources(int width, int height, int imagesAmount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int depthFormat = findDepthFormat();
            this.depthFormat = depthFormat;
            for (int i = 0; i < imagesAmount; i++) {
                VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
                imageInfo.sType$Default();
                imageInfo.imageType(VK_IMAGE_TYPE_2D);
                imageInfo.extent().width(width);
                imageInfo.extent().height(height);
                imageInfo.extent().depth(1);
                imageInfo.mipLevels(1);
                imageInfo.arrayLayers(1);
                imageInfo.format(depthFormat);
                imageInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
                imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
                imageInfo.usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT|VK_IMAGE_USAGE_TRANSFER_SRC_BIT);
                imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
                imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
                imageInfo.flags(0);
                long[] img = new long[1];
                long[] mem = new long[1];
                device.createImageWithInfo(stack, imageInfo, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, img, mem);
                depthImages.add(img[0]);
                depthImageMemories.add(mem[0]);

                VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
                viewInfo.sType$Default();
                viewInfo.image(depthImages.get(i));
                viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
                viewInfo.format(depthFormat);
                viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
                viewInfo.subresourceRange().baseMipLevel(0);
                viewInfo.subresourceRange().levelCount(1);
                viewInfo.subresourceRange().baseArrayLayer(0);
                viewInfo.subresourceRange().layerCount(1);

                long[] view = new long[1];
                if (vkCreateImageView(device.getDevice(), viewInfo, null, view) != VK_SUCCESS) {
                    throw new RuntimeException("failed to create texture image view!");
                }
                depthImageViews.add(view[0]);

            }
        }
    }

    private int findDepthFormat() {
        List<Integer> candidates = new ArrayList<>();
        candidates.add(VK_FORMAT_D32_SFLOAT);
        candidates.add(VK_FORMAT_D32_SFLOAT_S8_UINT);
        candidates.add(VK_FORMAT_D24_UNORM_S8_UINT);
        return device.findSupportedFormat(candidates,
                VK_IMAGE_TILING_OPTIMAL,
                VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT
        );
    }

    private void prepareColorAttachmentDescription(VkAttachmentDescription.Buffer result, int format, int amount, boolean output) {
        for (int i = 0; i < amount; ++i) {
            result.format(format);
            result.samples(VK_SAMPLE_COUNT_1_BIT);
            result.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            result.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            result.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            result.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            result.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            result.finalLayout(output ? KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR : VK_IMAGE_LAYOUT_GENERAL);
            result.get();
        }
    }

    private void prepareColorReference(VkAttachmentReference.Buffer references, int amount) {
        for (int i = 0; i < amount; ++i) {

            references.attachment(i);
            references.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            references.get();
        }
    }

    public long getRenderPass() {
        return renderPass;
    }
}
