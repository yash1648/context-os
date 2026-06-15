package com.grim.contextos.common.exception;

public class ResourceNotFoundException extends BusinessException{
    public ResourceNotFoundException(String resource,Object id) {
        super("NOT_FOUND",resource+" not found with id: "+id);
    }
}
