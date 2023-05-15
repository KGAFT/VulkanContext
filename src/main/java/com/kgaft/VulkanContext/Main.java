package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.DefaultVulkanLoggerCallback;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.VulkanLogger;
import com.kgaft.Window.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.glfw.GLFWVulkan;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK13.vkEnumeratePhysicalDevices;

public class Main {
    public static void main(String[] args) {
        Window.prepareWindow(800, 600, "WindowNull", true);

        VulkanLogger.getInstance().registerCallback(new DefaultVulkanLoggerCallback());
        VulkanInstance instance = new VulkanInstance();

        List<String> glfwExtensions = new ArrayList<>();
        

        PointerBuffer glfwExts = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        while(glfwExts.hasRemaining()){
            glfwExtensions.add(glfwExts.getStringUTF8());
        }

        System.out.print(instance.createInstance("MyApp", "MyEngine", true, glfwExtensions));
        int[] deviceCount = new int[1];
        vkEnumeratePhysicalDevices(instance.getInstance(), deviceCount, null);
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer pBuffer = stack.callocPointer(deviceCount[0]);
        vkEnumeratePhysicalDevices(instance.getInstance(), deviceCount, pBuffer);
        VkPhysicalDevice device = new VkPhysicalDevice(pBuffer.get(), instance.getInstance());
        
        Window window = Window.getWindow();
        long windowSurface = window.getSurface(instance.getInstance());   
        System.out.println(DeviceSuitability.isDeviceSuitable(device, windowSurface));
    }
}