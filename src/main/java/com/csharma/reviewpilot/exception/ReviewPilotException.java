package com.csharma.reviewpilot.exception;

public class ReviewPilotException extends RuntimeException {
    public ReviewPilotException(String message) { super(message); }
    public ReviewPilotException(String message, Throwable cause) { super(message, cause); }
} 