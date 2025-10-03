package com.reliaquest.api.exception;

public class EmployeeNotFoundException extends EmployeeApiException {
    public EmployeeNotFoundException(String id) {
        super("Employee not found with ID: " + id);
    }
}
