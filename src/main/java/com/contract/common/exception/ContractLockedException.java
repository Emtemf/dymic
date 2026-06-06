package com.contract.common.exception;

public class ContractLockedException extends BizException {
    public ContractLockedException(String message) {
        super("CONTRACT_LOCKED", message);
    }
}