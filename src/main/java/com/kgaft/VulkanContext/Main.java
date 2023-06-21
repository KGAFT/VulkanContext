package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.DefaultVulkanLoggerCallback;
import com.kgaft.VulkanContext.Vulkan.VulkanLogger.VulkanLogger;
import com.kgaft.VulkanContext.Vulkan.VulkanSwapChain;
import com.kgaft.Window.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.glfw.GLFWVulkan;
import java.util.ArrayList;
import java.util.List;


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

        System.out.println(instance.createInstance("MyApp", "MyEngine", true, glfwExtensions));
        
        
        Window window = Window.getWindow();
        long windowSurface = window.getSurface(instance.getInstance());   
        VkPhysicalDevice deviceToCreate = (VkPhysicalDevice) VulkanDevice.enumerateSupportedDevices(instance.getInstance(), windowSurface).keySet().toArray()[0];
        VulkanDevice device = new VulkanDevice(deviceToCreate, windowSurface, instance.getInstance(), true);
        VulkanSwapChain swapChain = new VulkanSwapChain(device, 800, 600);
        System.out.println("True");
    }
}