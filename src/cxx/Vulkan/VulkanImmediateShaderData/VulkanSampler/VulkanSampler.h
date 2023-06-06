//
// Created by KGAFT on 3/16/2023.
//
#pragma once

#include <vulkan/vulkan.h>
#include "../../VulkanDevice/VulkanDevice.h"
#include "../../VulkanDescriptors/IDescriptorObject.h"
class VulkanSampler : public IDescriptorObject
{

public:
    VulkanSampler(VulkanDevice *device, unsigned int binding);

private:
    VkSampler sampler;
    VkImageView samplerImageView = nullptr;
    VulkanDevice *device;
    unsigned int binding;
    bool destroyed = false;
public:
    void setSamplerImageView(VkImageView samplerImageView);

    unsigned int getBinding() override;

    VkDescriptorType getDescriptorType() override;

    VkImageView getImageView() override;

    VkSampler getSampler() override;

    VkBuffer getBuffer(unsigned int currentInstance) override;

    size_t getBufferSize() override;

    void destroy();

    ~VulkanSampler();

private:
    void createTextureSampler();
};