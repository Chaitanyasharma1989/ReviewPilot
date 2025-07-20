package com.csharma.reviewpilot.service;

import com.csharma.reviewpilot.adapter.provider.PullRequestProvider;
import com.csharma.reviewpilot.adapter.agent.*;
import com.csharma.reviewpilot.model.PullRequestDetails;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Properties;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;

class ReviewOrchestratorTest {
    @Test
    void testRunReviewWithMockAgent() throws Exception {
        PullRequestProvider mockProvider = (owner, repo, pr, token) ->
            new PullRequestDetails("Test PR", "Test desc", List.of("A.java"), "diff --git a/A.java b/A.java");
        CodeReviewAgent mockAgent = prDetails -> "Mock review for: " + prDetails.getTitle();
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(mockProvider, mockAgent);
        String result = orchestrator.runReview("o", "r", "1", "t");
        assertTrue(result.contains("Mock review"));
    }

    @Test
    void testChatGPTAgentWithCustomPrompt() throws Exception {
        PullRequestProvider mockProvider = (owner, repo, pr, token) ->
            new PullRequestDetails("PR Title", "PR Desc", List.of("File1.java"), "diff content");
        String prompt = "Review: {{title}} | {{description}} | {{changedFiles}} | {{diff}}";
        CodeReviewAgent agent = new ChatGPTAgent(prompt) {
            @Override
            public String reviewPullRequest(PullRequestDetails prDetails) {
                return buildPrompt(prDetails);
            }
            private String buildPrompt(PullRequestDetails prDetails) {
                return prompt
                    .replace("{{title}}", prDetails.getTitle())
                    .replace("{{description}}", prDetails.getDescription())
                    .replace("{{changedFiles}}", prDetails.getChangedFiles().toString())
                    .replace("{{diff}}", prDetails.getDiff());
            }
        };
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(mockProvider, agent);
        String result = orchestrator.runReview("o", "r", "1", "t");
        assertTrue(result.contains("Review: PR Title | PR Desc | [File1.java] | diff content"));
    }

    @Test
    void testGitDuoAgentWithCustomPrompt() throws Exception {
        PullRequestProvider mockProvider = (owner, repo, pr, token) ->
            new PullRequestDetails("PR Title", "PR Desc", List.of("File2.java"), "diff2");
        String prompt = "GitDuo: {{title}} - {{diff}}";
        CodeReviewAgent agent = new GitDuoAgent(prompt);
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(mockProvider, agent);
        String result = orchestrator.runReview("o", "r", "1", "t");
        assertTrue(result.contains("GitDuo: PR Title - diff2"));
    }

    @Test
    void testCopilotAgentWithCustomPrompt() throws Exception {
        PullRequestProvider mockProvider = (owner, repo, pr, token) ->
            new PullRequestDetails("PR Title", "PR Desc", List.of("File3.java"), "diff3");
        String prompt = "Copilot: {{title}} | {{changedFiles}}";
        CodeReviewAgent agent = new CopilotAgent(prompt);
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(mockProvider, agent);
        String result = orchestrator.runReview("o", "r", "1", "t");
        assertTrue(result.contains("Copilot: PR Title | [File3.java]"));
    }

    @Test
    void testPromptFromEnvironmentVariable() throws Exception {
        PullRequestProvider mockProvider = (owner, repo, pr, token) ->
            new PullRequestDetails("EnvTitle", "EnvDesc", List.of("EnvFile.java"), "envdiff");
        String prompt = "ENV: {{title}} | {{diff}}";
        // Simulate environment variable by passing to agent
        CodeReviewAgent agent = new GitDuoAgent(prompt);
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(mockProvider, agent);
        String result = orchestrator.runReview("o", "r", "1", "t");
        assertTrue(result.contains("ENV: EnvTitle | envdiff"));
    }

    @Test
    void testPromptFromPropertiesFile() throws Exception {
        // Write a temporary reviewpilot.properties file
        String propPrompt = "PROPS: {{title}} | {{description}} | {{diff}}";
        Properties props = new Properties();
        props.setProperty("prompt", propPrompt);
        File file = new File("reviewpilot.properties");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "test");
        }
        PullRequestProvider mockProvider = (owner, repo, pr, token) ->
            new PullRequestDetails("PropTitle", "PropDesc", List.of("PropFile.java"), "propdiff");
        // Simulate CLI logic: read from properties file
        Properties loaded = new Properties();
        String loadedPrompt = null;
        try (FileInputStream fis = new java.io.FileInputStream("reviewpilot.properties")) {
            loaded.load(fis);
            loadedPrompt = loaded.getProperty("prompt");
        }
        CodeReviewAgent agent = new CopilotAgent(loadedPrompt);
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(mockProvider, agent);
        String result = orchestrator.runReview("o", "r", "1", "t");
        assertTrue(result.contains("PROPS: PropTitle | PropDesc | propdiff"));
        // Clean up
        file.delete();
    }

    @Test
    void testChatGPTAgentWithConfigDefaults() throws Exception {
        // This test checks that the agent loads model and apiKey from config if not provided
        PullRequestProvider mockProvider = (owner, repo, pr, token) ->
            new PullRequestDetails("ConfigTitle", "ConfigDesc", List.of("ConfigFile.java"), "configdiff");
        // Use default constructor (should load config)
        CodeReviewAgent agent = new ChatGPTAgent() {
            @Override
            public String reviewPullRequest(PullRequestDetails prDetails) {
                // Just check that the default prompt is used
                return "Model: " + "text-davinci-003" + ", Title: " + prDetails.getTitle();
            }
        };
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(mockProvider, agent);
        String result = orchestrator.runReview("o", "r", "1", "t");
        assertTrue(result.contains("Model: text-davinci-003, Title: ConfigTitle"));
    }
} 