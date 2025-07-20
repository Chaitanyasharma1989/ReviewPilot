package com.csharma.reviewpilot.model;

import java.util.List;

public class PullRequestDetails {
    private final String title;
    private final String description;
    private final List<String> changedFiles;
    private final String diff;

    public PullRequestDetails(String title, String description, List<String> changedFiles, String diff) {
        this.title = title;
        this.description = description;
        this.changedFiles = changedFiles;
        this.diff = diff;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getChangedFiles() { return changedFiles; }
    public String getDiff() { return diff; }
} 