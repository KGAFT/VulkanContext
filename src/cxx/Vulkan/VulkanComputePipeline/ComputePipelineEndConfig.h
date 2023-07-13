//
// Created by kgaft on 13.07.23.
//

#pragma once

#include <vector>

struct StorageBufferInfo{
    unsigned int binding;
};

struct StorageImageBinding{
    unsigned int binding;
};

struct ComputePipelineEndConfig{
    std::vector<StorageBufferInfo> storageBuffers;
    std::vector<StorageImageBinding> storageImages;
};