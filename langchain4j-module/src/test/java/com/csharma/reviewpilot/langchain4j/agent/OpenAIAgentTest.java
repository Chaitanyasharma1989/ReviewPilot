package com.csharma.reviewpilot.langchain4j.agent;

import com.csharma.reviewpilot.langchain4j.model.PullRequestDetails;
import com.csharma.reviewpilot.langchain4j.model.ReviewConfig;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIAgentTest {

    @Test
    void testReviewConfig() {
        ReviewConfig config = new ReviewConfig();
        config.setModelName("gpt-4");
        config.setTemperature(0.1);
        config.setMaxTokens(4000);
        
        assertEquals("gpt-4", config.getModelName());
        assertEquals(0.1, config.getTemperature());
        assertEquals(4000, config.getMaxTokens());
    }

    @Test
    void testPullRequestDetails() {
        PullRequestDetails prDetails = new PullRequestDetails(
            "Test PR",
            "This is a test pull request",
            Arrays.asList("src/main.java", "src/test.java"),
            "diff content",
            42,
            "testowner",
            "testrepo",
            "main",
            "feature/test"
        );

        assertEquals("Test PR", prDetails.getTitle());
        assertEquals("This is a test pull request", prDetails.getDescription());
        assertEquals(2, prDetails.getChangedFiles().size());
        assertEquals(42, prDetails.getPrNumber());
        assertEquals("testowner", prDetails.getRepoOwner());
        assertEquals("testrepo", prDetails.getRepoName());
    }

    @Test
    void testPullRequestDetailsSetters() {
        PullRequestDetails prDetails = new PullRequestDetails();
        prDetails.setTitle("New Title");
        prDetails.setDescription("New Description");
        prDetails.setChangedFiles(Arrays.asList("file1.java", "file2.java"));
        prDetails.setPrNumber(123);
        prDetails.setRepoOwner("newowner");
        prDetails.setRepoName("newrepo");

        assertEquals("New Title", prDetails.getTitle());
        assertEquals("New Description", prDetails.getDescription());
        assertEquals(2, prDetails.getChangedFiles().size());
        assertEquals(123, prDetails.getPrNumber());
        assertEquals("newowner", prDetails.getRepoOwner());
        assertEquals("newrepo", prDetails.getRepoName());
    }

    @Test
    void testReviewConfigDefaults() {
        ReviewConfig config = new ReviewConfig();
        
        assertEquals("gpt-4", config.getModelName());
        assertEquals(0.1, config.getTemperature());
        assertEquals(4000, config.getMaxTokens());
        assertTrue(config.getIncludeSecurityAnalysis());
        assertTrue(config.getIncludePerformanceAnalysis());
        assertTrue(config.getIncludeCodeQualityAnalysis());
        assertTrue(config.getEnableMemory());
        assertTrue(config.getEnableVectorSearch());
    }
} 