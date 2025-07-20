package com.csharma.reviewpilot.service;

import com.csharma.reviewpilot.adapter.provider.PullRequestProvider;
import com.csharma.reviewpilot.adapter.agent.CodeReviewAgent;
import com.csharma.reviewpilot.model.PullRequestDetails;

public class ReviewOrchestrator {
    private final PullRequestProvider prProvider;
    private final CodeReviewAgent reviewAgent;

    public ReviewOrchestrator(PullRequestProvider prProvider, CodeReviewAgent reviewAgent) {
        this.prProvider = prProvider;
        this.reviewAgent = reviewAgent;
    }

    public String runReview(String repoOwner, String repoName, String prNumber, String authToken) throws Exception {
        PullRequestDetails prDetails = prProvider.fetchPullRequestDetails(repoOwner, repoName, prNumber, authToken);
        return reviewAgent.reviewPullRequest(prDetails);
    }
} 