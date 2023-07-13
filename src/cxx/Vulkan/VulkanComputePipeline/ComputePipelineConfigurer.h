//
// Created by kgaft on 7/12/23.
//
#pragma once

#include "Vulkan/VulkanDevice/VulkanDevice.h"
#include "ComputePipelineEndConfig.h"

class ComputePipelineConfigurer {
public:
    ComputePipelineConfigurer(VulkanDevice* device, ComputePipelineEndConfig* endConfig);
private:
    VulkanDevice* device;
    VkDescriptorSetLayout descriptorSetLayout;
    VkPipelineLayout pipelineLayout;
    ComputePipelineEndConfig endConfig;
public:
    VkDescriptorSetLayout getDescriptorSetLayout();
    VkPipelineLayout getPipelineLayout();
private:
    void createDescriptorSetLayout();
    void createPipelineLayout();

};


