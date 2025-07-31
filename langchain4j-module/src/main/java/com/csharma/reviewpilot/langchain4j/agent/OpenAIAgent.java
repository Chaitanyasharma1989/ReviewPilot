package com.csharma.reviewpilot.langchain4j.agent;

import com.csharma.reviewpilot.langchain4j.model.CodeReviewResult;
import com.csharma.reviewpilot.langchain4j.model.PullRequestDetails;
import com.csharma.reviewpilot.langchain4j.model.ReviewConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
// Memory functionality will be added later when dependencies are available
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI agent implementation using LangChain4j
 */
public class OpenAIAgent implements CodeReviewAgent {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIAgent.class);
    
    private final ReviewConfig config;
    private final ChatLanguageModel chatModel;
    // private final ChatMemory chatMemory; // Will be enabled when memory dependency is available

    public OpenAIAgent(ReviewConfig config) {
        this.config = config;
        
        // Initialize OpenAI chat model
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .timeout(Duration.ofSeconds(60))
                .build();
        
        // Initialize chat memory if enabled
        // if (config.getEnableMemory()) {
        //     this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // } else {
        //     this.chatMemory = null;
        // }
        
        logger.info("Initialized OpenAI agent with model: {}", config.getModelName());
    }

    @Override
    public CodeReviewResult reviewPullRequest(PullRequestDetails prDetails) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting code review for PR: {}", prDetails.getTitle());
        
        try {
            // Create review prompt
            String reviewPrompt = createReviewPrompt(prDetails);
            
            // Prepare messages
            List<ChatMessage> messages = new ArrayList<>();
            
            // Add system message
            messages.add(new SystemMessage("You are an expert code reviewer. Provide comprehensive, actionable feedback."));
            
            // Add user message with review prompt
            messages.add(new UserMessage(reviewPrompt));
            
            // Generate review
            Response<AiMessage> response = chatModel.generate(messages);
            String reviewSummary = response.content().text();
            
            // Calculate review time
            long reviewTime = System.currentTimeMillis() - startTime;
            
            // Create result
            CodeReviewResult result = new CodeReviewResult(reviewSummary);
            result.setReviewTime((double) reviewTime / 1000.0); // Convert to seconds
            result.setModelUsed(config.getModelName());
            result.setTokensUsed(estimateTokens(reviewSummary));
            result.setConfidenceScore(calculateConfidenceScore(reviewSummary));
            
            // Perform additional analyses if enabled
            if (config.getIncludeSecurityAnalysis()) {
                result.setSecurityConcerns(analyzeSecurity(prDetails));
            }
            
            if (config.getIncludePerformanceAnalysis()) {
                result.setPerformanceIssues(analyzePerformance(prDetails));
            }
            
            if (config.getIncludeCodeQualityAnalysis()) {
                result.setIssues(analyzeCodeQuality(prDetails));
                result.setSuggestions(generateSuggestions(prDetails));
                result.setCodeQualityScore(calculateQualityScore(prDetails));
            }
            
            logger.info("Completed code review in {} seconds", result.getReviewTime());
            return result;
            
        } catch (Exception e) {
            logger.error("Error during code review: {}", e.getMessage());
            throw new RuntimeException("Failed to review pull request: " + e.getMessage(), e);
        }
    }

    private String createReviewPrompt(PullRequestDetails prDetails) {
        if (config.getCustomPrompt() != null && !config.getCustomPrompt().isEmpty()) {
            return config.getCustomPrompt()
                    .replace("{{title}}", prDetails.getTitle())
                    .replace("{{description}}", prDetails.getDescription())
                    .replace("{{changedFiles}}", prDetails.getChangedFiles().toString())
                    .replace("{{diff}}", prDetails.getDiff());
        }
        
        return String.format("""
            Please review the following pull request and provide comprehensive feedback:
            
            Title: %s
            Description: %s
            Changed Files: %s
            Diff: %s
            
            Please provide:
            1. Overall assessment of the changes
            2. Code quality analysis
            3. Potential issues and concerns
            4. Suggestions for improvement
            5. Security considerations
            6. Performance implications
            
            Be specific and actionable in your feedback.
            """, 
            prDetails.getTitle(),
            prDetails.getDescription(),
            prDetails.getChangedFiles(),
            prDetails.getDiff()
        );
    }

    private List<String> analyzeSecurity(PullRequestDetails prDetails) {
        String securityPrompt = String.format("""
            Analyze the following code changes for security vulnerabilities:
            
            Title: %s
            Description: %s
            Changed Files: %s
            Diff: %s
            
            Focus on:
            1. SQL injection vulnerabilities
            2. XSS vulnerabilities
            3. Authentication/authorization issues
            4. Input validation problems
            5. Sensitive data exposure
            6. Insecure dependencies
            
            Provide specific security concerns found:
            """, 
            prDetails.getTitle(),
            prDetails.getDescription(),
            prDetails.getChangedFiles(),
            prDetails.getDiff()
        );
        
        try {
            Response<AiMessage> response = chatModel.generate(new UserMessage(securityPrompt));
            String result = response.content().text();
            return parseListFromResponse(result);
        } catch (Exception e) {
            logger.warn("Error analyzing security: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> analyzePerformance(PullRequestDetails prDetails) {
        String performancePrompt = String.format("""
            Analyze the following code changes for performance issues:
            
            Title: %s
            Description: %s
            Changed Files: %s
            Diff: %s
            
            Focus on:
            1. Algorithm efficiency
            2. Database query optimization
            3. Memory usage
            4. Network calls optimization
            5. Caching opportunities
            6. Resource leaks
            
            Provide specific performance issues found:
            """, 
            prDetails.getTitle(),
            prDetails.getDescription(),
            prDetails.getChangedFiles(),
            prDetails.getDiff()
        );
        
        try {
            Response<AiMessage> response = chatModel.generate(new UserMessage(performancePrompt));
            String result = response.content().text();
            return parseListFromResponse(result);
        } catch (Exception e) {
            logger.warn("Error analyzing performance: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> analyzeCodeQuality(PullRequestDetails prDetails) {
        // Simplified implementation - in a real scenario, you'd do more detailed analysis
        List<Map<String, Object>> issues = new ArrayList<>();
        
        // Example issues
        if (prDetails.getChangedFiles().size() > 10) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("type", "complexity");
            issue.put("message", "Too many files changed in single PR");
            issue.put("severity", "medium");
            issues.add(issue);
        }
        
        return issues;
    }

    private List<String> generateSuggestions(PullRequestDetails prDetails) {
        // Simplified implementation
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Consider adding unit tests for the changes");
        suggestions.add("Review the code for potential edge cases");
        return suggestions;
    }

    private Double calculateQualityScore(PullRequestDetails prDetails) {
        // Simplified scoring algorithm
        double score = 80.0; // Base score
        
        // Adjust based on number of files changed
        if (prDetails.getChangedFiles().size() > 10) {
            score -= 10;
        }
        
        // Adjust based on description quality
        if (prDetails.getDescription().length() < 50) {
            score -= 5;
        }
        
        return Math.max(0.0, Math.min(100.0, score));
    }

    private Double calculateConfidenceScore(String reviewSummary) {
        // Simple heuristic based on response length and structure
        if (reviewSummary.length() < 100) {
            return 30.0;
        } else if (reviewSummary.length() < 500) {
            return 60.0;
        } else {
            return 90.0;
        }
    }

    private Integer estimateTokens(String text) {
        // Rough estimation: 1 token ≈ 4 characters
        return text.length() / 4;
    }

    private List<String> parseListFromResponse(String response) {
        List<String> items = new ArrayList<>();
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") || line.startsWith("•") || line.startsWith("*")) {
                items.add(line.substring(1).trim());
            } else if (line.matches("\\d+\\..*")) {
                items.add(line.replaceFirst("\\d+\\.", "").trim());
            }
        }
        
        return items;
    }

    @Override
    public String getAgentName() {
        return "openai";
    }
} 