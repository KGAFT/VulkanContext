//
// Created by KGAFT on 3/16/2023.
//

#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include "../../VulkanDevice/VulkanDevice.h"
#include "../../VulkanDescriptors/IDescriptorObject.h"

class VulkanUniformBuffer : public IDescriptorObject {
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
    VulkanUniformBuffer(VulkanDevice
                        *device,
                        size_t bufferSize, VkShaderStageFlags
                        targetShaders,
                        unsigned int binding,
                        unsigned int instanceCount
    ) : device(device), size(bufferSize), shaderStages(targetShaders), binding(binding) {
        uniformBuffers.resize(instanceCount);
        uniformBuffersMemory.resize(instanceCount);
        uniformBuffersMapped.resize(instanceCount);

        for (size_t i = 0; i < instanceCount; i++) {
            device->createBuffer(bufferSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                 VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                 uniformBuffers[i], uniformBuffersMemory[i]);
            vkMapMemory(device->getDevice(), uniformBuffersMemory[i], 0, bufferSize, 0, &uniformBuffersMapped[i]);
        }
    }

    ~VulkanUniformBuffer() {
        destroy();
    }

    void destroy() {
        if (!destroyed) {
            for (int i = 0; i < uniformBuffers.size(); ++i) {
                vkDestroyBuffer(device->getDevice(), uniformBuffers[i], nullptr);
                vkFreeMemory(device->getDevice(), uniformBuffersMemory[i], nullptr);
            }
            destroyed = true;
        }

    }

    void write(void *data) {
        for (const auto &item: uniformBuffersMapped) {
            memcpy(item, data, size);
        }
    }

    unsigned int getBinding() override {
        return binding;
    }

    VkDescriptorType getDescriptorType() override {
        return VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
    }

    VkImageView getImageView() override {
        return nullptr;
    }

    VkSampler getSampler() override {
        return nullptr;
    }

    VkBuffer getBuffer(unsigned int currentInstance) override {
        return uniformBuffers[currentInstance];
    }

    size_t getBufferSize() override {
        return size;
    }
};