package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedExtensionException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedLayerException;
import com.kgaft.VulkanContext.MemoryUtils.MemoryStackUtils;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity.DeviceSuitability;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity.DeviceSuitabilityResults;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDeviceBuilder;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkPhysicalDevice;

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
        MemoryStack stack = MemoryStackUtils.acquireStack();
        int[] physicalDeviceCount = new int[1];
        vkEnumeratePhysicalDevices(instance.getInstance(), physicalDeviceCount, null);
        PointerBuffer pb = stack.callocPointer(physicalDeviceCount[0]);
        vkEnumeratePhysicalDevices(instance.getInstance(), physicalDeviceCount, pb);
        VulkanDeviceBuilder deviceBuilder = new VulkanDeviceBuilder();
        deviceBuilder.addExtension(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
        deviceBuilder.addExtension(VK_KHR_RAY_TRACING_PIPELINE_EXTENSION_NAME);
        deviceBuilder.adRequiredQueue(VK_QUEUE_GRAPHICS_BIT);
        deviceBuilder.adRequiredQueue(VK_QUEUE_COMPUTE_BIT);
        DeviceSuitabilityResults suitRes = new DeviceSuitabilityResults();
        while(pb.hasRemaining()){
            VkPhysicalDevice device = new VkPhysicalDevice(pb.get(), instance.getInstance());
            if(DeviceSuitability.isDeviceSuitable(stack, device, deviceBuilder, suitRes)){
                System.out.printf("SuitableDevice");
            }

        }

        System.out.print(instRes[0]==VK_SUCCESS);
    }
}