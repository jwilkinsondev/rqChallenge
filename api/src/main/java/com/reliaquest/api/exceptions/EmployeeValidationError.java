package com.reliaquest.api.exceptions;

public class EmployeeValidationError extends RuntimeException {
    public EmployeeValidationError(String message) {
        super(message);
    }
}
