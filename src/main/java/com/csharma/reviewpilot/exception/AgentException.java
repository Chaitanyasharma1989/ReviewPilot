package com.csharma.reviewpilot.exception;

public class AgentException extends ReviewPilotException {
    public AgentException(String message) { super(message); }
    public AgentException(String message, Throwable cause) { super(message, cause); }
} 