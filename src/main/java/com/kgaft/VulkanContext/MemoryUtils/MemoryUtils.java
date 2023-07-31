package com.kgaft.VulkanContext.MemoryUtils;

import java.nio.ByteBuffer;

public class MemoryUtils {
    public static void memcpy(ByteBuffer dst, ByteBuffer src, int size){
        src.limit(size);
        dst.put(src);
        src.limit(src.capacity()).rewind();
    }
}
