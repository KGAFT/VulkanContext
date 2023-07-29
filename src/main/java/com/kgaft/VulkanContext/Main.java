package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedExtensionException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedLayerException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDeviceBuilder;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import org.lwjgl.vulkan.VK13;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

import static org.lwjgl.vulkan.KHRRayTracingPipeline.*;


public class Main {
    public static void main(String[] args) throws NotSupportedExtensionException, NotSupportedLayerException, BuilderNotPopulatedException {
        VulkanInstance.getBuilderInstance().addLayer(VulkanInstance.VK_LAYER_KHRONOS_validation)
                .addExtension(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
                .setApplicationInfo("HelloApp", "HElloENgine", VK13.VK_API_VERSION_1_3, VK_MAKE_VERSION(1,0,0), VK_MAKE_VERSION(1,0,0));
        int[] instRes = new int[1];
        VulkanInstance instance = VulkanInstance.createInstance(instRes);

        VulkanDeviceBuilder deviceBuilder = new VulkanDeviceBuilder();
        deviceBuilder.addExtension(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
        deviceBuilder.addExtension(VK_KHR_RAY_TRACING_PIPELINE_EXTENSION_NAME);
        deviceBuilder.addRequiredQueue(VK_QUEUE_GRAPHICS_BIT);
        deviceBuilder.addRequiredQueue(VK_QUEUE_COMPUTE_BIT);


        System.out.print(instRes[0]==VK_SUCCESS);
    }
}