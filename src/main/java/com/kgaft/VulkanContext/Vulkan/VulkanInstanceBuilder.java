package com.kgaft.VulkanContext.Vulkan;

import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

public class VulkanInstanceBuilder {
    protected VkApplicationInfo appInfo;
    protected VkInstanceCreateInfo createInfo;
    protected List<String> enabledExtensions = new ArrayList<>();
    protected List<String> enabledLayers = new ArrayList<>();
    protected boolean appInfoEnabled = false;
    protected String appName;
    protected String engineName;

    protected VulkanInstanceBuilder() {
        appInfo = VkApplicationInfo.calloc();
        createInfo = VkInstanceCreateInfo.calloc();
    }

    /**
     * @param apiVersion    use one of vulkan api version definition, like VK_API_VERSION_1_3
     * @param appVersion    use macro VK_MAKE_VERSION
     * @param engineVersion use macro VK_MAKE_VERSION
     */
    public VulkanInstanceBuilder setApplicationInfo(String appName, String engineName, int apiVersion, int appVersion, int engineVersion) {
        this.appName = appName;
        this.engineName = engineName;
        createInfo.sType$Default();
        appInfo.sType$Default();
        appInfo.apiVersion(apiVersion);
        appInfo.applicationVersion(appVersion);
        appInfo.engineVersion(engineVersion);
        appInfo.pNext(0);
        createInfo.pNext(0);
        this.appInfoEnabled = true;
        return this;
    }

    /**
     * @param extension use one of vulkan defined extensions like VK_EXT_DEBUG_UTILS_EXTENSION_NAME
     */
    public VulkanInstanceBuilder addExtension(String extension) {
        createInfo.sType$Default();
        enabledExtensions.add(extension);
        createInfo.pNext(0);
        return this;
    }

    /**
     * @param layer use one defined in library layers, like VK_LAYER_KHRONOS_validation, or any another layer
     */
    public VulkanInstanceBuilder addLayer(String layer) {
        createInfo.sType$Default();
        enabledLayers.add(layer);
        createInfo.pNext(0);
        return this;
    }

    public VulkanInstanceBuilder setInstanceFlags(int flags) {
        createInfo.sType$Default();
        createInfo.flags(flags);
        createInfo.pNext(0);
        return this;
    }

    protected void clear() {
        enabledExtensions.clear();
        appInfoEnabled = false;
        enabledLayers.clear();
        appInfo.clear();
        createInfo.clear();
        appName = null;
        engineName = null;
    }
}
