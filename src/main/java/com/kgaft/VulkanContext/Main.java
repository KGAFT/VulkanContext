package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedExtensionException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedLayerException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import com.kgaft.VulkanContext.Vulkan.VulkanBuffer.VulkanBuffer;
import com.kgaft.VulkanContext.Vulkan.VulkanBuffer.VulkanBufferBuilder;

import org.lwjgl.vulkan.VK13;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class Main {
    public static void main(String[] args) throws NotSupportedExtensionException, NotSupportedLayerException, BuilderNotPopulatedException, InterruptedException {
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
    }
}