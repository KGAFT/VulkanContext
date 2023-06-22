
package com.kgaft.VulkanContext;

public abstract class DestroyableObject {
    protected boolean destroyed = false;
    
    public void destroy(){
        this.destroyed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        if(!destroyed){
            destroy();
        }
        super.finalize();
        
    }
    
}
