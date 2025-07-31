package com.csharma.reviewpilot.langchain4j.agent;

import com.csharma.reviewpilot.langchain4j.model.CodeReviewResult;
import com.csharma.reviewpilot.langchain4j.model.PullRequestDetails;

/**
 * Interface for code review agents
 */
public interface CodeReviewAgent {
    
    /**
     * Review a pull request and return the results
     * 
     * @param prDetails pull request details
     * @return code review result
     */
    CodeReviewResult reviewPullRequest(PullRequestDetails prDetails);
    
    /**
     * Get the agent name
     * 
     * @return agent name
     */
    String getAgentName();
} 