package com.dgx.exception;

public class CourseEntryNotFoundException extends RuntimeException {
    public CourseEntryNotFoundException(String message) {
        super(message);
    }
}
