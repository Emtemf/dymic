package com.contract.common.exception;

public class ConcurrentModificationException extends BizException {
    public ConcurrentModificationException(String message) {
        super("CONCURRENT_MODIFICATION", message);
    }
}