package com.kgaft.VulkanContext.Vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK13.*;
import java.nio.LongBuffer;
import java.util.Objects;

public class VulkanLogger implements VkDebugUtilsMessengerCallbackEXTI {
    private static VulkanLogger instance = null;
    public static void describeLogger(MemoryStack stack, VkDebugUtilsMessengerCreateInfoEXT createInfo, VkInstanceCreateInfo instanceCreateInfo){
        if(instanceCreateInfo!=null){
            instanceCreateInfo.ppEnabledLayerNames(stack.callocPointer(1));
            Objects.requireNonNull(instanceCreateInfo.ppEnabledLayerNames())
                    .put(Objects.requireNonNull(stack.UTF8Safe("VK_LAYER_KHRONOS_validation")));
            instanceCreateInfo.ppEnabledLayerNames().rewind();
        }
        createInfo.sType$Default();
        createInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT);
        createInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        if(instance==null){
            instance = new VulkanLogger();
        }
        createInfo.pfnUserCallback(instance);
    }

    public static boolean initLoggerInstance(VkInstance vkInstance, VkDebugUtilsMessengerCreateInfoEXT createInfoEXT, MemoryStack stack){
        if(instance==null){
            instance = new VulkanLogger();
        }
        LongBuffer handleResult = stack.mallocLong(1);
        if(instance.createDebugUtilsMessenger(vkInstance, createInfoEXT, handleResult)){
            instance.vkInstance = vkInstance;
            instance.handle = handleResult.get();
            return true;
        }
        return false;
    }

    private long handle;
    private VkInstance vkInstance;

    @Override
    public int invoke(int messageSeverity, int messageType, long pCallbackData, long pUserData) {
        return 0;
    }

    private boolean createDebugUtilsMessenger(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfoEXT, LongBuffer longBuffer){
        long functionHandle = vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT");
        if(functionHandle!=VK_NULL_HANDLE){
            return vkCreateDebugUtilsMessengerEXT(instance, createInfoEXT, null, longBuffer)==VK_SUCCESS;
        }
        return false;
    }
}
