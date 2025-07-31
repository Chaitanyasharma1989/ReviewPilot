package com.csharma.reviewpilot.langchain4j.model;

/**
 * Configuration for code review
 */
public class ReviewConfig {
    private String provider;
    private String agent;
    private String modelName;
    private Double temperature;
    private Integer maxTokens;
    private Boolean includeSecurityAnalysis;
    private Boolean includePerformanceAnalysis;
    private Boolean includeCodeQualityAnalysis;
    private String customPrompt;
    private Boolean enableMemory;
    private Boolean enableVectorSearch;
    private Integer chunkSize;
    private Integer chunkOverlap;

    public ReviewConfig() {
        // Default values
        this.modelName = "gpt-4";
        this.temperature = 0.1;
        this.maxTokens = 4000;
        this.includeSecurityAnalysis = true;
        this.includePerformanceAnalysis = true;
        this.includeCodeQualityAnalysis = true;
        this.enableMemory = true;
        this.enableVectorSearch = true;
        this.chunkSize = 2000;
        this.chunkOverlap = 200;
    }

    // Getters and Setters
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getAgent() { return agent; }
    public void setAgent(String agent) { this.agent = agent; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Boolean getIncludeSecurityAnalysis() { return includeSecurityAnalysis; }
    public void setIncludeSecurityAnalysis(Boolean includeSecurityAnalysis) { this.includeSecurityAnalysis = includeSecurityAnalysis; }

    public Boolean getIncludePerformanceAnalysis() { return includePerformanceAnalysis; }
    public void setIncludePerformanceAnalysis(Boolean includePerformanceAnalysis) { this.includePerformanceAnalysis = includePerformanceAnalysis; }

    public Boolean getIncludeCodeQualityAnalysis() { return includeCodeQualityAnalysis; }
    public void setIncludeCodeQualityAnalysis(Boolean includeCodeQualityAnalysis) { this.includeCodeQualityAnalysis = includeCodeQualityAnalysis; }

    public String getCustomPrompt() { return customPrompt; }
    public void setCustomPrompt(String customPrompt) { this.customPrompt = customPrompt; }

    public Boolean getEnableMemory() { return enableMemory; }
    public void setEnableMemory(Boolean enableMemory) { this.enableMemory = enableMemory; }

    public Boolean getEnableVectorSearch() { return enableVectorSearch; }
    public void setEnableVectorSearch(Boolean enableVectorSearch) { this.enableVectorSearch = enableVectorSearch; }

    public Integer getChunkSize() { return chunkSize; }
    public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }

    public Integer getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(Integer chunkOverlap) { this.chunkOverlap = chunkOverlap; }

    @Override
    public String toString() {
        return "ReviewConfig{" +
                "provider='" + provider + '\'' +
                ", agent='" + agent + '\'' +
                ", modelName='" + modelName + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", includeSecurityAnalysis=" + includeSecurityAnalysis +
                ", includePerformanceAnalysis=" + includePerformanceAnalysis +
                ", includeCodeQualityAnalysis=" + includeCodeQualityAnalysis +
                ", enableMemory=" + enableMemory +
                ", enableVectorSearch=" + enableVectorSearch +
                '}';
    }
} 