package com.csharma.reviewpilot.langchain4j.provider;

import com.csharma.reviewpilot.langchain4j.model.PullRequestDetails;

/**
 * Interface for pull request providers
 */
public interface PullRequestProvider {
    
    /**
     * Fetch pull request details from the provider
     * 
     * @param repoOwner repository owner
     * @param repoName repository name
     * @param prNumber pull request number
     * @return pull request details
     * @throws Exception if there's an error fetching the details
     */
    PullRequestDetails fetchPullRequestDetails(String repoOwner, String repoName, int prNumber) throws Exception;
    
    /**
     * Get the provider name
     * 
     * @return provider name
     */
    String getProviderName();
} 