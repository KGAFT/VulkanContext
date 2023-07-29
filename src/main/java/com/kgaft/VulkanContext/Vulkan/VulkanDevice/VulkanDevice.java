package com.kgaft.VulkanContext.Vulkan.VulkanDevice;

import com.kgaft.VulkanContext.MemoryUtils.MemoryStackUtils;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity.DeviceSuitability;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitabillity.DeviceSuitabilityResults;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK13.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VulkanDevice {
    private static VulkanDeviceBuilder deviceBuilderInstance = new VulkanDeviceBuilder();

    public static List<DeviceSuitabilityResults> enumerateSupportedDevices(VulkanInstance instance) {
        List<DeviceSuitabilityResults> results = new ArrayList<>();
        MemoryStack stack = MemoryStackUtils.acquireStack();
        int[] physicalDeviceCount = new int[1];
        vkEnumeratePhysicalDevices(instance.getInstance(), physicalDeviceCount, null);
        PointerBuffer pb = stack.callocPointer(physicalDeviceCount[0]);
        vkEnumeratePhysicalDevices(instance.getInstance(), physicalDeviceCount, pb);

        while (pb.hasRemaining()) {
            VkPhysicalDevice device = new VkPhysicalDevice(pb.get(), instance.getInstance());
            DeviceSuitabilityResults suitabilityResults = new DeviceSuitabilityResults();
            if (DeviceSuitability.isDeviceSuitable(stack, device, deviceBuilderInstance, suitabilityResults)) {
                results.add(suitabilityResults);
            }
        }
        MemoryStackUtils.freeStack(stack);
        return results;
    }

    public static VulkanDeviceBuilder getDeviceBuilderInstance() {
        return deviceBuilderInstance;
    }

    public static VulkanDevice buildDevice(VkPhysicalDevice device, DeviceSuitabilityResults results) {

        deviceBuilderInstance.clear();
        return null;
    }

    private VkDevice device;
    private VulkanInstance instance;
    private List<VulkanQueue> queues;
    private VkPhysicalDevice baseDevice;

    private VulkanDevice(DeviceSuitabilityResults results) {
        this.baseDevice = results.getBaseDevice();
    }

    private void createLogicalDevice(DeviceSuitabilityResults suitabilityResults) {
        try (MemoryStack stack = MemoryStackUtils.acquireStack()) {

            HashSet<Integer> uniqueIndices = new HashSet<>();
            suitabilityResults.getRequiredQueues().values().forEach(element -> {
                uniqueIndices.add(element);
            });
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


            PointerBuffer pExtensions = stack.callocPointer(suitabilityResults.getRequiredExtensions().size());
            suitabilityResults.getRequiredExtensions().forEach(extension -> {
                pExtensions.put(stack.UTF8Safe(extension));
            });
            pExtensions.rewind();

            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack);
            deviceCreateInfo.sType$Default();
            deviceCreateInfo.pQueueCreateInfos(queueCreateInfos);
            deviceCreateInfo.pEnabledFeatures(suitabilityResults.getEnaledFeatures());

            deviceCreateInfo.ppEnabledExtensionNames(pExtensions);

            if (!instance.getEnabledLayers().isEmpty()) {
                deviceCreateInfo.ppEnabledLayerNames(stack.callocPointer(instance.getEnabledLayers().size()));
                instance.getEnabledLayers().forEach(element->{
                    deviceCreateInfo.ppEnabledLayerNames().put(stack.UTF8Safe(element));
                });
                
                deviceCreateInfo.ppEnabledLayerNames().rewind();
            }
            PointerBuffer deviceResult = stack.callocPointer(1);
            if (vkCreateDevice(suitabilityResults.getBaseDevice(), deviceCreateInfo, null, deviceResult) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create device");
            }
            device = new VkDevice(deviceResult.get(), suitabilityResults.getBaseDevice(), deviceCreateInfo);
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
}
