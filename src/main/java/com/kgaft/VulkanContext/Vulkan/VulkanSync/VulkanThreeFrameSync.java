package com.kgaft.VulkanContext.Vulkan.VulkanSync;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanThreeFrameSync {

    private static final int MAX_FRAMES_IN_FLIGHT = 2;

    private List<Long> imageAvailableSemaphores = new ArrayList();
    private List<Long> renderFinishedSemaphores = new ArrayList();
    private List<Long> inFlightFences = new ArrayList();
    private List<Long> imagesInFlight = new ArrayList();
    private VulkanDevice device;
    private int currentFrame = 0;

    public VulkanThreeFrameSync(VulkanDevice device) {
        this.device = device;
        createSyncObjects();
    }

    public int prepareForNextImage(long swapChain) {
        int[] res = new int[1];
        vkWaitForFences(device.getDevice(), inFlightFences.get(currentFrame), true, Long.MAX_VALUE);
        KHRSwapchain.vkAcquireNextImageKHR(device.getDevice(), swapChain, Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), VK_NULL_HANDLE, res);
        while (res[0] > 2) {
            vkWaitForFences(device.getDevice(), inFlightFences.get(currentFrame), true, Long.MAX_VALUE);
            KHRSwapchain.vkAcquireNextImageKHR(device.getDevice(), swapChain, Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), VK_NULL_HANDLE, res);
        }
        return res[0];
    }

    private void createSyncObjects() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSemaphoreCreateInfo createInfo = VkSemaphoreCreateInfo.calloc(stack);
            createInfo.sType$Default();

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            fenceInfo.sType$Default();
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);
            long[] imgSemaphore = new long[1];
            long[] rendSemaphore = new long[1];
            long[] fence = new long[1];

            for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                if (vkCreateSemaphore(device.getDevice(), createInfo, null, imgSemaphore)
                        != VK_SUCCESS
                        || vkCreateSemaphore(device.getDevice(), createInfo, null, rendSemaphore)
                        != VK_SUCCESS
                        || vkCreateFence(device.getDevice(), fenceInfo, null, fence) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create sync object");
                }
                imageAvailableSemaphores.add(imgSemaphore[0]);
                renderFinishedSemaphores.add(rendSemaphore[0]);
                inFlightFences.add(fence[0]);
            }
            imagesInFlight.add(0l);
            imagesInFlight.add(0l);
            imagesInFlight.add(0l);
        }
    }
}
