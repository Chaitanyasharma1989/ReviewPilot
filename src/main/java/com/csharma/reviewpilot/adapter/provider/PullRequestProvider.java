package com.csharma.reviewpilot.adapter.provider;

import com.csharma.reviewpilot.model.PullRequestDetails;

public interface PullRequestProvider {
    PullRequestDetails fetchPullRequestDetails(String repoOwner, String repoName, String prNumber, String authToken) throws Exception;
} 