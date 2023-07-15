//
// Created by kgaft on 7/15/23.
//

#include "VulkanStorageBuffer.h"

#include <cstring>

VulkanStorageBuffer::VulkanStorageBuffer(VulkanDevice *device, unsigned int binding, size_t size, VkBufferUsageFlags usageFlags, unsigned int instanceCount) : device(device), size(size), binding(binding), usageFlags(usageFlags) {


    storageBuffers.resize(instanceCount);
    bufferMemories.resize(instanceCount);
    for (int i = 0; i < instanceCount; ++i){
        device->createBuffer(size, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT | usageFlags, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, storageBuffers[i], bufferMemories[i]);
    }


}

unsigned int VulkanStorageBuffer::getBinding() {
    return 0;
}

VkDescriptorType VulkanStorageBuffer::getDescriptorType() {
    return VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
}

VkImageView VulkanStorageBuffer::getImageView() {
    return nullptr;
}

VkSampler VulkanStorageBuffer::getSampler() {
    return nullptr;
}

VkBuffer VulkanStorageBuffer::getBuffer(unsigned int currentInstance) {
    return storageBuffers[currentInstance];
}

size_t VulkanStorageBuffer::getBufferSize() {
    return size;
}

void VulkanStorageBuffer::bind(VkCommandBuffer cmd, unsigned int currentInstance, VkIndexType indexBufferFormat) {
    if(usageFlags & VK_BUFFER_USAGE_VERTEX_BUFFER_BIT){
        VkBuffer buffers[] = {storageBuffers[currentInstance]};
        VkDeviceSize offsets[] = {0};
        vkCmdBindVertexBuffers(cmd, 0, 1, buffers, offsets);

    }
    else if(usageFlags & VK_BUFFER_USAGE_INDEX_BUFFER_BIT){
        vkCmdBindIndexBuffer(cmd, storageBuffers[currentInstance], 0, indexBufferFormat);
    }
    else{
        throw std::runtime_error("Failed to bind buffer for pipeline, there is no usage flags for draw");
    }
}

void VulkanStorageBuffer::storeDataInBuffer(void *data) {
    VkBuffer stagingBuffer;
    VkDeviceMemory stagingBufferMemory;
    device->createBuffer(size, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer, stagingBufferMemory);
    void* bufferData;
    vkMapMemory(device->getDevice(), stagingBufferMemory, 0, size, 0, &bufferData);
    memcpy(bufferData, data, (size_t)size);
    vkUnmapMemory(device->getDevice(), stagingBufferMemory);
    for (int i = 0; i < storageBuffers.size(); ++i){
        device->copyBuffer(stagingBuffer, storageBuffers[i], size);
    }
    vkDestroyBuffer(device->getDevice(), stagingBuffer, nullptr);
    vkFreeMemory(device->getDevice(), stagingBufferMemory, nullptr);
}
