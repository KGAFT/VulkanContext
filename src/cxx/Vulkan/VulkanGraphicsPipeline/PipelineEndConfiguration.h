#pragma once

#include <vulkan/vulkan.h>
#include <vector>

struct VertexInput{
    unsigned int location;
    unsigned int coordinatesAmount;
    size_t typeSize;
    VkFormat format;
};

struct PushConstantInfo{
    VkShaderStageFlags shaderStages;
    size_t size;
};

struct UniformBufferInfo{
    int binding;
    size_t size;
    VkShaderStageFlags shaderStages;
};

struct SamplerInfo{
    int binding;
    VkShaderStageFlags shaderStages;
};

struct PipelineEndConfig{
    std::vector<SamplerInfo> samplers;
    std::vector<UniformBufferInfo> uniformBuffers;
    std::vector<VertexInput> vertexInputs;
    std::vector<PushConstantInfo> pushConstantInfos;
};