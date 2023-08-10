package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanBuffer.VulkanBuffer;
import org.joml.Vector4f;
import org.lwjgl.vulkan.*;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanQueue;

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK13.*;

import java.util.ArrayList;
import java.util.List;

public class VulkanImage extends DestroyableObject {
    public static int findDepthFormat(VulkanDevice device) {
        List<Integer> candidates = new ArrayList<>();
        candidates.add(VK_FORMAT_D32_SFLOAT);
        candidates.add(VK_FORMAT_D32_SFLOAT_S8_UINT);
        candidates.add(VK_FORMAT_D24_UNORM_S8_UINT);
        return device.findSupportedFormat(candidates,
                VK_IMAGE_TILING_OPTIMAL,
                VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT
        );
    }

    private VulkanDevice device;
    private long image;
    private long imageMemory;
    private int imageLayout;
    private int imageTiling;
    private int imageFormat;
    private int sharingMode;
    private int samples;
    private int accessMask = 0;
    private int pipelineStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
    private int layerCount;
    private int mipLevels;
    private int width;
    private int height;
    private List<VulkanImageView> imageViews = new ArrayList<>();
    private int imageMemoryProperties;
    private int imageUsage;

    public VulkanImage(VulkanDevice device, VulkanImageBuilder builder) throws BuilderNotPopulatedException {
        this.device = device;
        builder.checkBuilder();
        this.imageLayout = builder.getInitialLayout();
        this.imageTiling = builder.getTiling();
        this.sharingMode = builder.getSharingMode();
        this.samples = builder.getSamples();
        this.imageFormat = builder.getFormat();
        this.layerCount = builder.getArraySize();
        this.mipLevels = builder.getMipLevels();
        this.width = builder.getWidth();
        this.height = builder.getHeight();
        this.imageUsage = builder.getRequiredUsage();
        this.imageMemoryProperties = builder.getImageMemoryProperties();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.image = createImage(stack, builder);
            this.imageMemory = createImageMemory(stack, device, builder.getImageMemoryProperties(), image);
        }

    }


    public void changeLayout(int targetLayout, boolean isDepth, int targetAccessMask, int targetPipelineStage, VulkanQueue queue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            changeLayout(stack, isDepth, targetLayout, targetAccessMask, targetPipelineStage, queue);
        }
    }

    public void changeLayout(MemoryStack stack, boolean isDepth, int targetLayout, int targetAccessMask, int targetPipelineStage,
                             VulkanQueue queue) {
        VkCommandBuffer cmd = queue.beginSingleTimeCommands(stack);
        changeLayout(stack, isDepth, targetLayout, targetAccessMask, targetPipelineStage, cmd);
        queue.endSingleTimeCommands(cmd, stack);
    }

    public void changeLayout(MemoryStack stack, boolean isDepth, int targetLayout, int targetAccessMask, int targetPipelineStage,
                             VkCommandBuffer cmd) {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.oldLayout(this.imageLayout);
        barrier.newLayout(targetLayout);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.image(image);
        barrier.subresourceRange().aspectMask(isDepth ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(this.mipLevels);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(layerCount);

        barrier.srcAccessMask(this.accessMask);
        barrier.dstAccessMask(targetAccessMask);

        vkCmdPipelineBarrier(cmd,
                this.pipelineStage, targetPipelineStage,
                0,
                null,
                null,
                barrier);
        this.accessMask = targetAccessMask;
        this.imageLayout = targetLayout;
        this.pipelineStage = targetPipelineStage;

    }

    public void copyFromImage(MemoryStack stack, VkCommandBuffer cmd, boolean isDepth, VulkanImage src, ImageTarget srcTarget, ImageTarget dstTarget){
        copyImage(stack, cmd, isDepth, this.width, this.height, src.image, src.imageLayout, srcTarget, image, imageLayout, dstTarget);
    }

    public void copyToImage(MemoryStack stack, VkCommandBuffer cmd, boolean isDepth, VulkanImage dst, ImageTarget dstTarget, ImageTarget srcTarget){
        copyImage(stack, cmd, isDepth, this.width, this.height, image, imageLayout, srcTarget, dst.image, dst.imageLayout, dstTarget);
    }
    public void copyFromBuffer(VulkanBuffer buffer, boolean isDepth, VkCommandBuffer cmd, MemoryStack stack, ImageTarget target){
        int lastLayout = this.imageLayout;
        int lastShaderStage = this.pipelineStage;
        int lastAccessMask = this.accessMask;
        changeLayout(stack, isDepth, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, cmd);

        VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
        region.bufferOffset(0);
        region.bufferRowLength(0);
        region.bufferImageHeight(0);
        region.imageSubresource().aspectMask(isDepth ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT);
        region.imageSubresource().mipLevel(target.getMipLevel());
        region.imageSubresource().baseArrayLayer(target.getStartLayerIndex());
        region.imageSubresource().layerCount(target.getLayersAmount());
        region.imageOffset(VkOffset3D.calloc(stack).x(0).y(0).z(0));
        region.imageExtent(VkExtent3D.calloc(stack).width(width).height(height).depth(1));

        vkCmdCopyBufferToImage(cmd, buffer.getBuffer(), image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
        changeLayout(stack, isDepth, lastLayout, lastAccessMask, lastShaderStage, cmd);
    }

    private void copyImage(MemoryStack stack, VkCommandBuffer cmd, boolean isDepth, int width, int height, long src, int srcLayout, ImageTarget srcTarget, long dst, int dstLayout, ImageTarget dstTarget) {
        VkImageCopy.Buffer imageCopy = VkImageCopy.calloc(1, stack);
        imageCopy.srcSubresource().aspectMask(isDepth ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT);
        imageCopy.srcSubresource().layerCount(srcTarget.getLayersAmount());
        imageCopy.srcSubresource().baseArrayLayer(srcTarget.getStartLayerIndex());
        imageCopy.srcSubresource().mipLevel(srcTarget.getMipLevel());
        imageCopy.dstSubresource().baseArrayLayer(dstTarget.getStartLayerIndex());
        imageCopy.dstSubresource().layerCount(srcTarget.getLayersAmount());
        imageCopy.dstSubresource().mipLevel(dstTarget.getMipLevel());
        imageCopy.extent().width(width);
        imageCopy.extent().height(height);
        imageCopy.extent().depth(1);
        vkCmdCopyImage(
                cmd,
                src, srcLayout,
                dst, dstLayout,
                imageCopy);
    }

    public void clearColorImage(VkCommandBuffer cmd, MemoryStack stack, Vector4f newColor, ImageTarget target) {
        VkClearColorValue clearColorValue = VkClearColorValue.calloc(stack);
        clearColorValue.float32(0, newColor.x);
        clearColorValue.float32(1, newColor.y);
        clearColorValue.float32(2, newColor.z);
        clearColorValue.float32(3, newColor.w);
        VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack);
        subresourceRange.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        subresourceRange.baseArrayLayer(target.getStartLayerIndex());
        subresourceRange.layerCount(target.getLayersAmount());
        subresourceRange.baseMipLevel(target.getMipLevel());
        subresourceRange.levelCount(target.getMipLevelCount());
        vkCmdClearColorImage(cmd, image, imageLayout, clearColorValue, subresourceRange);
    }

    public VulkanImageView acquireImageView(MemoryStack stack, boolean isDepth, int type, ImageTarget imageTarget) {
        if (imageTarget.getStartLayerIndex() >= this.layerCount || imageTarget.getStartLayerIndex() < 0) {
            throw new RuntimeException("Failed to acquired image with not existing index: " + imageTarget.getStartLayerIndex());
        }
        if (type == VK_IMAGE_VIEW_TYPE_2D) {
            for (VulkanImageView view : imageViews) {
                if (view.getType() == VK_IMAGE_VIEW_TYPE_2D && view.getArrayLayerIndex() == imageTarget.getStartLayerIndex()
                        && view.getMipLevel() == imageTarget.getMipLevel()
                        && view.getMipLevelAmount() == imageTarget.getMipLevelCount()) {
                    return view;
                }
            }
            long imageViewHandle = createImageView(stack, isDepth, this.image, this.imageFormat, 1,
                    imageTarget.getStartLayerIndex(), imageTarget.getMipLevel(), imageTarget.getMipLevelCount(), type);
            VulkanImageView imageView = new VulkanImageView(device, this, type, imageTarget.getStartLayerIndex(), 1, imageViewHandle);
            imageViews.add(imageView);
            return imageView;
        } else if (type == VK_IMAGE_VIEW_TYPE_2D_ARRAY || type == VK_IMAGE_VIEW_TYPE_CUBE) {
            if (imageTarget.getLayersAmount() > this.layerCount || layerCount < 1 || imageTarget.getStartLayerIndex() >= this.layerCount
                    || imageTarget.getStartLayerIndex() < 0) {
                throw new RuntimeException("Failed to create multiple image view, because layerCount more than image layers");
            }
            for (VulkanImageView view : imageViews) {
                if (view.getType() == type && view.getArrayLayerIndex() == imageTarget.getStartLayerIndex()
                        && view.getLayerCount() == layerCount && view.getMipLevel() == imageTarget.getMipLevel()
                        && view.getMipLevelAmount() == imageTarget.getMipLevelCount()) {
                    return view;
                }
            }
            long viewHandle = createImageView(stack, isDepth, image, this.imageFormat, imageTarget.getLayersAmount(), imageTarget.getStartLayerIndex(), imageTarget.getMipLevel(), imageTarget.getMipLevelCount(), type);
            VulkanImageView view = new VulkanImageView(device, this, type, imageTarget.getStartLayerIndex(), layerCount, viewHandle);
            view.setMipLevel(imageTarget.getMipLevel());
            view.setMipLevelAmount(imageTarget.getMipLevelCount());
            this.imageViews.add(view);
            return view;
        } else {
            throw new RuntimeException("Unsupported type");
        }
    }

    private long createImage(MemoryStack stack, VulkanImageBuilder builder) {

        VkImageCreateInfo createInfo = VkImageCreateInfo.calloc(stack);
        createInfo.sType$Default();
        createInfo.imageType(VK_IMAGE_TYPE_2D);
        createInfo.extent().width(builder.getWidth());
        createInfo.extent().height(builder.getHeight());
        createInfo.extent().depth(1);
        createInfo.mipLevels(builder.getMipLevels());
        createInfo.arrayLayers(builder.getArraySize());
        createInfo.format(builder.getFormat());
        createInfo.tiling(builder.getTiling());
        createInfo.initialLayout(builder.getInitialLayout());
        createInfo.samples(builder.getSamples());
        createInfo.sharingMode(builder.getSharingMode());
        createInfo.flags(builder.getArraySize() >= 6 ? VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT : 0);
        createInfo.usage(builder.getRequiredUsage());
        long[] result = new long[1];
        if (vkCreateImage(device.getDevice(), createInfo, null, result) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image");
        }
        return result[0];

    }

    private long createImageMemory(MemoryStack stack, VulkanDevice device, int properties, long image) {
        VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
        vkGetImageMemoryRequirements(device.getDevice(), image, memRequirements);

        VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
        allocInfo.sType$Default();
        allocInfo.allocationSize(memRequirements.size());
        allocInfo.memoryTypeIndex(device.findMemoryType(memRequirements.memoryTypeBits(), properties, stack));
        long[] res = new long[1];
        if (vkAllocateMemory(device.getDevice(), allocInfo, null, res) != VK_SUCCESS) {
            throw new RuntimeException("failed to allocate image memory!");
        }

        vkBindImageMemory(device.getDevice(), image, res[0], 0);
        return res[0];
    }

    private long createImageView(MemoryStack stack, boolean isDepth,  long image, int format, int layerCount, int baseArrayLayer, int mipLevelIndex, int mipLevelCount, int type) {

        VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
        viewInfo.sType$Default();
        viewInfo.image(image);
        viewInfo.viewType(type);
        viewInfo.format(format);
        viewInfo.subresourceRange().aspectMask(isDepth ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT);
        viewInfo.subresourceRange().baseMipLevel(mipLevelIndex);
        viewInfo.subresourceRange().levelCount(mipLevelCount);
        viewInfo.subresourceRange().baseArrayLayer(baseArrayLayer);
        viewInfo.subresourceRange().layerCount(layerCount);

        long[] result = new long[1];
        if (vkCreateImageView(device.getDevice(), viewInfo, null, result) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image view");
        }
        return result[0];
    }

    /**
     * WARNING ALL DATA IN IMAGE WILL BE LOST! You will also need to recreate views!
     */
    public void resize(MemoryStack stack, boolean isDepth, VkCommandBuffer cmd, int width, int height){
        destroy();
        this.width = width;
        this.height = height;
        super.destroyed = false;
        VulkanImageBuilder builder = new VulkanImageBuilder();
        builder.setFormat(this.imageFormat);
        builder.setHeight(this.height);
        builder.setWidth(this.width);
        builder.setArraySize(this.layerCount);
        builder.setInitialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        builder.setImageMemoryProperties(this.imageMemoryProperties);
        builder.setMipLevels(this.mipLevels);
        builder.setRequiredUsage(this.imageUsage);
        builder.setSamples(this.samples);
        builder.setSharingMode(this.sharingMode);
        builder.setTiling(this.imageTiling);

        int lastShaderStage = this.pipelineStage;
        int lastLayout = this.imageLayout;

        this.image = createImage(stack, builder);
        this.imageMemory = createImageMemory(stack, device, builder.getImageMemoryProperties(), this.image);

        changeLayout(stack,isDepth, lastLayout, accessMask, lastShaderStage, cmd);
    }
    @Override
    public void destroy() {
        imageViews.forEach(VulkanImageView::destroy);
        vkDestroyImage(device.getDevice(), image, null);
        vkFreeMemory(device.getDevice(), imageMemory, null);
        super.destroy();
    }




    public VulkanDevice getDevice() {
        return device;
    }

    public long getImage() {
        return image;
    }


    public int getImageFormat() {
        return imageFormat;
    }

    public long getImageMemory() {
        return imageMemory;
    }

    public int getImageLayout() {
        return imageLayout;
    }

    public int getImageTiling() {
        return imageTiling;
    }

    public int getSharingMode() {
        return sharingMode;
    }

    public int getSamples() {
        return samples;
    }

    public int getAccessMask() {
        return accessMask;
    }

    public int getPipelineStage() {
        return pipelineStage;
    }

    public int getLayerCount() {
        return layerCount;
    }

    public int getMipLevels() {
        return mipLevels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
