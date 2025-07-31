package com.csharma.reviewpilot.langchain4j.provider;

import com.csharma.reviewpilot.langchain4j.model.PullRequestDetails;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GitHub provider implementation using GitHub API
 */
public class GitHubProvider implements PullRequestProvider {
    private static final Logger logger = LoggerFactory.getLogger(GitHubProvider.class);
    private final GitHub gitHub;

    public GitHubProvider(String authToken) throws IOException {
        this.gitHub = GitHub.connectUsingOAuth(authToken);
    }

    @Override
    public PullRequestDetails fetchPullRequestDetails(String repoOwner, String repoName, int prNumber) throws Exception {
        logger.info("Fetching PR #{} from {}/{}", prNumber, repoOwner, repoName);
        
        try {
            GHRepository repository = gitHub.getRepository(repoOwner + "/" + repoName);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);
            
            // Get changed files
            List<String> changedFiles = pullRequest.listFiles().toList().stream()
                    .map(file -> file.getFilename())
                    .collect(Collectors.toList());
            
            // Get diff (simplified - in real implementation you'd get the full diff)
            String diff = "";
            if (!changedFiles.isEmpty()) {
                diff = "Files changed: " + String.join(", ", changedFiles);
            }
            
            PullRequestDetails details = new PullRequestDetails(
                    pullRequest.getTitle(),
                    pullRequest.getBody() != null ? pullRequest.getBody() : "",
                    changedFiles,
                    diff,
                    pullRequest.getNumber(),
                    repoOwner,
                    repoName,
                    pullRequest.getBase().getRef(),
                    pullRequest.getHead().getRef()
            );
            
            // Set additional details
            details.setCreatedAt(pullRequest.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            details.setUpdatedAt(pullRequest.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            details.setAuthor(pullRequest.getUser().getLogin());
            
            // Set labels
            details.setLabels(pullRequest.getLabels().stream()
                    .map(label -> label.getName())
                    .collect(Collectors.toList()));
            
            // Set assignees
            details.setAssignees(pullRequest.getAssignees().stream()
                    .map(assignee -> assignee.getLogin())
                    .collect(Collectors.toList()));
            
            logger.info("Successfully fetched PR details: {}", details.getTitle());
            return details;
            
        } catch (IOException e) {
            logger.error("Error fetching PR details: {}", e.getMessage());
            throw new Exception("Failed to fetch pull request details: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "github";
    }
} 