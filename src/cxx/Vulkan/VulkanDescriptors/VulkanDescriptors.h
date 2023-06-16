//
// Created by KGAFT on 3/15/2023.
//

#pragma once

#include <vulkan/vulkan.h>
#include "../VulkanDevice/VulkanDevice.h"
#include "VulkanDescritptorSet.h"
#include "IDescriptorObject.h"

class VulkanDescriptors
{
public:
    VulkanDescriptors(VulkanDevice *device, PipelineEndConfig *endConfig, VkDescriptorSetLayout layout, unsigned int instanceCount);

private:
    VkDescriptorPool descriptorPool;
    VulkanDevice *device;
    VkDescriptorSetLayout layout;
    unsigned int instanceCount;
    std::vector<VulkanDescriptorSet *> existingDescriptorSets;
    PipelineEndConfig endConfig;

public:
    

    VulkanDescriptorSet *acquireDescriptorSet();

    ~VulkanDescriptors();
};
