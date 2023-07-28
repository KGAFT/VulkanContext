package com.kgaft.VulkanContext.Exceptions;

public class NotSupportedExtensionException extends Throwable {
    public NotSupportedExtensionException(String message) {
        this.message = message;
    }
    private String message;
    @Override
    public String getMessage() {
        return message;
    }
}
