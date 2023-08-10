package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Exceptions.BufferException;
import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedExtensionException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedLayerException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanImage.ImageTarget;
import com.kgaft.VulkanContext.Vulkan.VulkanImage.VulkanImage;
import com.kgaft.VulkanContext.Vulkan.VulkanImage.VulkanImageBuilder;
import com.kgaft.VulkanContext.Vulkan.VulkanImage.VulkanImageView;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import com.kgaft.VulkanContext.Vulkan.VulkanBuffer.VulkanBuffer;
import com.kgaft.VulkanContext.Vulkan.VulkanBuffer.VulkanBufferBuilder;

import com.kgaft.VulkanContext.Vulkan.VulkanRenderPass.VulkanRenderPass;
import com.kgaft.VulkanContext.Vulkan.VulkanRenderPass.VulkanRenderPassBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws NotSupportedExtensionException, NotSupportedLayerException, BuilderNotPopulatedException, InterruptedException, BufferException {
        VulkanInstance.getBuilderInstance().addLayer(VulkanInstance.VK_LAYER_KHRONOS_validation)
                .addExtension(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
                .setApplicationInfo("HelloApp", "HElloENgine", VK13.VK_API_VERSION_1_3, VK_MAKE_VERSION(1,0,0), VK_MAKE_VERSION(1,0,0));
        int[] instRes = new int[1];
        VulkanInstance instance = VulkanInstance.createInstance(instRes);
        VulkanDevice.getDeviceBuilderInstance().addRequiredQueue(VK_QUEUE_GRAPHICS_BIT).addRequiredQueue(VK_QUEUE_COMPUTE_BIT);
        VulkanDevice device = VulkanDevice.buildDevice(instance, VulkanDevice.enumerateSupportedDevices(instance).get(0));
        VulkanBufferBuilder bufferBuilder = new VulkanBufferBuilder();
        bufferBuilder.setCreateMapped(true);
        bufferBuilder.setMapFlags(0);
        bufferBuilder.setRequiredProperties(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT|VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        bufferBuilder.setRequiredSharingMode(VK_SHARING_MODE_EXCLUSIVE);
        bufferBuilder.setRequiredUsage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT|VK_BUFFER_USAGE_TRANSFER_DST_BIT|VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
        bufferBuilder.setRequiredSize("PTNH".getBytes(StandardCharsets.UTF_8).length*5);
        VulkanBuffer buffer = new VulkanBuffer(device, bufferBuilder);
        ByteBuffer data = ByteBuffer.allocate("PTNH".getBytes(StandardCharsets.UTF_8).length);
        data.put("PRIV".getBytes(StandardCharsets.UTF_8));
        data.rewind();
        buffer.writeData(data, "PTNH".getBytes(StandardCharsets.UTF_8).length, 0);
        vkDeviceWaitIdle(device.getDevice());
        data.clear();
        data.rewind();
        buffer.getData(data, "PTNH".getBytes(StandardCharsets.UTF_8).length, (long)"PTNH".getBytes(StandardCharsets.UTF_8).length, 0);
        data.rewind();
        byte[] out = new byte["PTNH".getBytes(StandardCharsets.UTF_8).length];
        data.get(out);
        System.out.println(new String(out));


        VulkanImageBuilder imageBuilder = new VulkanImageBuilder();
        imageBuilder.setArraySize(6);
        imageBuilder.setFormat(VK_FORMAT_R32G32B32A32_SFLOAT);
        imageBuilder.setHeight(800);
        imageBuilder.setWidth(800);
        imageBuilder.setImageMemoryProperties(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        imageBuilder.setInitialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        imageBuilder.setMipLevels(4);
        imageBuilder.setSamples(1);
        imageBuilder.setSharingMode(VK_SHARING_MODE_EXCLUSIVE);
        imageBuilder.setTiling(VK_IMAGE_TILING_OPTIMAL);
        imageBuilder.setRequiredUsage(VK_IMAGE_USAGE_TRANSFER_DST_BIT|VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
        VulkanImage image = new VulkanImage(device, imageBuilder);
        ImageTarget imageTarget = new ImageTarget();
        imageTarget.setLayersAmount(6);
        imageTarget.setMipLevel(0);
        imageTarget.setStartLayerIndex(0);
        imageTarget.setMipLevelCount(1);
        image.changeLayout(MemoryStack.stackPush(), false, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_USAGE_SAMPLED_BIT, VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT, device.getQueueByType(VK_QUEUE_GRAPHICS_BIT));

        VulkanImageView cubeView = image.acquireImageView(MemoryStack.stackPush(), false, VK_IMAGE_VIEW_TYPE_CUBE, imageTarget);
        imageTarget.setStartLayerIndex(0);
        imageTarget.setLayersAmount(1);
        VulkanImageView baseView = image.acquireImageView(MemoryStack.stackPush(),false, VK_IMAGE_VIEW_TYPE_2D, imageTarget);

        VulkanRenderPassBuilder renderPassBuilder = new VulkanRenderPassBuilder();

        imageBuilder.setArraySize(1);
        imageBuilder.setFormat(VulkanImage.findDepthFormat(device));
        imageBuilder.setMipLevels(1);
        imageBuilder.setArraySize(1);
        imageBuilder.setRequiredUsage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
        VulkanImage depthImage = new VulkanImage(device, imageBuilder);
        depthImage.changeLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, true,VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT|VK_IMAGE_USAGE_SAMPLED_BIT, VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT, device.getQueueByType(VK_QUEUE_GRAPHICS_BIT));
        List<VulkanImage> targets = new ArrayList<>();
        List<VulkanImage> depthAttachment = new ArrayList<>();
        depthAttachment.add(depthImage);
        targets.add(image);
        renderPassBuilder.setColorAttachments(targets);
        renderPassBuilder.setImagePerStepAmount(1);
        renderPassBuilder.setDepthAttachments(depthAttachment);
        renderPassBuilder.setAutoGenerateDepthImages(false);
        VulkanRenderPass renderPass = new VulkanRenderPass(device, renderPassBuilder);


        }
}