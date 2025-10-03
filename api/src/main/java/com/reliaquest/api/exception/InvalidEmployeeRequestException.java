package com.reliaquest.api.exception;

public class InvalidEmployeeRequestException extends EmployeeApiException {
    public InvalidEmployeeRequestException(String message) {
        super("Invalid request: " + message);
    }
}
