package com.csharma.reviewpilot.langchain4j.cli;

import com.csharma.reviewpilot.langchain4j.model.CodeReviewResult;
import com.csharma.reviewpilot.langchain4j.model.ReviewConfig;
import com.csharma.reviewpilot.langchain4j.service.ReviewOrchestrator;
import com.github.freva.asciitable.AsciiTable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.Map;

/**
 * CLI interface for ReviewPilot LangChain4j
 */
@Command(
    name = "reviewpilot-langchain4j",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "ReviewPilot LangChain4j - AI-powered code review tool"
)
public class ReviewPilotCLI implements Runnable {

    @Command(name = "review", description = "Review a pull request using AI")
    static class ReviewCommand implements Runnable {
        
        @Option(names = {"--provider", "-p"}, description = "Git provider (github, gitlab, bitbucket)", defaultValue = "github")
        private String provider;
        
        @Option(names = {"--agent", "-a"}, description = "AI agent (openai, anthropic, cohere)", defaultValue = "openai")
        private String agent;
        
        @Option(names = {"--model", "-m"}, description = "LLM model name", defaultValue = "gpt-4")
        private String model;
        
        @Option(names = {"--temperature", "-t"}, description = "LLM temperature", defaultValue = "0.1")
        private Double temperature;
        
        @Option(names = {"--max-tokens"}, description = "Maximum tokens", defaultValue = "4000")
        private Integer maxTokens;
        
        @Option(names = {"--custom-prompt"}, description = "Custom review prompt")
        private String customPrompt;
        
        @Option(names = {"--no-security"}, description = "Skip security analysis")
        private Boolean noSecurity;
        
        @Option(names = {"--no-performance"}, description = "Skip performance analysis")
        private Boolean noPerformance;
        
        @Option(names = {"--no-quality"}, description = "Skip code quality analysis")
        private Boolean noQuality;
        
        @Option(names = {"--no-memory"}, description = "Disable conversation memory")
        private Boolean noMemory;
        
        @Option(names = {"--no-vector-search"}, description = "Disable vector search")
        private Boolean noVectorSearch;
        
        @Parameters(index = "0", description = "Repository owner")
        private String repoOwner;
        
        @Parameters(index = "1", description = "Repository name")
        private String repoName;
        
        @Parameters(index = "2", description = "Pull request number")
        private Integer prNumber;

