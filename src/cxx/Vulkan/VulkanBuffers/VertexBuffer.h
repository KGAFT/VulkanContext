//
// Created by daniil on 27.01.23.
//
#pragma once

#include <cstring>
#include "../VulkanDevice/VulkanDevice.h"

class VertexBuffer {

public:
    VertexBuffer(size_t stepSize, unsigned int verticesAmount, VulkanDevice *device, void *data);
private:
    VkBuffer vertexBuffer;
    VkDeviceMemory vertexBufferMemory;
    VulkanDevice *device;
    unsigned int verticesAmount;

public:

    ~VertexBuffer();

    void destroy();


    void draw(VkCommandBuffer commandBuffer);

    void bind(VkCommandBuffer commandBuffer);

private:
    void createVertexBuffers(void *vertices, size_t stepSize, unsigned int verticesAmount);
};

