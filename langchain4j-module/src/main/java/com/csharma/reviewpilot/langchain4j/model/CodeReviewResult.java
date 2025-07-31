package com.csharma.reviewpilot.langchain4j.model;

import java.util.List;
import java.util.Map;

/**
 * Result of a code review
 */
public class CodeReviewResult {
    private String summary;
    private List<Map<String, Object>> issues;
    private List<String> suggestions;
    private List<String> securityConcerns;
    private List<String> performanceIssues;
    private Double codeQualityScore;
    private Double confidenceScore;
    private Double reviewTime;
    private Integer tokensUsed;
    private String modelUsed;

    public CodeReviewResult() {}

    public CodeReviewResult(String summary) {
        this.summary = summary;
    }

    // Getters and Setters
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<Map<String, Object>> getIssues() { return issues; }
    public void setIssues(List<Map<String, Object>> issues) { this.issues = issues; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public List<String> getSecurityConcerns() { return securityConcerns; }
    public void setSecurityConcerns(List<String> securityConcerns) { this.securityConcerns = securityConcerns; }

    public List<String> getPerformanceIssues() { return performanceIssues; }
    public void setPerformanceIssues(List<String> performanceIssues) { this.performanceIssues = performanceIssues; }

    public Double getCodeQualityScore() { return codeQualityScore; }
    public void setCodeQualityScore(Double codeQualityScore) { this.codeQualityScore = codeQualityScore; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public Double getReviewTime() { return reviewTime; }
    public void setReviewTime(Double reviewTime) { this.reviewTime = reviewTime; }

    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    @Override
    public String toString() {
        return "CodeReviewResult{" +
                "summary='" + summary + '\'' +
                ", codeQualityScore=" + codeQualityScore +
                ", confidenceScore=" + confidenceScore +
                ", reviewTime=" + reviewTime +
                ", tokensUsed=" + tokensUsed +
                ", modelUsed='" + modelUsed + '\'' +
                '}';
    }
} 