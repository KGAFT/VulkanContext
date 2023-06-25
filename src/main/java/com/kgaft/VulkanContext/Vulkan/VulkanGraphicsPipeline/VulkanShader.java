package com.kgaft.VulkanContext.Vulkan.VulkanGraphicsPipeline;

import com.kgaft.VulkanContext.DestroyableObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK13.*;

public class VulkanShader extends DestroyableObject {

    public static final int VERTEX_SHADER = 0;
    public static final int FRAGMENT_SHADER = 1;
    public static final int GEOMETRY_SHADER = 2;
    private VkPipelineShaderStageCreateInfo.Buffer stages;
    private MemoryStack stack;

    public VulkanShader(HashMap<Long, Integer> toCreate) {
        stack = MemoryStack.create();
        stages = VkPipelineShaderStageCreateInfo.calloc(toCreate.size(), stack);
        for (Map.Entry<Long, Integer> entry : toCreate.entrySet()) {
            Long module = entry.getKey();
            Integer shaderType = entry.getValue();
            stages.sType$Default();
            stages.stage(translateShaderType(shaderType));
            stages.module(module);
            stages.pName(stack.UTF8Safe("main"));
            stages.flags(0);
            stages.pNext(0);
            stages.get();
        }
        stages.rewind();
    }

    public VkPipelineShaderStageCreateInfo.Buffer getStages() {
        return stages;
    }

    private int translateShaderType(int type) {
        switch (type) {
            case FRAGMENT_SHADER:
                return VK_SHADER_STAGE_FRAGMENT_BIT;
            case GEOMETRY_SHADER:
                return VK_SHADER_STAGE_GEOMETRY_BIT;
            default:
                return VK_SHADER_STAGE_VERTEX_BIT;
        }
    }
}
