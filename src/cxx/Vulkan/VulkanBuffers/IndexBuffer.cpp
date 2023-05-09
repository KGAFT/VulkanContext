#include "IndexBuffer.h"

IndexBuffer::IndexBuffer(VulkanDevice *device, unsigned int *indices, unsigned int indicesAmount) : device(device)
{
    createIndexBuffer(indices, indicesAmount);
}

IndexBuffer::~IndexBuffer()
{
    destroy();
}

void IndexBuffer::destroy()
{
    vkDestroyBuffer(device->getDevice(), indexBuffer, nullptr);
    vkFreeMemory(device->getDevice(), indexBufferMemory, nullptr);
}

void IndexBuffer::bind(VkCommandBuffer commandBuffer)
{
    vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT32);
}

void IndexBuffer::draw(VkCommandBuffer commandBuffer)
{
    vkCmdDrawIndexed(commandBuffer, indicesCount, 1, 0, 0, 0);
}

void IndexBuffer::createIndexBuffer(unsigned int *indices, unsigned int amount)
{
    indicesCount = amount;
    VkDeviceSize bufferSize = sizeof(unsigned int) * amount;
    VkBuffer stagingBuffer;
    VkDeviceMemory stagingBufferMemory;
    device->createBuffer(
        bufferSize,
        VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
        stagingBuffer,
        stagingBufferMemory);

    void *data;

    vkMapMemory(device->getDevice(), stagingBufferMemory, 0, bufferSize, 0, &data);
    memcpy(data, indices, static_cast<size_t>(bufferSize));
    vkUnmapMemory(device->getDevice(), stagingBufferMemory);

    device->createBuffer(
        bufferSize,
        VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
        VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
        indexBuffer,
        indexBufferMemory);

    device->copyBuffer(stagingBuffer, indexBuffer, bufferSize);

    vkDestroyBuffer(device->getDevice(), stagingBuffer, nullptr);
    vkFreeMemory(device->getDevice(), stagingBufferMemory, nullptr);
}
