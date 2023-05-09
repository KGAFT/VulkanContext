#include "VulkanDescriptors.h"

VulkanDescriptors::VulkanDescriptors(VulkanDevice *device, PipelineEndConfig *endConfig, VkDescriptorSetLayout layout, unsigned int instanceCount) : layout(layout), instanceCount(instanceCount), endConfig(endConfig)
{
    this->device = device;
    std::vector<VkDescriptorPoolSize> sizes;
    sizes.push_back({VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1000});
    sizes.push_back({VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1000});
    VkDescriptorPoolCreateInfo poolInfo{};
    poolInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
    poolInfo.poolSizeCount = sizes.size();
    poolInfo.pPoolSizes = sizes.data();
    poolInfo.maxSets = 1000;

    if (vkCreateDescriptorPool(device->getDevice(), &poolInfo, nullptr, &descriptorPool) != VK_SUCCESS)
    {
        throw std::runtime_error("failed to create descriptor pool!");
    }
}

VulkanDescriptorSet *VulkanDescriptors::acquireDescriptorSet()
{
    if (!existingDescriptorSets.empty())
    {
        for (auto descriptorSet : existingDescriptorSets)
        {
            if (descriptorSet->attachInstance == nullptr)
            {
                return descriptorSet;
            }
        }
    }
    std::vector<VkDescriptorSetLayout> layouts(instanceCount, layout);
    auto *descriptorSet = new VulkanDescriptorSet(device);

    VkDescriptorSetAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
    allocInfo.descriptorPool = descriptorPool;
    allocInfo.descriptorSetCount = instanceCount;
    allocInfo.pSetLayouts = layouts.data();
    descriptorSet->descriptorSets.resize(instanceCount);
    VkResult result = vkAllocateDescriptorSets(device->getDevice(), &allocInfo, descriptorSet->descriptorSets.data());
    if (result != VK_SUCCESS)
    {
        switch (result)
        {
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
    descriptorSet->initImmediate(endConfig);
    existingDescriptorSets.push_back(descriptorSet);
    return descriptorSet;
}

VulkanDescriptors::~VulkanDescriptors()
{
    vkDestroyDescriptorPool(device->getDevice(), descriptorPool, nullptr);
    for (const auto &item : existingDescriptorSets)
    {
        delete item;
    }
}