        @Override
        public void run() {
            try {
                // Create configuration
                ReviewConfig config = new ReviewConfig();
                config.setProvider(provider);
                config.setAgent(agent);
                config.setModelName(model);
                config.setTemperature(temperature);
                config.setMaxTokens(maxTokens);
                config.setCustomPrompt(customPrompt);
                config.setIncludeSecurityAnalysis(noSecurity == null || !noSecurity);
                config.setIncludePerformanceAnalysis(noPerformance == null || !noPerformance);
                config.setIncludeCodeQualityAnalysis(noQuality == null || !noQuality);
                config.setEnableMemory(noMemory == null || !noMemory);
                config.setEnableVectorSearch(noVectorSearch == null || !noVectorSearch);
                
                System.out.println("🚀 ReviewPilot LangChain4j");
                System.out.println("=".repeat(50));
                System.out.printf("Provider: %s%n", provider);
                System.out.printf("Agent: %s%n", agent);
                System.out.printf("Model: %s%n", model);
                System.out.printf("Repository: %s/%s%n", repoOwner, repoName);
                System.out.printf("PR: #%d%n", prNumber);
                System.out.println();
                
                // Initialize orchestrator
                ReviewOrchestrator orchestrator = new ReviewOrchestrator(config);
                
                // Run review
                System.out.println("⏳ Starting review...");
                CodeReviewResult result = orchestrator.runReview(repoOwner, repoName, prNumber);
                
                // Display results
                displayResults(result);
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Command(name = "config", description = "Show current configuration")
    static class ConfigCommand implements Runnable {
        
        @Override
        public void run() {
            System.out.println("ReviewPilot LangChain4j Configuration");
            System.out.println("=".repeat(40));
            
            String[][] data = {
                {"GITHUB_TOKEN", maskToken(System.getenv("GITHUB_TOKEN"))},
                {"GITLAB_TOKEN", maskToken(System.getenv("GITLAB_TOKEN"))},
                {"BITBUCKET_TOKEN", maskToken(System.getenv("BITBUCKET_TOKEN"))},
                {"OPENAI_API_KEY", maskToken(System.getenv("OPENAI_API_KEY"))},
                {"ANTHROPIC_API_KEY", maskToken(System.getenv("ANTHROPIC_API_KEY"))},
                {"COHERE_API_KEY", maskToken(System.getenv("COHERE_API_KEY"))}
            };
            
            String table = AsciiTable.getTable(new String[]{"Setting", "Value"}, data);
            System.out.println(table);
        }
        
        private String maskToken(String token) {
            if (token == null || token.isEmpty()) {
                return "Not set";
            }
            return "***" + token.substring(Math.max(0, token.length() - 4));
        }
    }

    @Command(name = "version", description = "Show version information")
    static class VersionCommand implements Runnable {
        
        @Override
        public void run() {
            System.out.println("ReviewPilot LangChain4j v1.0.0");
            System.out.println("Built with LangChain4j v0.27.1");
            System.out.println("Java " + System.getProperty("java.version"));
        }
    }

    @Override
    public void run() {
        // Show help if no subcommand is provided
        CommandLine.usage(this, System.out);
    }

    private static void displayResults(CodeReviewResult result) {
        System.out.println("📊 Review Results");
        System.out.println("=".repeat(50));
        
        System.out.printf("📝 Summary: %s%n", result.getSummary());
        System.out.println();
        
        // Metrics table
        String[][] metricsData = {
            {"Code Quality Score", String.format("%.1f/100", result.getCodeQualityScore() != null ? result.getCodeQualityScore() : 0.0)},
            {"Confidence Score", String.format("%.1f/100", result.getConfidenceScore() != null ? result.getConfidenceScore() : 0.0)},
            {"Review Time", String.format("%.2fs", result.getReviewTime() != null ? result.getReviewTime() : 0.0)},
            {"Tokens Used", String.valueOf(result.getTokensUsed() != null ? result.getTokensUsed() : 0)},
            {"Model Used", result.getModelUsed() != null ? result.getModelUsed() : "Unknown"}
        };
        
        String metricsTable = AsciiTable.getTable(new String[]{"Metric", "Value"}, metricsData);
        System.out.println(metricsTable);
        System.out.println();
        
        // Issues
        if (result.getIssues() != null && !result.getIssues().isEmpty()) {
            System.out.println("❌ Issues Found:");
            for (Map<String, Object> issue : result.getIssues()) {
                System.out.printf("  • %s%n", issue.get("message"));
            }
            System.out.println();
        }
        
        // Suggestions
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            System.out.println("💡 Suggestions:");
            for (String suggestion : result.getSuggestions()) {
                System.out.printf("  • %s%n", suggestion);
            }
            System.out.println();
        }
        
        // Security concerns
        if (result.getSecurityConcerns() != null && !result.getSecurityConcerns().isEmpty()) {
            System.out.println("🔒 Security Concerns:");
            for (String concern : result.getSecurityConcerns()) {
                System.out.printf("  • %s%n", concern);
            }
            System.out.println();
        }
        
        // Performance issues
        if (result.getPerformanceIssues() != null && !result.getPerformanceIssues().isEmpty()) {
            System.out.println("⚡ Performance Issues:");
            for (String issue : result.getPerformanceIssues()) {
                System.out.printf("  • %s%n", issue);
            }
            System.out.println();
        }
        
        System.out.println("✅ Review completed successfully!");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new ReviewPilotCLI());
        commandLine.addSubcommand("review", new ReviewCommand());
        commandLine.addSubcommand("config", new ConfigCommand());
        commandLine.addSubcommand("version", new VersionCommand());
        
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
} 