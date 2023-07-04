//
// Created by daniil on 28.01.23.
//
#pragma once

#include <vulkan/vulkan.h>
#include <cstring>

#include "../VulkanDevice/VulkanDevice.h"

class IndexBuffer
{
public:
    IndexBuffer(VulkanDevice *device, unsigned int *indices, unsigned int indicesAmount);

private:
    VkBuffer indexBuffer;
    VkDeviceMemory indexBufferMemory;
    VulkanDevice *device;
    unsigned int indicesCount;
    bool destroyed;
public:
    ~IndexBuffer();

    void destroy();

    void bind(VkCommandBuffer commandBuffer);

    void draw(VkCommandBuffer commandBuffer);

    void recreate(unsigned int *indices, unsigned int indicesAmount);
private:
    void createIndexBuffer(unsigned int *indices, unsigned int amount);
};
