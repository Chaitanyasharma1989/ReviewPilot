package com.csharma.reviewpilot.exception;

public class ProviderException extends ReviewPilotException {
    public ProviderException(String message) { super(message); }
    public ProviderException(String message, Throwable cause) { super(message, cause); }
} 