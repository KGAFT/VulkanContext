package com.kgaft.VulkanContext.MemoryUtils;

import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryStackUtils {
    private static HashMap<MemoryStack, Boolean> allocatedMemoryStacks = new HashMap<>();

    public static MemoryStack acquireStack(){
        for (Map.Entry<MemoryStack, Boolean> entry : allocatedMemoryStacks.entrySet()) {
            if(!entry.getValue()){
                entry.setValue(true);
                return entry.getKey();
            }
        }
        return MemoryStack.create();
    }

    public static void freeStack(MemoryStack stack){
        stack.setPointer(0);
        allocatedMemoryStacks.replace(stack, false);
        checkGarbageStacks();
    }

    private static void checkGarbageStacks(){
        int garbageCount = 0;
        for (Map.Entry<MemoryStack, Boolean> entry : allocatedMemoryStacks.entrySet()) {
           if(!entry.getValue()){
               garbageCount++;
           }
           if(garbageCount>=3){
               break;
           }
        }
        if(garbageCount>=3){
            List<MemoryStack> removed = new ArrayList<>();
            for (Map.Entry<MemoryStack, Boolean> entry : allocatedMemoryStacks.entrySet()) {
                if(removed.size()==2){
                    break;
                }
                if(!entry.getValue()){
                    entry.getKey().close();
                    allocatedMemoryStacks.remove(entry.getKey());
                    removed.add(entry.getKey());
                }
            }
            removed.forEach(el->{
                
                allocatedMemoryStacks.remove(el);
            });
        }
    }
}
