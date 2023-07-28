package com.kgaft.VulkanContext;


import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedExtensionException;
import com.kgaft.VulkanContext.Exceptions.NotSupportedLayerException;
import com.kgaft.VulkanContext.Vulkan.VulkanInstance;
import org.lwjgl.vulkan.VK13;

import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class Main {
    public static void main(String[] args) throws NotSupportedExtensionException, NotSupportedLayerException, BuilderNotPopulatedException {
        VulkanInstance.getBuilderInstance().addLayer(VulkanInstance.VK_LAYER_KHRONOS_validation)
                .addExtension(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
                .setApplicationInfo("HelloApp", "HElloENgine", VK13.VK_API_VERSION_1_3, VK_MAKE_VERSION(1,0,0), VK_MAKE_VERSION(1,0,0));
        int[] instRes = new int[1];
        VulkanInstance instance = VulkanInstance.createInstance(instRes);
        System.out.print(instRes[0]==VK_SUCCESS);
    }
}