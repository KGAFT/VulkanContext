package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.DefaultVulkanLoggerCallback;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.VulkanLogger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.vulkan.VkPhysicalDevice;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.vulkan.VK13.vkEnumeratePhysicalDevices;

public class Main {
    public static void main(String[] args) {
        VulkanLogger.getInstance().registerCallback(new DefaultVulkanLoggerCallback());
        VulkanInstance instance = new VulkanInstance();
        System.out.print(instance.createInstance("MyApp", "MyEngine", true, new ArrayList<>()));
        int[] deviceCount = new int[1];
        vkEnumeratePhysicalDevices(instance.getInstance(), deviceCount, null);
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer pBuffer = stack.callocPointer(deviceCount[0]);
        vkEnumeratePhysicalDevices(instance.getInstance(), deviceCount, pBuffer);
        VkPhysicalDevice device = new VkPhysicalDevice(pBuffer.get(), instance.getInstance());

        GLFW.glfwInit();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        long window = GLFW.glfwCreateWindow(640, 480, "Test", 0, 0);
        long[] windowHandle = new long[1];
        glfwCreateWindowSurface(instance.getInstance(), window, null, windowHandle);
        PointerBuffer error = stack.callocPointer(1);

        System.out.println(DeviceSuitability.isDeviceSuitable(device, windowHandle[0]));
        while(!GLFW.glfwWindowShouldClose(window)){
            GLFW.glfwPollEvents();
        }
    }
}