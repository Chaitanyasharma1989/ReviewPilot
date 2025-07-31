package com.csharma.reviewpilot.langchain4j.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data model for pull request details
 */
public class PullRequestDetails {
    private String title;
    private String description;
    private List<String> changedFiles;
    private String diff;
    private int prNumber;
    private String repoOwner;
    private String repoName;
    private String baseBranch;
    private String headBranch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String author;
    private List<String> labels;
    private List<String> assignees;

    public PullRequestDetails() {}

    public PullRequestDetails(String title, String description, List<String> changedFiles, String diff,
                            int prNumber, String repoOwner, String repoName, String baseBranch, String headBranch) {
        this.title = title;
        this.description = description;
        this.changedFiles = changedFiles;
        this.diff = diff;
        this.prNumber = prNumber;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.baseBranch = baseBranch;
        this.headBranch = headBranch;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getChangedFiles() { return changedFiles; }
    public void setChangedFiles(List<String> changedFiles) { this.changedFiles = changedFiles; }

    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }

    public int getPrNumber() { return prNumber; }
    public void setPrNumber(int prNumber) { this.prNumber = prNumber; }

    public String getRepoOwner() { return repoOwner; }
    public void setRepoOwner(String repoOwner) { this.repoOwner = repoOwner; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getBaseBranch() { return baseBranch; }
    public void setBaseBranch(String baseBranch) { this.baseBranch = baseBranch; }

    public String getHeadBranch() { return headBranch; }
    public void setHeadBranch(String headBranch) { this.headBranch = headBranch; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<String> getAssignees() { return assignees; }
    public void setAssignees(List<String> assignees) { this.assignees = assignees; }

    @Override
    public String toString() {
        return "PullRequestDetails{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", changedFiles=" + changedFiles +
                ", prNumber=" + prNumber +
                ", repoOwner='" + repoOwner + '\'' +
                ", repoName='" + repoName + '\'' +
                ", baseBranch='" + baseBranch + '\'' +
                ", headBranch='" + headBranch + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
} 