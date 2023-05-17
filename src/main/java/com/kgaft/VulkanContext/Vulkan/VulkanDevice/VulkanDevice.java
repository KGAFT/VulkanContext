package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.FloatBuffer;
import java.util.HashSet;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;


import static org.lwjgl.vulkan.VK13.*;

public class VulkanDevice {
    public static LinkedHashMap<VkPhysicalDevice, VkPhysicalDeviceProperties> enumerateSupportedDevices(
            VkInstance instance, long surface) {
        int[] deviceCount = new int[1];
        vkEnumeratePhysicalDevices(instance, deviceCount, null);
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer pBuffer = stack.callocPointer(deviceCount[0]);
        vkEnumeratePhysicalDevices(instance, deviceCount, pBuffer);
        LinkedHashMap<VkPhysicalDevice, VkPhysicalDeviceProperties> result = new LinkedHashMap<>();
        while (pBuffer.hasRemaining()) {
            VkPhysicalDevice device = new VkPhysicalDevice(pBuffer.get(), instance);
            if (DeviceSuitability.isDeviceSuitable(device, surface)) {
                VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc();
                vkGetPhysicalDeviceProperties(device, properties);
                result.put(device, properties);
            }
        }
        return result;

    }

    private VkPhysicalDevice deviceToCreate;
    private long renderSurface;
    private VkInstance vkInstance;
    private int sampleCount;
    private VkDevice device;
    private VkQueue graphicsQueue;
    private VkQueue presentQueue;
    private long commandPool;

    public VulkanDevice(VkPhysicalDevice deviceToCreate, long surface, VkInstance instance, boolean debugDevice) {
        this.deviceToCreate = deviceToCreate;
        this.renderSurface = surface;
        this.vkInstance = instance;
        if (!DeviceSuitability.isDeviceSuitable(deviceToCreate, surface)) {
            throw new UnsupportedOperationException("Error: the device you specified is not supported");
        }
        VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc();
        vkGetPhysicalDeviceProperties(deviceToCreate, properties);
        this.sampleCount = properties.limits().framebufferColorSampleCounts()
                & properties.limits().framebufferDepthSampleCounts();

        properties.free();
        createLogicalDevice(debugDevice);
        createCommandPool();
    }

    private void createLogicalDevice(boolean enableLog) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            QueueFamilyIndices indices = DeviceSuitability.findQueueFamilies(deviceToCreate, renderSurface);
            HashSet<Integer> uniqueIndices = new HashSet<>();
            uniqueIndices.add(indices.graphicsFamily);
            uniqueIndices.add(indices.presentFamily);
            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueIndices.size(),
                    stack);
            AtomicInteger counter = new AtomicInteger(0);
            FloatBuffer queuePriority = stack.callocFloat(1);
            queuePriority.put(1.0f);
            queuePriority.rewind();

            uniqueIndices.forEach(index -> {
                queueCreateInfos.get(counter.get()).sType$Default();
                queueCreateInfos.get(counter.get()).queueFamilyIndex(index);
                queueCreateInfos.get(counter.get()).pQueuePriorities(queuePriority);
                counter.getAndIncrement();
            });
            queueCreateInfos.rewind();

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
            deviceFeatures.samplerAnisotropy(true);
            List<String> exts = DeviceSuitability.getRequiredDeviceExtensions();

            PointerBuffer pExtensions = stack.callocPointer(exts.size());
            exts.forEach(extension -> {
                pExtensions.put(stack.UTF8Safe(extension));
            });
            pExtensions.rewind();

            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack);
            deviceCreateInfo.sType$Default();
            deviceCreateInfo.pQueueCreateInfos(queueCreateInfos);
            deviceCreateInfo.pEnabledFeatures(deviceFeatures);

            deviceCreateInfo.ppEnabledExtensionNames(pExtensions);

            if (enableLog) {
                deviceCreateInfo.ppEnabledLayerNames(stack.callocPointer(1));
                deviceCreateInfo.ppEnabledLayerNames().put(stack.UTF8Safe("VK_LAYER_KHRONOS_validation"));
                deviceCreateInfo.ppEnabledLayerNames().rewind();
            }
            PointerBuffer deviceResult = stack.callocPointer(1);
            if (vkCreateDevice(deviceToCreate, deviceCreateInfo, null, deviceResult) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create device");
            }
            device = new VkDevice(deviceResult.get(), deviceToCreate, deviceCreateInfo);
            deviceResult.clear();
            deviceResult.rewind();

            vkGetDeviceQueue(device, indices.graphicsFamily, 0, deviceResult);
            graphicsQueue = new VkQueue(deviceResult.get(), device);
            deviceResult.clear();
            deviceResult.rewind();

            vkGetDeviceQueue(device, indices.presentFamily, 0, deviceResult);
            presentQueue = new VkQueue(deviceResult.get(), device);
        }
    }

    private void createCommandPool() {
        QueueFamilyIndices indices = DeviceSuitability.findQueueFamilies(deviceToCreate, renderSurface);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo createInfo = VkCommandPoolCreateInfo.calloc(stack);
            createInfo.sType$Default();
            createInfo.queueFamilyIndex(indices.graphicsFamily);
            createInfo.flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT | VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            long[] result = new long[1];
            vkCreateCommandPool(device, createInfo, null, result);
            this.commandPool = result[0];
        }
    }

    VkCommandBuffer beginSingleTimeCommands() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType$Default();
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(commandPool);
            allocInfo.commandBufferCount(1);
            
            PointerBuffer tempRes = stack.callocPointer(1);
            vkAllocateCommandBuffers(device, allocInfo, tempRes);
            VkCommandBuffer result = new VkCommandBuffer(tempRes.get(), device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType$Default();
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            vkBeginCommandBuffer(result, beginInfo);
            return result;
        }
    }

    void endSingleTimeCommands(VkCommandBuffer cmd){
        vkEndCommandBuffer(cmd);
        try(MemoryStack stack = MemoryStack.stackPush()){
            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack);
            Vk
            submitInfo.sType$Default();
            submitInfo.pCommandBuffers(cmd);
            vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE);
            vkQueueWaitIdle(graphicsQueue);

            vkFreeCommandBuffer(device, commandPool, cmd);
        }
        
    }
}
