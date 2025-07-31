package com.csharma.reviewpilot.langchain4j.service;

import com.csharma.reviewpilot.langchain4j.agent.CodeReviewAgent;
import com.csharma.reviewpilot.langchain4j.agent.OpenAIAgent;
import com.csharma.reviewpilot.langchain4j.model.CodeReviewResult;
import com.csharma.reviewpilot.langchain4j.model.PullRequestDetails;
import com.csharma.reviewpilot.langchain4j.model.ReviewConfig;
import com.csharma.reviewpilot.langchain4j.provider.GitHubProvider;
import com.csharma.reviewpilot.langchain4j.provider.PullRequestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Main orchestrator for coordinating providers and agents
 */
public class ReviewOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(ReviewOrchestrator.class);
    
    private final ReviewConfig config;
    private final PullRequestProvider provider;
    private final CodeReviewAgent agent;

    public ReviewOrchestrator(ReviewConfig config) throws Exception {
        this.config = config;
        this.provider = createProvider();
        this.agent = createAgent();
        
        logger.info("Initialized ReviewOrchestrator with provider: {} and agent: {}", 
                   provider.getProviderName(), agent.getAgentName());
    }

    /**
     * Run a complete code review
     * 
     * @param repoOwner repository owner
     * @param repoName repository name
     * @param prNumber pull request number
     * @return review result
     * @throws Exception if there's an error during the review
     */
    public CodeReviewResult runReview(String repoOwner, String repoName, int prNumber) throws Exception {
        logger.info("Starting review for PR #{} in {}/{}", prNumber, repoOwner, repoName);
        
        // Fetch pull request details
        PullRequestDetails prDetails = provider.fetchPullRequestDetails(repoOwner, repoName, prNumber);
        logger.info("Fetched PR details: {}", prDetails.getTitle());
        
        // Run the review
        CodeReviewResult result = agent.reviewPullRequest(prDetails);
        logger.info("Completed review with score: {}", result.getCodeQualityScore());
        
        return result;
    }

    private PullRequestProvider createProvider() throws Exception {
        String authToken = getAuthToken();
        
        switch (config.getProvider().toLowerCase()) {
            case "github":
                return new GitHubProvider(authToken);
            case "gitlab":
                // TODO: Implement GitLab provider
                throw new UnsupportedOperationException("GitLab provider not yet implemented");
            case "bitbucket":
                // TODO: Implement Bitbucket provider
                throw new UnsupportedOperationException("Bitbucket provider not yet implemented");
            default:
                throw new IllegalArgumentException("Unsupported provider: " + config.getProvider());
        }
    }

    private CodeReviewAgent createAgent() {
        switch (config.getAgent().toLowerCase()) {
            case "openai":
                return new OpenAIAgent(config);
            case "anthropic":
                // TODO: Implement Anthropic agent
                throw new UnsupportedOperationException("Anthropic agent not yet implemented");
            case "cohere":
                // TODO: Implement Cohere agent
                throw new UnsupportedOperationException("Cohere agent not yet implemented");
            default:
                throw new IllegalArgumentException("Unsupported agent: " + config.getAgent());
        }
    }

    private String getAuthToken() {
        Map<String, String> tokenMapping = Map.of(
            "github", "GITHUB_TOKEN",
            "gitlab", "GITLAB_TOKEN",
            "bitbucket", "BITBUCKET_TOKEN"
        );
        
        String envVar = tokenMapping.get(config.getProvider().toLowerCase());
        if (envVar == null) {
            throw new IllegalArgumentException("Unknown provider: " + config.getProvider());
        }
        
        String token = System.getenv(envVar);
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Authentication token not found for provider " + 
                                          config.getProvider() + ". Please set " + envVar + " environment variable.");
        }
        
        return token;
    }

    public ReviewConfig getConfig() {
        return config;
    }

    public PullRequestProvider getProvider() {
        return provider;
    }

    public CodeReviewAgent getAgent() {
        return agent;
    }
} 