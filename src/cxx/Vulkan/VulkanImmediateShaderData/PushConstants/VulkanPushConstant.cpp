#include "VulkanPushConstant.h"

VulkanPushConstant::VulkanPushConstant(size_t size, VkShaderStageFlags shaderStages) : size(size), shaderStages(shaderStages)
{
}

size_t VulkanPushConstant::getSize()
{
    return size;
}

VkShaderStageFlags VulkanPushConstant::getShaderStages()
{
    return shaderStages;
}

void *VulkanPushConstant::getData()
{
    return data;
}

void VulkanPushConstant::setData(void *data)
{
    VulkanPushConstant::data = data;
}