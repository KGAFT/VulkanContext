//
// Created by KGAFT on 3/15/2023.
//

#pragma once

#include <vulkan/vulkan.h>
#include "../VulkanDevice/VulkanDevice.h"
#include "IDescriptorObject.h"

class VulkanDescriptors {
private:
    VkDescriptorPool descriptorPool;
    VulkanDevice *device;
    std::vector<VkDescriptorSet> descriptorSets;
public:
    VulkanDescriptors(VulkanDevice *device, VkDescriptorSetLayout layout, unsigned int instanceCount) {
        this->device = device;
        std::vector<VkDescriptorPoolSize> sizes;
        for (int i = 0; i < 100; ++i) {
            VkDescriptorPoolSize poolSize{};
            if (i % 2 == 0) {
                poolSize.type = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
            } else {
                poolSize.type = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
            }
            poolSize.descriptorCount = instanceCount;
            sizes.push_back(poolSize);
        }
        VkDescriptorPoolCreateInfo poolInfo{};
        poolInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
        poolInfo.poolSizeCount = sizes.size();
        poolInfo.pPoolSizes = sizes.data();
        poolInfo.maxSets = instanceCount;

        if (vkCreateDescriptorPool(device->getDevice(), &poolInfo, nullptr, &descriptorPool) != VK_SUCCESS) {
            throw std::runtime_error("failed to create descriptor pool!");
        }
        createDescriptorSets(layout, instanceCount);
    }

    void writeDescriptorObject(IDescriptorObject *descriptorObject, int currentDescriptor) {
        VkWriteDescriptorSet write{};
        std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo> info = getChildOfObject(descriptorObject,
                                                                                         currentDescriptor);
        write.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
        write.dstSet = descriptorSets[currentDescriptor];
        write.dstBinding = descriptorObject->getBinding();
        write.dstArrayElement = 0;
        write.descriptorType = descriptorObject->getDescriptorType();
        write.descriptorCount = 1;
        if (descriptorObject->getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
            write.pBufferInfo = &info.first;
        } else {
            write.pImageInfo = &info.second;
        }
        vkUpdateDescriptorSets(device->getDevice(), 1, &write, 0, nullptr);
    }

    void writeDescriptorObjects(IDescriptorObject **descriptorObjects, unsigned int objectCount,
                                unsigned int currentDescriptor) {
        std::vector<VkWriteDescriptorSet> writes;
        std::vector<std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo> *> infos;
        for (int i = 0; i < objectCount; ++i) {
            std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo> *info = new std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo>(
                    getChildOfObject(descriptorObjects[i], currentDescriptor));
            VkWriteDescriptorSet write{};
            write.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
            write.dstSet = descriptorSets[currentDescriptor];
            write.dstBinding = descriptorObjects[i]->getBinding();
            write.dstArrayElement = 0;
            write.descriptorType = descriptorObjects[i]->getDescriptorType();
            write.descriptorCount = 1;
            if (descriptorObjects[i]->getDescriptorType() == VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
                write.pBufferInfo = &info->first;
            } else {
                write.pImageInfo = &info->second;
            }
            writes.push_back(write);
            infos.push_back(info);
        }
        vkUpdateDescriptorSets(device->getDevice(), writes.size(), writes.data(), 0, nullptr);
        for (const auto &item: infos) {
            delete item;
        }
    }

    void bind(unsigned int instanceNumber, VkCommandBuffer commandBuffer, VkPipelineLayout layout) {
        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, layout, 0, 1,
                                &descriptorSets[instanceNumber], 0, nullptr);
    }

private:
    std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo>
    getChildOfObject(IDescriptorObject *object, unsigned int currentInstance) {
        std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo> result;
        if (object->getDescriptorType() == VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER) {
            result.second = {};
            result.second.imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
            result.second.imageView = object->getImageView();
            result.second.sampler = object->getSampler();
        } else {
            result.first = {};
            result.first.buffer = object->getBuffer(currentInstance);
            result.first.offset = 0;
            result.first.range = object->getBufferSize();
        }
        return result;
    }

    void createDescriptorSets(VkDescriptorSetLayout layout, unsigned int instanceCount) {
        std::vector<VkDescriptorSetLayout> layouts(instanceCount, layout);
        VkDescriptorSetAllocateInfo allocInfo{};
        allocInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
        allocInfo.descriptorPool = descriptorPool;
        allocInfo.descriptorSetCount = instanceCount;
        allocInfo.pSetLayouts = layouts.data();
        descriptorSets.resize(instanceCount);
        VkResult result = vkAllocateDescriptorSets(device->getDevice(), &allocInfo, descriptorSets.data());
        if (result != VK_SUCCESS) {
            switch (result) {
                case VK_ERROR_OUT_OF_HOST_MEMORY:
                    std::cerr << "OHM" << std::endl;
                    break;
                case VK_ERROR_OUT_OF_DEVICE_MEMORY:
                    std::cerr << "ODM" << std::endl;
                    break;
                case VK_ERROR_FRAGMENTED_POOL:
                    std::cerr << "FM" << std::endl;
                    break;
                case VK_ERROR_OUT_OF_POOL_MEMORY:
                    std::cerr << "OPM" << std::endl;
                    break;
                default:
                    break;
            }
            throw std::runtime_error("failed to allocate descriptor sets!");
        }

    }
};

