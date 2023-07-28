package com.kgaft.VulkanContext.Vulkan.VulkanLogger;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK13.*;

import java.io.PrintStream;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Objects;

public class VulkanLogger implements VkDebugUtilsMessengerCallbackEXTI {
    public void describeLogger(MemoryStack stack, VkDebugUtilsMessengerCreateInfoEXT createInfo) {

        createInfo.sType$Default();
        createInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT);
        createInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        createInfo.pfnUserCallback(this);
    }

    public void initLoggerInstance(VkInstance vkInstance, VkDebugUtilsMessengerCreateInfoEXT createInfoEXT, MemoryStack stack) {
        LongBuffer handleResult = stack.mallocLong(1);
        if (createDebugUtilsMessenger(vkInstance, createInfoEXT, handleResult)) {
            this.vkInstance = vkInstance;
            this.handle = handleResult.get();
        }
    }

    private long handle;
    private VkInstance vkInstance;
    private ArrayList<IVulkanLoggerCallback> callbacks = new ArrayList<>();
    
    public VulkanLogger(){

    }

    @Override
    public int invoke(int messageSeverity, int messageType, long pCallbackData, long pUserData) {
        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        if (callbacks.size() == 0) {

            PrintStream outputStream = messageSeverity==VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT?System.err:System.out;
            outputStream.println("Vulkan: "+callbackData.pMessageString());
        }
        else{
            StringBuilder textSeverity = new StringBuilder();
            StringBuilder textType = new StringBuilder();
            callbacks.forEach(callback -> {
                switch (callback.getCallbackType()){
                    case RAW_VULKAN_DEFS:
                        callback.messageRaw(messageSeverity, messageType, pCallbackData, pUserData);
                        break;
                    case TRANSLATED_VULKAN_DEFS:
                        translateDebugMessageData(textSeverity, textType, messageSeverity, messageType);
                        callback.translatedMessage(textSeverity.toString(), textType.toString(), callbackData.pMessageString(), messageSeverity==VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
                        break;
                    case BOTH_VULKAN_DEFS:
                        callback.messageRaw(messageSeverity, messageType, pCallbackData, pUserData);
                        translateDebugMessageData(textSeverity, textType, messageSeverity, messageType);
                        callback.translatedMessage(textSeverity.toString(), textType.toString(), callbackData.pMessageString(), messageSeverity==VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
                        break;
                }
            });
        }
        return VK_FALSE;
    }

    private boolean createDebugUtilsMessenger(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfoEXT, LongBuffer longBuffer) {
        long functionHandle = vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT");
        if (functionHandle != VK_NULL_HANDLE) {
            return vkCreateDebugUtilsMessengerEXT(instance, createInfoEXT, null, longBuffer) == VK_SUCCESS;
        }
        return false;
    }

    public void registerCallback(IVulkanLoggerCallback callback) {
        callbacks.add(callback);
    }

    public void removeCallback(IVulkanLoggerCallback callback){
        callbacks.remove(callback);
    }

    private void translateDebugMessageData(StringBuilder textSeverity, StringBuilder textType, int messageSeverity, int messageType){
        if(textSeverity.length()==0 && textType.length()==0){
            textSeverity.append(translateSeverity(messageSeverity));
            textType.append(translateType(messageType));
        }
    }

    private String translateSeverity(int severity) {
        String res;
        switch (severity) {
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:
                res = "VERBOSE";
                break;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
                res = "ERROR";
                break;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
                res = "WARNING";
                break;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
                res = "INFO";
                break;
            default:
                res = "UNDEFINED";
                break;
        }
        return res;
    }

    private String translateType(int type) {
        String res = "";
        switch (type) {
            case VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT:
                res = "GENERAL";
                break;
            case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT:
                res = "PERFORMANCE";
                break;
            case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT:
                res = "VALIDATION";
                break;
            default:
                res = "UNDEFINED";
                break;
        }
        return res;
    }


}
