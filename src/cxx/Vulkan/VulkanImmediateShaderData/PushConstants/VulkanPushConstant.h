//
// Created by KGAFT on 3/15/2023.
//
#pragma once

#include <vulkan/vulkan.h>

class VulkanPushConstant{
    friend class VulkanPushConstantManager;
private:
    size_t size;
    VkShaderStageFlags shaderStages;
    void *data;
public:
    VulkanPushConstant(size_t size, VkShaderStageFlags shaderStages) : size(size), shaderStages(shaderStages) {

    }
    size_t getSize() {
        return size;
    }

    VkShaderStageFlags getShaderStages() {
        return shaderStages;
    }

    void *getData() {
        return data;
    }

    void setData(void *data) {
        VulkanPushConstant::data = data;
    }
};