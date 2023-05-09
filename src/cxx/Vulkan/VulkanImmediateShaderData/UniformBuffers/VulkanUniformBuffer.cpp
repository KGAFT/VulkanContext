#include "VulkanUniformBuffer.h"

VulkanUniformBuffer::VulkanUniformBuffer(VulkanDevice
                                             *device,
                                         size_t bufferSize, VkShaderStageFlags targetShaders,
                                         unsigned int binding,
                                         unsigned int instanceCount) : device(device), size(bufferSize), shaderStages(targetShaders), binding(binding)
{
    uniformBuffers.resize(instanceCount);
    uniformBuffersMemory.resize(instanceCount);
    uniformBuffersMapped.resize(instanceCount);

    for (size_t i = 0; i < instanceCount; i++)
    {
        device->createBuffer(bufferSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                             VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                             uniformBuffers[i], uniformBuffersMemory[i]);
        vkMapMemory(device->getDevice(), uniformBuffersMemory[i], 0, bufferSize, 0, &uniformBuffersMapped[i]);
    }
}

VulkanUniformBuffer::~VulkanUniformBuffer()
{
    destroy();
}

void VulkanUniformBuffer::destroy()
{
    if (!destroyed)
    {
        for (int i = 0; i < uniformBuffers.size(); ++i)
        {
            vkDestroyBuffer(device->getDevice(), uniformBuffers[i], nullptr);
            vkFreeMemory(device->getDevice(), uniformBuffersMemory[i], nullptr);
        }
        destroyed = true;
    }
}

void VulkanUniformBuffer::write(void *data)
{
    for (const auto &item : uniformBuffersMapped)
    {
        memcpy(item, data, size);
    }
}

unsigned int VulkanUniformBuffer::getBinding() 
{
    return binding;
}

VkDescriptorType VulkanUniformBuffer::getDescriptorType() 
{
    return VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
}

VkImageView VulkanUniformBuffer::getImageView() 
{
    return nullptr;
}

VkSampler VulkanUniformBuffer::getSampler() 
{
    return nullptr;
}

VkBuffer VulkanUniformBuffer::getBuffer(unsigned int currentInstance) 
{
    return uniformBuffers[currentInstance];
}

size_t VulkanUniformBuffer::getBufferSize() 
{
    return size;
}

