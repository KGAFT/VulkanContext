package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability.QueueFamilyIndices;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability.DeviceSuitability;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.FloatBuffer;
import java.util.HashSet;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.VK13.*;

public class VulkanDevice extends DestroyableObject {

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

    /**
     *
     * @return VkBuffer at index 0. VkBufferMemory at index 1.
     */
    public long[] createBuffer(long size, int usageFlags, int memoryPropertyFlags) {
        long[] result = new long[2];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo createInfo = VkBufferCreateInfo.calloc(stack);
            createInfo.sType$Default();
            createInfo.size(size);
            createInfo.usage(usageFlags);
            createInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            long[] tempRes = new long[1];
            if (vkCreateBuffer(device, createInfo, null, tempRes) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create buffer");
            }
            result[0] = tempRes[0];
            VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
            vkGetBufferMemoryRequirements(device, tempRes[0], memRequirements);

            VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.calloc(stack);
            allocateInfo.sType$Default();
            allocateInfo.allocationSize(memRequirements.size());
            allocateInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), memoryPropertyFlags, stack));
            tempRes[0] = 0;
            if (vkAllocateMemory(device, allocateInfo, null, tempRes) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate memory for buffer");
            }
            result[1] = tempRes[0];
            return result;
        }
    }

    public void memcpy(ByteBuffer dst, ByteBuffer src, int size){
        src.limit(size);
        dst.put(src);
        src.limit(src.capacity()).rewind();
    }

    public void copyBufferToImage(MemoryStack stack, long buffer, long image, int width, int height, int layerCount)
    {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();

        VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
        region.bufferOffset(0);
        region.bufferRowLength(0);
        region.bufferImageHeight(0);
        region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        region.imageSubresource().mipLevel(0);
        region.imageSubresource().baseArrayLayer(0);
        region.imageSubresource().layerCount(layerCount);
        region.imageOffset(VkOffset3D.calloc(stack).x(0).y(0).z(0));
        region.imageExtent(VkExtent3D.calloc(stack).width(width).height(height).depth(1));

        vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);

        endSingleTimeCommands(commandBuffer);
    }

    public long getCommandPool() {
        return commandPool;
    }
    
    
    public long createImage(int width, int height, int format, int tiling, int usage, boolean isFrameBuffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo createInfo = VkImageCreateInfo.calloc(stack);
            createInfo.sType$Default();
            createInfo.imageType(VK_IMAGE_TYPE_2D);
            createInfo.extent().width(width);
            createInfo.extent().height(height);
            createInfo.extent().depth(1);
            createInfo.mipLevels(1);
            createInfo.arrayLayers(1);
            createInfo.format(format);
            createInfo.tiling(tiling);
            createInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            createInfo.samples(VK_SAMPLE_COUNT_1_BIT);
            createInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            long[] result = new long[1];
            if (vkCreateImage(device, createInfo, null, result) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image");
            }
            return result[0];
        }
    }

    public VkQueue getGraphicsQueue() {
        return graphicsQueue;
    }

    public VkQueue getPresentQueue() {
        return presentQueue;
    }

    @Override
    public void destroy() {
        vkDestroyCommandPool(device, commandPool, null);
        vkDestroyDevice(device, null);
        super.destroy();
    }

    public long createImageView(long image, int format) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType$Default();
            viewInfo.image(image);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.format(format);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            long[] result = new long[1];
            if (vkCreateImageView(device, viewInfo, null, result) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image view");
            }
            return result[0];
        }
    }

    public int findSupportedFormat(List<Integer> candidates, int tiling, int features) {
        for (int format : candidates) {
            VkFormatProperties props = VkFormatProperties.calloc();
            vkGetPhysicalDeviceFormatProperties(deviceToCreate, format, props);

            if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
                return format;
            } else if (tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
                return format;
            }
            props.free();
        }
        throw new RuntimeException("failed to find supported format!");
    }

    public void createImageWithInfo(MemoryStack stack, VkImageCreateInfo imageInfo, int properties, long[] image, long[] imageMemory) {
        if (vkCreateImage(device, imageInfo, null, image) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image!");
        }
        VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
        vkGetImageMemoryRequirements(device, image[0], memRequirements);
        VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
        allocInfo.sType$Default();
        allocInfo.allocationSize(memRequirements.size());
        allocInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), properties, stack));
        if (vkAllocateMemory(device, allocInfo, null, imageMemory) != VK_SUCCESS) {
            throw new RuntimeException("failed to allocate image memory!");
        }

        if (vkBindImageMemory(device, image[0], imageMemory[0], 0) != VK_SUCCESS) {
            throw new RuntimeException("failed to bind image memory!");
        }
    }

    public int findMemoryType(int typeFilter, int properties, MemoryStack stack) {
        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.calloc(stack);
        vkGetPhysicalDeviceMemoryProperties(deviceToCreate, memProperties);
        for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
            if ((typeFilter & (int) (1 << i)) > 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }
        throw new RuntimeException("Failed to find suitable memory type");
    }

    public void copyBuffer(long srcBuffer, long dstBuffer, long size) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBuffer commandBuffer = beginSingleTimeCommands();
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
            copyRegion.srcOffset(0); // Optional
            copyRegion.dstOffset(0); // Optional
            copyRegion.size(size);
            vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);
            endSingleTimeCommands(commandBuffer);
        }

    }

    public VkCommandBuffer beginSingleTimeCommands() {
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

    public void endSingleTimeCommands(VkCommandBuffer cmd) {
        vkEndCommandBuffer(cmd);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack);
            submitInfo.sType$Default();
            PointerBuffer pBuffer = stack.callocPointer(1);
            pBuffer.put(cmd.address());
            pBuffer.rewind();
            submitInfo.pCommandBuffers(pBuffer);
            vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE);
            vkQueueWaitIdle(graphicsQueue);

            vkFreeCommandBuffers(device, commandPool, pBuffer);
        }

    }

    public VkPhysicalDevice getDeviceToCreate() {
        return deviceToCreate;
    }

    public long getRenderSurface() {
        return renderSurface;
    }

    public VkDevice getDevice() {
        return device;
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

}
