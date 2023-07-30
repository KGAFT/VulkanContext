package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanQueue {

    public static final int PRESENT_QUEUE = -1233841221;
    private int queueType;
    private VkQueue base;
    private VkDevice device;
    private long commandPool;
    private int queueIndex;

    protected VulkanQueue(VkDevice device, int queueIndex, int queueType, VkQueue base, MemoryStack stack) {
        this.queueType = queueType;
        this.base = base;
        this.device = device;
        this.queueIndex = queueIndex;

        VkCommandPoolCreateInfo createInfo = VkCommandPoolCreateInfo.calloc(stack);
        createInfo.sType$Default();
        createInfo.queueFamilyIndex(queueIndex);
        createInfo.flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT | VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        long[] result = new long[1];
        vkCreateCommandPool(device, createInfo, null, result);
        this.commandPool = result[0];
    }

    public int getQueueType() {
        return queueType;
    }

    public VkQueue getBase() {
        return base;
    }
    

    public int getQueueIndex() {
      return queueIndex;
    }

    public VkCommandBuffer beginSingleTimeCommands(MemoryStack stack) {

        VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
        allocInfo.sType$Default();
        allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
        allocInfo.commandPool(commandPool);
        allocInfo.commandBufferCount(1);

        PointerBuffer tempRes = stack.callocPointer(1);
        vkAllocateCommandBuffers(device, allocInfo, tempRes);
        VkCommandBuffer result = new VkCommandBuffer(tempRes.get(), device);

        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
        beginInfo.sType$Default();
        beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
        vkBeginCommandBuffer(result, beginInfo);
        return result;

    }

    public void endSingleTimeCommands(VkCommandBuffer cmd, MemoryStack stack) {
        vkEndCommandBuffer(cmd);
        VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack);
        submitInfo.sType$Default();
        PointerBuffer pBuffer = stack.callocPointer(1);
        pBuffer.put(cmd.address());
        pBuffer.rewind();
        submitInfo.pCommandBuffers(pBuffer);
        vkQueueSubmit(base, submitInfo, VK_NULL_HANDLE);
        vkQueueWaitIdle(base);
        vkFreeCommandBuffers(device, commandPool, pBuffer);
    }

}
