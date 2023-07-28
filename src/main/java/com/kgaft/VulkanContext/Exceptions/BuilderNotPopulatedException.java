package com.kgaft.VulkanContext.Exceptions;

public class BuilderNotPopulatedException extends Throwable {
    public BuilderNotPopulatedException(String message) {
        this.message = message;
    }

    private String message;

    @Override
    public String getMessage() {
        return message;
    }
}
