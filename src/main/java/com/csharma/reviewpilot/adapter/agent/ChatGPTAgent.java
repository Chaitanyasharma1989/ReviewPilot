package com.csharma.reviewpilot.adapter.agent;

import com.csharma.reviewpilot.model.PullRequestDetails;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionChoice;
import java.util.List;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.csharma.reviewpilot.exception.AgentException;

public class ChatGPTAgent implements CodeReviewAgent {
    private final OpenAiService openAiService;
    private final String model;
    private final String promptTemplate;

    private static final String DEFAULT_PROMPT =
        "You are an expert code reviewer. Review the following pull request and provide consolidated review comments for the developer to improve code quality.\n" +
        "Title: {{title}}\n" +
        "Description: {{description}}\n" +
        "Changed Files: {{changedFiles}}\n" +
        "Diff: {{diff}}\n";

    public ChatGPTAgent() {
        this(null);
    }

    public ChatGPTAgent(String promptTemplate) {
        Config config = ConfigFactory.load();
        String apiKey = config.hasPath("reviewpilot.openai.api-key") ? config.getString("reviewpilot.openai.api-key") : System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new AgentException("OPENAI_API_KEY not set in config or environment");
        }
        this.openAiService = new OpenAiService(apiKey);
        this.model = config.hasPath("reviewpilot.openai.model") ? config.getString("reviewpilot.openai.model") : "text-davinci-003";
        this.promptTemplate = (promptTemplate == null || promptTemplate.isBlank()) ? DEFAULT_PROMPT : promptTemplate;
    }

    @Override
    public String reviewPullRequest(PullRequestDetails prDetails) {
        String prompt = buildPrompt(prDetails);
        CompletionRequest request = CompletionRequest.builder()
                .prompt(prompt)
                .model(model)
                .maxTokens(512)
                .temperature(0.2)
                .build();
        List<CompletionChoice> choices = openAiService.createCompletion(request).getChoices();
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).getText().trim();
        }
        return "[ChatGPT] No review comments returned.";
    }

    private String buildPrompt(PullRequestDetails prDetails) {
        return promptTemplate
            .replace("{{title}}", prDetails.getTitle())
            .replace("{{description}}", prDetails.getDescription())
            .replace("{{changedFiles}}", prDetails.getChangedFiles().toString())
            .replace("{{diff}}", prDetails.getDiff());
    }
} 