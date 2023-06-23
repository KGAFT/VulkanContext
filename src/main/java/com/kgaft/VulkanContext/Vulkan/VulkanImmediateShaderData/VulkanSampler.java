package com.kgaft.VulkanContext.Vulkan.VulkanImmediateShaderData;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDescriptors.IDescriptorObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanSampler extends DestroyableObject implements IDescriptorObject {

    private long sampler;
    private long samplerImageView;
    private VulkanDevice device;
    private int binding;
    private int imageLayout;

    public VulkanSampler(VulkanDevice device, int binding) {
        this.device = device;
        this.binding = binding;
        createSampler();
    }

    public int getBinding() {
        return binding;
    }

    @Override
    public void destroy() {
        vkDestroySampler(device.getDevice(), sampler, null);
        super.destroy();
    }

    public void setSamplerImageView(long samplerImageView, int imageLayout) {
        this.samplerImageView = samplerImageView;
        this.imageLayout = imageLayout;
    }

    public long getSamplerImageView() {
        return samplerImageView;
    }

    public int getImageLayout() {
        return imageLayout;
    }

    @Override
    public void prepareWriteInfo(MemoryStack stack, VkWriteDescriptorSet.Buffer output, int currentInstanceIndex) {
        VkDescriptorImageInfo.Buffer descriptorImage = VkDescriptorImageInfo.calloc(1, stack);
        descriptorImage.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        descriptorImage.imageView(samplerImageView);
        descriptorImage.sampler(sampler);

        output.sType$Default();
        output.dstBinding(binding);
        output.dstArrayElement(0);
        output.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        output.descriptorCount(1);
        output.pImageInfo(descriptorImage);

    }

    private void createSampler() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc(stack);
            vkGetPhysicalDeviceProperties(device.getDeviceToCreate(), properties);

            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
            samplerInfo.sType$Default();
            samplerInfo.magFilter(VK_FILTER_LINEAR);
            samplerInfo.minFilter(VK_FILTER_LINEAR);
            samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(properties.limits().maxSamplerAnisotropy());
            samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            long[] res = new long[1];
            if (vkCreateSampler(device.getDevice(), samplerInfo, null, res) != VK_SUCCESS) {
                throw new RuntimeException("failed to create texture sampler!");
            }
            this.sampler = res[0];
        }
    }

}
