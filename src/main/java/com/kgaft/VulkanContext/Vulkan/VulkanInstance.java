package com.kgaft.VulkanContext.Vulkan;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedExtensionException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedLayerException;
import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.VulkanLogger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK13.*;

public class VulkanInstance extends DestroyableObject {
    public static final String VK_LAYER_KHRONOS_validation = "VK_LAYER_KHRONOS_validation";
    public static final String VK_LAYER_KHRONOS_synchronization2 = "VK_LAYER_KHRONOS_synchronization2";
    public static final String VK_LAYER_KHRONOS_profiles = "VK_LAYER_KHRONOS_profiles";
    public static final String VK_LAYER_LUNARG_api_dump = "VK_LAYER_LUNARG_api_dump";
    public static final String VK_LAYER_LUNARG_gfxreconstruct = "VK_LAYER_LUNARG_gfxreconstruct";
    public static final String VK_LAYER_LUNARG_monitor = "VK_LAYER_LUNARG_monitor";
    public static final String VK_LAYER_LUNARG_screenshot = "VK_LAYER_LUNARG_screenshot";
    private static VulkanInstanceBuilder builderInstance = null;

    public static VulkanInstanceBuilder getBuilderInstance() {
        if (builderInstance == null) {
            builderInstance = new VulkanInstanceBuilder();
        }
        return builderInstance;
    }

    public static VulkanInstance createInstance(int[] resOutput)
            throws BuilderNotPopulatedException, NotSupportedExtensionException, NotSupportedLayerException {
        if (!builderInstance.appInfoEnabled) {
            throw new BuilderNotPopulatedException("Error you have not specified the info about your application");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            try {
                if (!builderInstance.enabledExtensions.isEmpty()) {
                    checkExtensions(builderInstance.enabledExtensions, stack);
                    builderInstance.createInfo
                            .ppEnabledExtensionNames(stack.callocPointer(builderInstance.enabledExtensions.size()));
                    builderInstance.enabledExtensions.forEach(element -> {
                        builderInstance.createInfo.ppEnabledExtensionNames().put(stack.UTF8Safe(element));
                    });
                    builderInstance.createInfo.ppEnabledExtensionNames().rewind();
                }
                if (!builderInstance.enabledLayers.isEmpty()) {
                    checkLayers(builderInstance.enabledLayers, stack);
                    builderInstance.createInfo
                            .ppEnabledLayerNames(stack.callocPointer(builderInstance.enabledLayers.size()));
                    builderInstance.enabledLayers.forEach(element -> {
                        builderInstance.createInfo.ppEnabledLayerNames().put(stack.UTF8Safe(element));
                    });
                    builderInstance.createInfo.ppEnabledLayerNames().rewind();
                }
            } catch (NotSupportedExtensionException | NotSupportedLayerException e) {
                
                throw e;
            }
            VulkanLogger logger = new VulkanLogger();
            VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = null;
            if (!builderInstance.enabledLayers.isEmpty()) {
                debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
                logger.describeLogger(stack, debugCreateInfo);
                builderInstance.createInfo.pNext(debugCreateInfo);
            }
            PointerBuffer instRes = stack.callocPointer(1);
            int res = vkCreateInstance(builderInstance.createInfo, null, instRes);
            if (resOutput != null) {
                resOutput[0] = res;
            }
            if (res != VK_SUCCESS) {
                return null;
            }
            VkInstance instance = new VkInstance(instRes.get(), builderInstance.createInfo);
            VulkanInstance result = new VulkanInstance(instance, null);
            if (!builderInstance.enabledLayers.isEmpty()) {
                logger.initLoggerInstance(instance, debugCreateInfo, stack);
                result.logger = logger;
            }
            result.setEnabledLayers(builderInstance.enabledLayers);

            return result;
        }

    }

    private static void checkLayers(List<String> toCheck, MemoryStack stack) throws NotSupportedLayerException {
        int[] layerCount = new int[1];
        vkEnumerateInstanceLayerProperties(layerCount, null);
        VkLayerProperties.Buffer layers = VkLayerProperties.calloc(layerCount[0], stack);
        vkEnumerateInstanceLayerProperties(layerCount, layers);
        for (String el : toCheck) {
            boolean found = false;
            for (VkLayerProperties cel : layers) {
                if (cel.layerNameString().equals(el)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NotSupportedLayerException(el);
            }
        }
    }

    private static void checkExtensions(List<String> toCheck, MemoryStack stack) throws NotSupportedExtensionException {
        int[] extensionCount = new int[1];
        vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, null);
        VkExtensionProperties.Buffer extensions = VkExtensionProperties.calloc(extensionCount[0], stack);
        vkEnumerateInstanceExtensionProperties((ByteBuffer) null, extensionCount, extensions);
        for (String el : toCheck) {
            boolean found = false;
            for (VkExtensionProperties cel : extensions) {
                if (cel.extensionNameString().equals(el)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NotSupportedExtensionException(el);
            }
        }
    }

    private VkInstance instance;
    private VulkanLogger logger;
    private List<String> enabledLayers;

    public VulkanInstance(VkInstance instance, VulkanLogger logger) {
        this.instance = instance;
        this.logger = logger;
    }

    protected void setEnabledLayers(List<String> enabledLayers) {
        this.enabledLayers = enabledLayers;
    }

    public List<String> getEnabledLayers() {
        return enabledLayers;
    }

    public VkInstance getInstance() {
        return instance;
    }

    public VulkanLogger getLogger() {
        return logger;
    }

    @Override
    public void destroy() {
        this.destroyed = true;
        logger.destroy();
        vkDestroyInstance(instance, null);
    }
}
