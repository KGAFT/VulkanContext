package com.kgaft.VulkanContext.Vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK13.*;

import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;


public class VulkanInstance {
    private VkInstance instance;

    public boolean createInstance(String appName, String engineName, boolean enableLogging, List<String> requiredExtensions){
        try(MemoryStack stack = MemoryStack.stackPush()){
            VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack);
            applicationInfo.sType$Default();
            applicationInfo.pApplicationName(stack.UTF8Safe(appName));
            applicationInfo.applicationVersion(VK_MAKE_VERSION(1,0,0));
            applicationInfo.pEngineName(stack.UTF8Safe(engineName));
            applicationInfo.engineVersion(VK_MAKE_VERSION(1,0,0));
            applicationInfo.apiVersion(VK_API_VERSION_1_3);

            VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc(stack);
            instanceCreateInfo.sType$Default();
            instanceCreateInfo.pApplicationInfo(applicationInfo);
            instanceCreateInfo.ppEnabledExtensionNames(getRequiredExtensions(requiredExtensions, enableLogging, stack));

            VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
            if(enableLogging){
                VulkanLogger.describeLogger(stack, debugCreateInfo, instanceCreateInfo);
                instanceCreateInfo.pNext(debugCreateInfo);
            }
            PointerBuffer instanceResult = stack.callocPointer(1);
            if(vkCreateInstance(instanceCreateInfo, null, instanceResult)!=VK_SUCCESS){
                return false;
            }
            instance = new VkInstance(instanceResult.get(), instanceCreateInfo);
            if(enableLogging){
                return VulkanLogger.initLoggerInstance(instance, debugCreateInfo, stack);
            }
            return true;
        }
    }
    private PointerBuffer getRequiredExtensions(List<String> baseExtensions, boolean enableLogging, MemoryStack stack){
        PointerBuffer result = stack.callocPointer(baseExtensions.size()+(enableLogging?1:0));
        baseExtensions.forEach(extension->{
            result.put(Objects.requireNonNull(stack.UTF8Safe(extension)));
        });
        if(enableLogging){
            result.put(Objects.requireNonNull(stack.UTF8Safe(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)));
        }
        result.rewind();
        return result;
    }

}
