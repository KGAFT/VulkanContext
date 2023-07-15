//
// Created by kgaft on 7/15/23.
//
#pragma once

#include "Vulkan/VulkanDevice/VulkanDevice.h"
#include "Vulkan/VulkanDescriptors/IDescriptorObject.h"

class VulkanStorageBuffer : public IDescriptorObject{
public:
    VulkanStorageBuffer(VulkanDevice* device, unsigned int binding, size_t size, VkBufferUsageFlags usageFlags, unsigned int instanceCount);
private:
    VulkanDevice* device;
    std::vector<VkBuffer> storageBuffers;
    std::vector<VkDeviceMemory> bufferMemories;
    size_t size;
    unsigned int binding;
    VkBufferUsageFlags usageFlags;
public:
    unsigned int getBinding() override;

    void storeDataInBuffer(void* data);

    VkDescriptorType getDescriptorType() override;

    VkImageView getImageView() override;

    VkSampler getSampler() override;

    VkBuffer getBuffer(unsigned int currentInstance) override;

    size_t getBufferSize() override;

    void bind(VkCommandBuffer cmd, unsigned int currentInstance, VkIndexType indexBufferFormat);

};