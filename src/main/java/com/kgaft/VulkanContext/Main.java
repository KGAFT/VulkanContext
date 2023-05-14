package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        VulkanInstance instance = new VulkanInstance();
        System.out.print(instance.createInstance("MyApp", "MyEngine", true, new ArrayList<>()));

    }
}