package com.kgaft.VulkanContext.Exceptions;

public class NotSupportedLayerException extends Throwable {
    public NotSupportedLayerException(String message) {
        this.message = message;
    }
    private String message;
    @Override
    public String getMessage() {
        return message;
    }
}
