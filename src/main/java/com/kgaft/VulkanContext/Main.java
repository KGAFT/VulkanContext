package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedExtensionException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedLayerException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDeviceBuilder;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

import static org.lwjgl.vulkan.KHRRayTracingPipeline.*;


public class Main {
    public static void main(String[] args) throws NotSupportedExtensionException, NotSupportedLayerException, BuilderNotPopulatedException, InterruptedException {
        VulkanInstance.getBuilderInstance().addLayer(VulkanInstance.VK_LAYER_KHRONOS_validation)
                .addExtension(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
                .setApplicationInfo("HelloApp", "HElloENgine", VK13.VK_API_VERSION_1_3, VK_MAKE_VERSION(1,0,0), VK_MAKE_VERSION(1,0,0));
        int[] instRes = new int[1];
        VulkanInstance instance = VulkanInstance.createInstance(instRes);
        VulkanDevice.getDeviceBuilderInstance().addExtension(VK_KHR_SWAPCHAIN_EXTENSION_NAME).addRequiredQueue(VK_QUEUE_GRAPHICS_BIT).addRequiredQueue(VK_QUEUE_COMPUTE_BIT).addExtension(VK_KHR_RAY_TRACING_PIPELINE_EXTENSION_NAME);
        VulkanDevice.enumerateSupportedDevices(instance).forEach((dev, compatibilities)->{
            System.out.println(compatibilities.getProperties().deviceNameString());
        });
    }
}