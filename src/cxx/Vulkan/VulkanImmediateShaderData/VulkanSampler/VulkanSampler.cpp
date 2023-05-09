#include "VulkanSampler.h"

VulkanSampler::VulkanSampler(VulkanDevice *device, unsigned int binding) : device(device), binding(binding)
{
    createTextureSampler();
}

void VulkanSampler::setSamplerImageView(VkImageView samplerImageView)
{
    VulkanSampler::samplerImageView = samplerImageView;
}

unsigned int VulkanSampler::getBinding() 
{
    return binding;
}

VkDescriptorType VulkanSampler::getDescriptorType() 
{
    return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
}

VkImageView VulkanSampler::getImageView() 
{
    return samplerImageView;
}

VkSampler VulkanSampler::getSampler() 
{
    return sampler;
}

VkBuffer VulkanSampler::getBuffer(unsigned int currentInstance) 
{
    return nullptr;
}

size_t VulkanSampler::getBufferSize() 
{
    return 0;
}

void VulkanSampler::createTextureSampler()
{
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

    if (vkCreateSampler(device->getDevice(), &samplerInfo, nullptr, &sampler) != VK_SUCCESS)
    {
        throw std::runtime_error("failed to create texture sampler!");
    }
}
