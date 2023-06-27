package com.kgaft.VulkanContext.Vulkan.VulkanBuffers;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;

public class VulkanIndexBuffer extends DestroyableObject {

    private long indexBuffer;
    private long indexBufferMemory;
    private VulkanDevice device;
    private int indicesAmount;

    public VulkanIndexBuffer(int amount, VulkanDevice device, ByteBuffer data) {
        this.device = device;
        this.indicesAmount = amount;
        createIndexBuffer(data, amount);
    }

    @Override
    public void destroy() {
        vkDestroyBuffer(device.getDevice(), indexBuffer, null);
        vkFreeMemory(device.getDevice(), indexBufferMemory, null);
        super.destroy();
    }

    public void draw(VkCommandBuffer cmd){
        vkCmdDrawIndexed(cmd, indicesAmount, 1, 0, 0, 0);
    }

    public void bind(VkCommandBuffer cmd){
        vkCmdBindIndexBuffer(cmd, indexBuffer, 0, VK_INDEX_TYPE_UINT32);
    }

    private void createIndexBuffer(ByteBuffer indices, int amount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int bufferSize = Integer.BYTES*amount;
            long stagingBuffer;
            long stagingBufferMemory;
            long[] buffers = device.createBuffer(bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            stagingBuffer = buffers[0];
            stagingBufferMemory = buffers[1];
            PointerBuffer data = stack.callocPointer(1);
            vkMapMemory(device.getDevice(), stagingBufferMemory, 0, bufferSize, 0, data);
            device.memcpy(data.getByteBuffer(0, bufferSize), indices, bufferSize);
            vkUnmapMemory(device.getDevice(), stagingBufferMemory);
            buffers = device.createBuffer(
                    bufferSize,
                    VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            device.copyBuffer(stagingBuffer, buffers[0], bufferSize);
            indexBuffer = buffers[0];
            indexBuffer = buffers[1];
            vkDestroyBuffer(device.getDevice(), stagingBuffer, null);
            vkFreeMemory(device.getDevice(), stagingBufferMemory, null);
        }
    }

}
