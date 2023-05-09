//
// Created by KGAFT on 3/16/2023.
//

#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include "../../VulkanDevice/VulkanDevice.h"
#include "../../VulkanDescriptors/IDescriptorObject.h"
#include <cstring>

class VulkanUniformBuffer : public IDescriptorObject
{
public:
    VulkanUniformBuffer(VulkanDevice
                            *device,
                        size_t bufferSize, VkShaderStageFlags targetShaders,
                        unsigned int binding,
                        unsigned int instanceCount);

private:
    std::vector<VkBuffer> uniformBuffers;
    std::vector<VkDeviceMemory> uniformBuffersMemory;
    std::vector<void *> uniformBuffersMapped;
    VulkanDevice *device;
    size_t size;
    VkShaderStageFlags shaderStages;
    unsigned int binding;
    size_t bufferSize;
    bool destroyed = false;

public:
    void write(void *data);

    unsigned int getBinding() override;

    VkDescriptorType getDescriptorType() override;

    VkImageView getImageView() override;

    VkSampler getSampler() override;

    VkBuffer getBuffer(unsigned int currentInstance) override;

    size_t getBufferSize() override;

    ~VulkanUniformBuffer();

    void destroy();
};