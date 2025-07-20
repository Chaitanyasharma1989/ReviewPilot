package com.csharma.reviewpilot.adapter.agent;

import com.csharma.reviewpilot.model.PullRequestDetails;

public interface CodeReviewAgent {
    String reviewPullRequest(PullRequestDetails prDetails) throws Exception;
} 