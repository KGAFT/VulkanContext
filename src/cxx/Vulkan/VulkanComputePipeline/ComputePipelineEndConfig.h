//
// Created by kgaft on 13.07.23.
//

#pragma once

#include <vector>

struct StorageBufferInfo{
    unsigned int binding;
    size_t size;
    VkBufferUsageFlags usageFlags;
};

struct UniformBufferInfo{
    unsigned int binding;
    size_t size;
};

struct ComputePipelineEndConfig{
    std::vector<StorageBufferInfo> storageBuffers;
    std::vector<UniformBufferInfo> uniformBuffers;
};