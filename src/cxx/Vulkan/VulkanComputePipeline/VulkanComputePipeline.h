//
// Created by kgaft on 7/12/23.
//
#pragma once

#include "../VulkanDevice/VulkanDevice.h"
#include "../VulkanShader/VulkanShader.h"
#include "ComputePipelineConfigurer.h"
class VulkanComputePipeline {
public:
    VulkanComputePipeline(VulkanDevice *device, VulkanShader *shader, ComputePipelineEndConfig* endConfig);

private:
    VkPipeline computePipeline;
    VulkanDevice *device;
    VulkanShader *shader;
    ComputePipelineConfigurer configurer;
private:
    void createPipeline();
public:
    VkPipeline getComputePipeline();
};

