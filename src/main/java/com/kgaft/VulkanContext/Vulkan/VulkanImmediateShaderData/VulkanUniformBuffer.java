package com.kgaft.VulkanContext.Vulkan.VulkanImmediateShaderData;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import com.kgaft.VulkanContext.Vulkan.VulkanDescriptors.IDescriptorObject;
import java.nio.ByteBuffer;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanUniformBuffer extends DestroyableObject implements IDescriptorObject{

    private List<Long> uniformBuffers = new ArrayList();
    private List<Long> uniformBuffersMemory = new ArrayList();
    private List<PointerBuffer> uniformBuffersMapped = new ArrayList();
    private VulkanDevice device;
    private int binding;
    private long size;
    private int shaderStages;
    private ByteBuffer data;
    public VulkanUniformBuffer(VulkanDevice device, int binding, long size, int shaderStages, int instanceCount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.device = device;
            this.binding = binding;
            this.size = size;
            this.shaderStages = shaderStages;

            for (int i = 0; i < instanceCount; i++) {
                long[] res = device.createBuffer(size, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                uniformBuffers.add(res[0]);
                uniformBuffersMemory.add(res[1]);
                PointerBuffer data = stack.callocPointer(1);
                vkMapMemory(device.getDevice(), res[1], 0, size, 0, data);
                uniformBuffersMapped.add(data);
            }
            data = ByteBuffer.allocateDirect((int)size);
        }
    }
    
    public void flush(){
        for(PointerBuffer buff : uniformBuffersMapped){
            buff.put(data);
        }
    }

    public ByteBuffer getDataForWriting() {
        return data;
    }
    
    @Override
    public void destroy() {
         for (int i = 0; i < uniformBuffers.size(); ++i)
        {
            vkDestroyBuffer(device.getDevice(), uniformBuffers.get(i), null);
            vkFreeMemory(device.getDevice(), uniformBuffersMemory.get(i), null);
        }
        
        super.destroy();
    }

    @Override
    public void prepareWriteInfo(MemoryStack stack, VkWriteDescriptorSet.Buffer output, int currentInstanceIndex) {
        VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
        bufferInfo.buffer(uniformBuffers.get(currentInstanceIndex));
        bufferInfo.offset(0);
        bufferInfo.range(size);
        output.sType$Default();
        output.dstBinding(binding);
        output.dstArrayElement(0);
        output.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        output.descriptorCount(1);
        
        output.pBufferInfo(bufferInfo);
    }
    

}
