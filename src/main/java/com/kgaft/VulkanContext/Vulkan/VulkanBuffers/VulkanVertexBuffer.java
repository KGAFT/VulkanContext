package com.kgaft.VulkanContext.Vulkan.VulkanBuffers;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.vulkan.VK13.*;

import java.nio.ByteBuffer;

public class VulkanVertexBuffer extends DestroyableObject {
    private long vertexBuffer;
    private long vertexBufferMemory;
    private VulkanDevice device;
    private int verticesAmount;

    public VulkanVertexBuffer(int stepSize, int verticesAmount, VulkanDevice device, ByteBuffer data) {
        this.device = device;
        this.verticesAmount = verticesAmount;
        createVertexBuffer(data, stepSize, verticesAmount);
    }

    @Override
    public void destroy() {
        vkDestroyBuffer(device.getDevice(), vertexBuffer, null);
        vkFreeMemory(device.getDevice(), vertexBufferMemory, null);
        super.destroy();
    }

    public void draw(VkCommandBuffer cmd){
        vkCmdDraw(cmd, verticesAmount, 1, 0,0 );
    }

    public void bind(VkCommandBuffer cmd){
        long[] buffers = new long[]{vertexBuffer};
        long[] offsets = new long[]{0};
        vkCmdBindVertexBuffers(cmd, 0, buffers, offsets);
    }

    private void createVertexBuffer(ByteBuffer vertices, int stepSize, int verticesAmount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int bufferSize = stepSize * verticesAmount;
            long stagingBuffer;
            long stagingBufferMemory;
            long[] buffers = device.createBuffer(bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            stagingBuffer = buffers[0];
            stagingBufferMemory = buffers[1];
            PointerBuffer data = stack.callocPointer(1);
            vkMapMemory(device.getDevice(), stagingBufferMemory, 0, bufferSize, 0, data);
            data.put(vertices);
            vkUnmapMemory(device.getDevice(), stagingBufferMemory);
            buffers = device.createBuffer(
                    bufferSize,
                    VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            device.copyBuffer(stagingBuffer, buffers[0], bufferSize);
            vertexBuffer = buffers[0];
            vertexBufferMemory = buffers[1];
            vkDestroyBuffer(device.getDevice(), stagingBuffer, null);
            vkFreeMemory(device.getDevice(), stagingBufferMemory, null);
        }
    }
}
