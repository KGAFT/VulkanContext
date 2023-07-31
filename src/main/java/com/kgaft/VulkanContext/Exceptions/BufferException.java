package com.kgaft.VulkanContext.Exceptions;

public class BufferException extends Throwable{
   public BufferException(String message) {
        this.message = message;
    }

    private String message;

    @Override
    public String getMessage() {
        return message;
    }
}
