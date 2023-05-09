//
// Created by KGAFT on 3/15/2023.
//
#pragma once

#include <vulkan/vulkan.h>

class VulkanPushConstant
{
    friend class VulkanPushConstantManager;

public:
    VulkanPushConstant(size_t size, VkShaderStageFlags shaderStages);

private:
    size_t size;
    VkShaderStageFlags shaderStages;
    void *data;

public:
    size_t getSize();

    VkShaderStageFlags getShaderStages();

    void *getData();

    void setData(void *data);
};