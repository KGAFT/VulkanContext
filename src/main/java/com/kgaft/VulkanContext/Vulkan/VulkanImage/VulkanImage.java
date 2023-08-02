package com.kgaft.VulkanContext.Vulkan.VulkanImage;

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

import javax.management.RuntimeErrorException;

public class VulkanImage {
    private VulkanDevice device;
    private long image;
    private long imageMemory;
    private long imageView;
    private int imageLayout;
    private int imageTiling;
    private int imageFormat;
    private int sharingMode;
    private int samples;
    private int accessMask = 0;
    private int shaderStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
    private int layerCount;
    private List<VulkanImageView> imageViews = new ArrayList<>();

    public VulkanImage(VulkanDevice device, VulkanImageBuilder builder) throws BuilderNotPopulatedException {
        this.device = device;
        builder.checkBuilder();
        this.imageLayout = builder.getInitialLayout();
        this.imageTiling = builder.getTiling();
        this.sharingMode = builder.getSharingMode();
        this.samples = builder.getSamples();
        this.imageFormat = builder.getFormat();
        this.layerCount = builder.getArraySize();
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.image = createImage(stack, builder);
            this.imageMemory = createImageMemory(stack, device, builder.getImageMemoryProperties(), image);
            
        }

    }

    public VulkanDevice getDevice() {
        return device;
    }

    public long getImage() {
        return image;
    }

    public long getImageView() {
        return imageView;
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

    public void changeLayout(int targetLayout, int targetAccessMask, int targetShaderStage, VulkanQueue queue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            changeLayout(stack, targetLayout, targetAccessMask, targetShaderStage, queue);
        }
    }

    public void changeLayout(MemoryStack stack, int targetLayout, int targetAccessMask, int targetShaderStage,
            VulkanQueue queue) {
        VkCommandBuffer cmd = queue.beginSingleTimeCommands(stack);
        changeLayout(stack, targetLayout, targetAccessMask, targetShaderStage, cmd);
        queue.endSingleTimeCommands(cmd, stack);
    }

    public void changeLayout(MemoryStack stack, int targetLayout, int targetAccessMask, int targetShaderStage,
            VkCommandBuffer cmd) {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.oldLayout(this.imageLayout);
        barrier.newLayout(targetLayout);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.image(image);
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(layerCount);

        barrier.srcAccessMask(this.accessMask);
        barrier.dstAccessMask(targetAccessMask);

        vkCmdPipelineBarrier(cmd,
                this.shaderStage, targetShaderStage,
                0,
                null,
                null,
                barrier);
        this.accessMask = targetAccessMask;
        this.imageLayout = targetLayout;
        this.shaderStage = targetShaderStage;

    }

    public int getAccessMask() {
        return accessMask;
    }

    public int getShaderStage() {
        return shaderStage;
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
        createInfo.flags(builder.getArraySize()>=6?VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT:0);
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

    private long createImageView(MemoryStack stack, long image, int format, int layerCount, int baseArrayLayer, int type) {

        VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
        viewInfo.sType$Default();
        viewInfo.image(image);
        viewInfo.viewType(type);
        viewInfo.format(format);
        viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        viewInfo.subresourceRange().baseMipLevel(0);
        viewInfo.subresourceRange().levelCount(1);
        viewInfo.subresourceRange().baseArrayLayer(0);
        viewInfo.subresourceRange().layerCount(layerCount);

        long[] result = new long[1];
        if (vkCreateImageView(device.getDevice(), viewInfo, null, result) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image view");
        }
        return result[0];
    }

    public VulkanImageView acquireImageView(MemoryStack stack, int type, int arrayIndex, int layerCount){
        if(arrayIndex>=this.layerCount || arrayIndex<0){
            throw new RuntimeException("Failed to acquired image with not existing index: "+arrayIndex);
        }
        if(type==VK_IMAGE_VIEW_TYPE_2D){
            for(VulkanImageView view : imageViews){
                if(view.getType()==VK_IMAGE_VIEW_TYPE_2D && view.getArrayLayerIndex()==arrayIndex){
                    return view;
                }
            }
            long imageViewHandle = createImageView(stack, this.image,  this.imageFormat, 1, arrayIndex, type);
            VulkanImageView imageView = new VulkanImageView(type, arrayIndex, 1, imageViewHandle);
            imageViews.add(imageView);
            return imageView;
        }
        else if(type==VK_IMAGE_VIEW_TYPE_2D_ARRAY || type==VK_IMAGE_VIEW_TYPE_CUBE){
            if(layerCount>this.layerCount || layerCount<1 || arrayIndex>=this.layerCount || arrayIndex<0){
                throw new RuntimeException("Failed to create multiple image view, because layerCount more than image layers");
            }
            for(VulkanImageView view : imageViews){
                if(view.getType()==type && view.getArrayLayerIndex()==arrayIndex && view.getLayerCount()==layerCount){
                    return view;
                }
            }
            long viewHandle = createImageView(stack, image, this.imageFormat, layerCount, arrayIndex, type);
            VulkanImageView view = new VulkanImageView(type, arrayIndex, layerCount, viewHandle);
            this.imageViews.add(view);
            return view;
        }
        else{
            throw new RuntimeException("Unsupported type");
        }
    }
}
