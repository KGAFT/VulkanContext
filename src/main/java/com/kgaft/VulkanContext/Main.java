package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.DefaultVulkanLoggerCallback;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.VulkanLogger;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        VulkanLogger.getInstance().registerCallback(new DefaultVulkanLoggerCallback());
        VulkanInstance instance = new VulkanInstance();
        System.out.print(instance.createInstance("MyApp", "MyEngine", true, new ArrayList<>()));

    }
}