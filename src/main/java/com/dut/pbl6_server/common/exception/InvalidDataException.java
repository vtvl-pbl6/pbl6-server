package com.dut.pbl6_server.common.exception;

public class InvalidDataException extends RuntimeException {
    public String resource;
    public String fieldName;

    public InvalidDataException(String resource, String fieldName, String message) {
        super(message);
        this.resource = resource;
        this.fieldName = fieldName;
    }
}
