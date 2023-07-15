//
// Created by kgaft on 7/12/23.
//

#include "ComputePipelineConfigurer.h"

ComputePipelineConfigurer::ComputePipelineConfigurer(VulkanDevice* device, ComputePipelineEndConfig *endConfig) {
    this->device = device;
    this->endConfig = *endConfig;
    createDescriptorSetLayout();
    createPipelineLayout();
}

void ComputePipelineConfigurer::createDescriptorSetLayout() {
    std::vector<VkDescriptorSetLayoutBinding> bindings;
    for (const auto &item: endConfig.storageBuffers){
        bindings.push_back({item.binding, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 1, VK_SHADER_STAGE_COMPUTE_BIT, nullptr});
    }
    for (const auto &item: endConfig.uniformBuffers){
        bindings.push_back({item.binding, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK_SHADER_STAGE_COMPUTE_BIT, nullptr});
    }
    VkDescriptorSetLayoutCreateInfo layoutInfo{};
    layoutInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
    layoutInfo.bindingCount = bindings.size();
    layoutInfo.pBindings = bindings.data();
    if(vkCreateDescriptorSetLayout(device->getDevice(), &layoutInfo, nullptr, &descriptorSetLayout)!=VK_SUCCESS){
        throw std::runtime_error("failed to create descriptor set layout");
    }

}

void ComputePipelineConfigurer::createPipelineLayout() {
    VkPipelineLayoutCreateInfo pipelineLayoutInfo{};
    pipelineLayoutInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
    pipelineLayoutInfo.setLayoutCount = 1;
    pipelineLayoutInfo.pSetLayouts = &descriptorSetLayout;
    if (vkCreatePipelineLayout(device->getDevice(), &pipelineLayoutInfo, nullptr, &pipelineLayout) != VK_SUCCESS) {
        throw std::runtime_error("failed to create compute pipeline layout!");
    }
}

VkDescriptorSetLayout ComputePipelineConfigurer::getDescriptorSetLayout() {
    return descriptorSetLayout;
}

VkPipelineLayout ComputePipelineConfigurer::getPipelineLayout() {
    return pipelineLayout;
}
