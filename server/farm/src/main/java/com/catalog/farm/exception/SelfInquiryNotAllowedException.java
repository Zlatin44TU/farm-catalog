package com.catalog.farm.exception;

public class SelfInquiryNotAllowedException extends RuntimeException {
    public SelfInquiryNotAllowedException(String message) {
        super(message);
    }
}
