//
// Created by kgaft on 7/26/23.
//
#include <iostream>

#include "cxx/VulkanContext/Vulkan/VulkanInstance.h"

int main(){
    VulkanInstance::getBuilderInstance()
    ->addExtension(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
    ->addExtension(VK_KHR_RAY_TRACING_PIPELINE_EXTENSION_NAME)
    ->addLayer(VK_LAYER_KHRONOS_validation)
    ->setApplicationInfo("HelloApp", "HelloEngine", VK_API_VERSION_1_3, VK_MAKE_VERSION(1,0,0), VK_MAKE_VERSION(1,0,0));
    VulkanInstance* instance;
    VulkanInstance::createInstance(&instance);
    return 0;
}
