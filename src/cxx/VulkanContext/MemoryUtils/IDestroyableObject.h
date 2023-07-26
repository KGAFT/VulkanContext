//
// Created by kgaft on 7/26/23.
//

#pragma once

class IDestroyableObject{
protected:
    bool destroyed = false;
public:
    virtual void destroy(){

    }
    ~IDestroyableObject(){
        if(!destroyed){
            destroy();
        }
    }
};