package com.csharma.cli;

import com.csharma.reviewpilot.adapter.provider.*;
import com.csharma.reviewpilot.adapter.agent.*;
import com.csharma.reviewpilot.service.ReviewOrchestrator;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App 
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args )
    {
        if (args.length < 5) {
            logger.info("Usage: java -jar ReviewPilot.jar <provider> <agent> <repoOwner> <repoName> <prNumber> [authToken] [prompt]\n" +
                    "  provider: github | gitlab | bitbucket | <plugin>\n" +
                    "  agent: chatgpt | gitduo | copilot | <plugin>\n" +
                    "  authToken: (optional, will use env if not provided)\n" +
                    "  prompt: (optional, use {{title}}, {{description}}, {{changedFiles}}, {{diff}} as placeholders)\n" +
                    "  If prompt is not provided, will check REVIEWPILOT_PROMPT env var, then reviewpilot.properties file.\n");
            System.exit(1);
        }
        String providerName = args[0].toLowerCase();
        String agentName = args[1].toLowerCase();
        String repoOwner = args[2];
        String repoName = args[3];
        String prNumber = args[4];
        String authToken = args.length > 5 ? args[5] : System.getenv("REVIEWPILOT_TOKEN");
        String prompt = null;
        if (args.length > 6) {
            prompt = args[6];
        } else if (System.getenv("REVIEWPILOT_PROMPT") != null) {
            prompt = System.getenv("REVIEWPILOT_PROMPT");
        } else {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("reviewpilot.properties")) {
                props.load(fis);
                prompt = props.getProperty("prompt");
            } catch (IOException ignored) {}
        }
        if (authToken == null || authToken.isEmpty()) {
            logger.error("No auth token provided (pass as argument or set REVIEWPILOT_TOKEN env var)");
            System.exit(2);
        }
        // Load providers and agents via ServiceLoader
        Map<String, PullRequestProvider> providerMap = new HashMap<>();
        for (PullRequestProvider p : ServiceLoader.load(PullRequestProvider.class)) {
            providerMap.put(p.getClass().getSimpleName().replace("PullRequestProvider", "").toLowerCase(), p);
        }
        PullRequestProvider prProvider = providerMap.get(providerName);
        if (prProvider == null) {
            logger.error("Unknown provider: {}. Available: {}", providerName, providerMap.keySet());
            return;
        }
        Map<String, CodeReviewAgent> agentMap = new HashMap<>();
        for (CodeReviewAgent a : ServiceLoader.load(CodeReviewAgent.class)) {
            String key = a.getClass().getSimpleName().replace("Agent", "").toLowerCase();
            // If agent supports prompt, use constructor with prompt
            if (prompt != null && a.getClass().getConstructors().length > 0) {
                try {
                    agentMap.put(key, a.getClass().getConstructor(String.class).newInstance(prompt));
                } catch (Exception e) {
                    agentMap.put(key, a); // fallback
                }
            } else {
                agentMap.put(key, a);
            }
        }
        CodeReviewAgent reviewAgent = agentMap.get(agentName);
        if (reviewAgent == null) {
            logger.error("Unknown agent: {}. Available: {}", agentName, agentMap.keySet());
            return;
        }
        ReviewOrchestrator orchestrator = new ReviewOrchestrator(prProvider, reviewAgent);
        try {
            String review = orchestrator.runReview(repoOwner, repoName, prNumber, authToken);
            logger.info("AI Review Result:\n{}", review);
        } catch (Exception e) {
            logger.error("Error during review: {}", e.getMessage(), e);
        }
    }
} 