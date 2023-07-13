//
// Created by kgaft on 7/12/23.
//

#include "VulkanComputePipeline.h"


VulkanComputePipeline::VulkanComputePipeline(VulkanDevice *device, VulkanShader *shader, ComputePipelineEndConfig* endConfig) : device(device),
                                                                                           shader(shader), configurer(device, endConfig) {
    createPipeline();
}

void VulkanComputePipeline::createPipeline() {
    VkComputePipelineCreateInfo pipelineInfo{};
    pipelineInfo.sType = VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO;
    pipelineInfo.layout = configurer.getPipelineLayout();
    pipelineInfo.stage = shader->getStages()[0];
    if (vkCreateComputePipelines(device->getDevice(), VK_NULL_HANDLE, 1, &pipelineInfo, nullptr, &computePipeline) != VK_SUCCESS) {
        throw std::runtime_error("failed to create compute pipeline!");
    }
    delete shader;
    shader = nullptr;
}

VkPipeline VulkanComputePipeline::getComputePipeline() {
    return computePipeline;
}
