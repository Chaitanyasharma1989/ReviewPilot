package com.csharma.reviewpilot.adapter.agent;

import com.csharma.reviewpilot.model.PullRequestDetails;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.csharma.reviewpilot.exception.AgentException;

public class GitDuoAgent implements CodeReviewAgent {
    private final String apiKey;
    private final String apiUrl;
    private final String promptTemplate;

    private static final String DEFAULT_PROMPT =
        "[GitDuo] Review: Please refactor the code for better readability.\n" +
        "Title: {{title}}\n" +
        "Description: {{description}}\n" +
        "Changed Files: {{changedFiles}}\n" +
        "Diff: {{diff}}\n";

    public GitDuoAgent() {
        this(null);
    }

    public GitDuoAgent(String promptTemplate) {
        Config config = ConfigFactory.load();
        this.apiKey = config.hasPath("reviewpilot.gitduo.api-key") ? config.getString("reviewpilot.gitduo.api-key") : System.getenv("GITDUO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new AgentException("GITDUO_API_KEY not set in config or environment");
        }
        this.apiUrl = config.getString("reviewpilot.gitduo.api-url");
        this.promptTemplate = (promptTemplate == null || promptTemplate.isBlank()) ? DEFAULT_PROMPT : promptTemplate;
    }

    @Override
    public String reviewPullRequest(PullRequestDetails prDetails) {
        // TODO: Implement HTTP POST to GitDuo API with prDetails and prompt
        // For now, return the prompt with placeholders replaced
        return promptTemplate
            .replace("{{title}}", prDetails.getTitle())
            .replace("{{description}}", prDetails.getDescription())
            .replace("{{changedFiles}}", prDetails.getChangedFiles().toString())
            .replace("{{diff}}", prDetails.getDiff());
    }
} 