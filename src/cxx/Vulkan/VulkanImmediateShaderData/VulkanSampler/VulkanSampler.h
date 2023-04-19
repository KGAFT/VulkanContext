//
// Created by KGAFT on 3/16/2023.
//
#pragma once

#include <vulkan/vulkan.h>
#include "../../VulkanDevice/VulkanDevice.h"
#include "../../VulkanDescriptors/IDescriptorObject.h"
class VulkanSampler : public IDescriptorObject{
private:
    VkSampler sampler;
    VkImageView samplerImageView = nullptr;
    VulkanDevice* device;
    unsigned int binding;
public:
    VulkanSampler(VulkanDevice *device, unsigned int binding) : device(device), binding(binding) {
        createTextureSampler();
    }


    void setSamplerImageView(VkImageView samplerImageView) {
        VulkanSampler::samplerImageView = samplerImageView;
    }

    unsigned int getBinding() override {
        return binding;
    }

    VkDescriptorType getDescriptorType() override {
        return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    }

    VkImageView getImageView() override {
        return samplerImageView;
    }

    VkSampler getSampler() override {
        return sampler;
    }

    VkBuffer getBuffer(unsigned int currentInstance) override {
        return nullptr;
    }

    size_t getBufferSize() override {
        return 0;
    }

private:
    void createTextureSampler() {
        VkPhysicalDeviceProperties properties{};
        vkGetPhysicalDeviceProperties(device->getDeviceToCreate(), &properties);

        VkSamplerCreateInfo samplerInfo{};
        samplerInfo.sType = VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
        samplerInfo.magFilter = VK_FILTER_LINEAR;
        samplerInfo.minFilter = VK_FILTER_LINEAR;
        samplerInfo.addressModeU = VK_SAMPLER_ADDRESS_MODE_REPEAT;
        samplerInfo.addressModeV = VK_SAMPLER_ADDRESS_MODE_REPEAT;
        samplerInfo.addressModeW = VK_SAMPLER_ADDRESS_MODE_REPEAT;
        samplerInfo.anisotropyEnable = VK_TRUE;
        samplerInfo.maxAnisotropy = properties.limits.maxSamplerAnisotropy;
        samplerInfo.borderColor = VK_BORDER_COLOR_INT_OPAQUE_BLACK;
        samplerInfo.unnormalizedCoordinates = VK_FALSE;
        samplerInfo.compareEnable = VK_FALSE;
        samplerInfo.compareOp = VK_COMPARE_OP_ALWAYS;
        samplerInfo.mipmapMode = VK_SAMPLER_MIPMAP_MODE_LINEAR;

        if (vkCreateSampler(device->getDevice(), &samplerInfo, nullptr, &sampler) != VK_SUCCESS) {
            throw std::runtime_error("failed to create texture sampler!");
        }
    }
};