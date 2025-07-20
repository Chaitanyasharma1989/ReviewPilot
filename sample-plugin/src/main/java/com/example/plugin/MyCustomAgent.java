package com.example.plugin;

import com.csharma.reviewpilot.adapter.agent.CodeReviewAgent;
import com.csharma.reviewpilot.model.PullRequestDetails;

public class MyCustomAgent implements CodeReviewAgent {
    @Override
    public String reviewPullRequest(PullRequestDetails prDetails) {
        return "[MyCustomAgent] Review for: " + prDetails.getTitle();
    }
} 