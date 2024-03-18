package com.pyre.auth.exception.customexception;

public class PermissionFailException extends RuntimeException{
    public PermissionFailException(String message) {
        super(message);
    }
}
