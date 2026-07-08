package com.example.demo.exception;

public class AppointmentLimitExceededException extends RuntimeException {
    public AppointmentLimitExceededException(String message) {
        super(message);
    }
}
