//
// Created by kgaft on 7/12/23.
//
#pragma once

#include "../VulkanDevice/VulkanDevice.h"
#include "../VulkanShader/VulkanShader.h"

class VulkanComputePipeline {
private:
    VkPipeline computePipeline;
    VulkanDevice *device;
    VulkanShader *shader;
};

