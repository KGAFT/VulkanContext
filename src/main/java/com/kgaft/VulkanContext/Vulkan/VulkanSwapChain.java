
package com.kgaft.VulkanContext.Vulkan;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability.DeviceSuitability;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability.QueueFamilyIndices;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.vulkan.*;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.DeviceSuitability.SwapChainSupportDetails;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.vulkan.KHRSurface.*;

import static org.lwjgl.vulkan.VK13.*;

public class VulkanSwapChain {
    private List<Long> swapChainImages = new ArrayList<>();
    private List<Long> swapChainImageViews = new ArrayList<>();
    private int width;
    private int height;
    private VulkanDevice device;
    private long swapChain;
    private int swapChainImageFormat;
    private VkExtent2D swapChainExtent;
    
    public VulkanSwapChain(VulkanDevice device, int width, int height){
        this.device = device;
        this.width = width;
        this.height = height;
        createSwapChain();
    }
    
    private void createSwapChain(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            SwapChainSupportDetails details = DeviceSuitability.querySwapChainSupport(device.getDeviceToCreate(), device.getRenderSurface());
            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(details.formats);
            int presentMode = chooseSwapPresentMode(details.presentModes);
            VkExtent2D extent = chooseSwapExtent(details.capabilities);
            int imageCount = details.capabilities.minImageCount()+1;
            if(details.capabilities.maxImageCount()>0 && imageCount>details.capabilities.maxImageCount()){
                imageCount = details.capabilities.maxImageCount();
            }
            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
            createInfo.sType$Default();
            createInfo.surface(device.getRenderSurface());
            
            createInfo.minImageCount(imageCount);
            createInfo.imageFormat(surfaceFormat.format());
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
            
            QueueFamilyIndices indices = DeviceSuitability.findQueueFamilies(device.getDeviceToCreate(), device.getRenderSurface());
            if(indices.graphicsFamily!=indices.presentFamily){
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
            }
            else{
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
             
            }
            createInfo.preTransform(details.capabilities.currentTransform());
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);
            createInfo.oldSwapchain(VK_NULL_HANDLE);
            long[] res = new long[1];
            if(KHRSwapchain.vkCreateSwapchainKHR(device.getDevice(), createInfo, null, res)!=VK_SUCCESS){
                throw new RuntimeException("Failed to create swap chain");
            }
            this.swapChain = res[0];
            int[] swImageCount = new int[1];
            KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChain, swImageCount, null);
            long[] swImages = new long[swImageCount[0]];
            KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapChain, swImageCount, swImages);
            for(long image : swImages){
                swapChainImages.add(image);
            }
            this.swapChainImageFormat = surfaceFormat.format();
            this.swapChainExtent = extent;
        }
    }
    
    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(List<VkSurfaceFormatKHR> availableFormats){
           return availableFormats.stream()
                    .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_UNORM)
                    .filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                    .findAny()
                    .orElse(availableFormats.get(0));
    }
    
    private int chooseSwapPresentMode(List<Integer> availablePresentModes){
        for(int presentMode : availablePresentModes){
            if(presentMode==VK_PRESENT_MODE_MAILBOX_KHR){
                return presentMode;
            }
        }
        return VK_PRESENT_MODE_IMMEDIATE_KHR;
    }
    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities){
         if(capabilities.currentExtent().width() != Long.MAX_VALUE) {
                return capabilities.currentExtent();
            }

            VkExtent2D actualExtent = VkExtent2D.calloc().set(width, height);

            VkExtent2D minExtent = capabilities.minImageExtent();
            VkExtent2D maxExtent = capabilities.maxImageExtent();

            actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
            actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

            return actualExtent;
    }
    
     private int clamp(int min, int max, int value) {
            return Math.max(min, Math.min(max, value));
      }
}
