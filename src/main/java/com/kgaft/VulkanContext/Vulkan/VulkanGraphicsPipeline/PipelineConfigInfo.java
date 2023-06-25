package com.kgaft.VulkanContext.Vulkan.VulkanGraphicsPipeline;

import org.lwjgl.vulkan.*;

import java.nio.channels.Pipe;
import java.util.List;

import static org.lwjgl.vulkan.VK13.*;

public class PipelineConfigInfo {

    public void defaultPipelineConfigInfo(PipelineConfigInfo configInfo, int width, int height, int attachmentCount, boolean alphaBlending, boolean depthTest, int culling){
        configInfo.inputAssemblyInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        configInfo.inputAssemblyInfo.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        configInfo.inputAssemblyInfo.primitiveRestartEnable(false);


        configInfo.viewport.x(0);
        configInfo.viewport.y(0);
        configInfo.viewport.width(height);
        configInfo.viewport.height(width);
        configInfo.viewport.minDepth(0);
        configInfo.viewport.maxDepth(1);

        VkOffset2D offset2D = VkOffset2D.malloc();
        offset2D.clear();
        offset2D.x(0);
        offset2D.y(0);
        configInfo.scissor.offset(offset2D);

        VkExtent2D extent2D = VkExtent2D.calloc();
        extent2D.width(width);
        extent2D.height(height);
        configInfo.scissor.extent(extent2D);


        configInfo.rasterizationInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        configInfo.rasterizationInfo.depthClampEnable(false);
        configInfo.rasterizationInfo.rasterizerDiscardEnable(false);
        configInfo.rasterizationInfo.polygonMode(VK_POLYGON_MODE_FILL);
        configInfo.rasterizationInfo.lineWidth(1.0f);
        configInfo.rasterizationInfo.cullMode(culling);
        configInfo.rasterizationInfo.frontFace(VK_FRONT_FACE_CLOCKWISE);
        configInfo.rasterizationInfo.depthBiasEnable(false);
        configInfo.rasterizationInfo.depthBiasConstantFactor(0.0f); // Optional
        configInfo.rasterizationInfo.depthBiasClamp(0.0f); // Optional
        configInfo.rasterizationInfo.depthBiasSlopeFactor(0.0f); // Optional



        configInfo.multisampleInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        configInfo.multisampleInfo.sampleShadingEnable(false);
        configInfo.multisampleInfo.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
        configInfo.multisampleInfo.minSampleShading(1.0f); // Optional
        configInfo.multisampleInfo.pSampleMask(null); // Optional
        configInfo.multisampleInfo.alphaToCoverageEnable(false); // Optional
        configInfo.multisampleInfo.alphaToOneEnable(false); // Optional

        configInfo.colorBlendAttachments = VkPipelineColorBlendAttachmentState.calloc(attachmentCount);

        if(alphaBlending){

            for (int i = 0; i < attachmentCount; ++i){
                configInfo.colorBlendAttachments.blendEnable(true);
                configInfo.colorBlendAttachments.colorWriteMask(
                        VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT |
                                VK_COLOR_COMPONENT_A_BIT);
                configInfo.colorBlendAttachments.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
                configInfo.colorBlendAttachments.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
                configInfo.colorBlendAttachments.colorBlendOp(VK_BLEND_OP_ADD);
                configInfo.colorBlendAttachments.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE);
                configInfo.colorBlendAttachments.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);
                configInfo.colorBlendAttachments.alphaBlendOp(VK_BLEND_OP_ADD);
                configInfo.colorBlendAttachments.get();
            }
        }
        else{
            for (int i = 0; i < attachmentCount; ++i){
                configInfo.colorBlendAttachments.colorWriteMask(
                        VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT |
                                VK_COLOR_COMPONENT_A_BIT);
                configInfo.colorBlendAttachments.blendEnable(false);
                configInfo.colorBlendAttachments.srcColorBlendFactor(VK_BLEND_FACTOR_ONE); // Optional
                configInfo.colorBlendAttachments.dstColorBlendFactor(VK_BLEND_FACTOR_ZERO); // Optional
                configInfo.colorBlendAttachments.colorBlendOp(VK_BLEND_OP_ADD); // Optional
                configInfo.colorBlendAttachments.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE); // Optional
                configInfo.colorBlendAttachments.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO); // Optional
                configInfo.colorBlendAttachments.alphaBlendOp(VK_BLEND_OP_ADD); // Optional
                configInfo.colorBlendAttachments.get();
            }
        }

        configInfo.colorBlendAttachments.rewind();

        configInfo.colorBlendInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        configInfo.colorBlendInfo.logicOpEnable(false);
        configInfo.colorBlendInfo.logicOp(VK_LOGIC_OP_COPY); // Optional
        configInfo.colorBlendInfo.pAttachments(configInfo.colorBlendAttachments);
        configInfo.colorBlendInfo.blendConstants(0, 0); // Optional
        configInfo.colorBlendInfo.blendConstants(1, 0); // Optional
        configInfo.colorBlendInfo.blendConstants(2, 0); // Optional
        configInfo.colorBlendInfo.blendConstants(3, 0); // Optional

        configInfo.depthStencilInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
        configInfo.depthStencilInfo.depthTestEnable(depthTest);
        configInfo.depthStencilInfo.depthWriteEnable(true);
        configInfo.depthStencilInfo.depthCompareOp(VK_COMPARE_OP_LESS);
        configInfo.depthStencilInfo.depthBoundsTestEnable(false);
        configInfo.depthStencilInfo.minDepthBounds(0.0f); // Optional
        configInfo.depthStencilInfo.maxDepthBounds(1.0f); // Optional
        configInfo.depthStencilInfo.stencilTestEnable(false);
        VkStencilOpState frState = VkStencilOpState.calloc();
        configInfo.depthStencilInfo.front(frState); // Optional
        VkStencilOpState bkState = VkStencilOpState.calloc();
        configInfo.depthStencilInfo.back(bkState); // Optional

        configInfo.subpass = 0;
    }
    private VkViewport viewport;
    private VkRect2D scissor;
    private VkPipelineInputAssemblyStateCreateInfo inputAssemblyInfo;
    private VkPipelineRasterizationStateCreateInfo rasterizationInfo;
    private VkPipelineMultisampleStateCreateInfo multisampleInfo;
    private VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachments;
    private VkPipelineColorBlendStateCreateInfo colorBlendInfo;
    private VkPipelineDepthStencilStateCreateInfo depthStencilInfo;

    private long subpass;
    public PipelineConfigInfo(){
        viewport = VkViewport.calloc();
        scissor = VkRect2D.calloc();
        scissor.extent(VkExtent2D.calloc());
        inputAssemblyInfo = VkPipelineInputAssemblyStateCreateInfo.calloc();
        rasterizationInfo = VkPipelineRasterizationStateCreateInfo.calloc();
        multisampleInfo = VkPipelineMultisampleStateCreateInfo.calloc();
        colorBlendInfo = VkPipelineColorBlendStateCreateInfo.calloc();
        depthStencilInfo = VkPipelineDepthStencilStateCreateInfo.calloc();
    }
}
