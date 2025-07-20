package com.csharma.reviewpilot.adapter.provider;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.csharma.reviewpilot.model.PullRequestDetails;
import com.csharma.reviewpilot.exception.ProviderException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class GitLabPullRequestProvider implements PullRequestProvider {
    private final String apiUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitLabPullRequestProvider() {
        Config config = ConfigFactory.load();
        this.apiUrl = config.getString("reviewpilot.gitlab.api-url");
    }

    @Override
    public PullRequestDetails fetchPullRequestDetails(String repoOwner, String repoName, String prNumber, String authToken) throws Exception {
        String projectPath = URLEncoder.encode(repoOwner + "/" + repoName, StandardCharsets.UTF_8);
        String mrUrl = String.format("%s/%s/merge_requests/%s", apiUrl, projectPath, prNumber);
        HttpRequest mrRequest = HttpRequest.newBuilder()
                .uri(URI.create(mrUrl))
                .header("PRIVATE-TOKEN", authToken)
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> mrResponse = httpClient.send(mrRequest, HttpResponse.BodyHandlers.ofString());
        if (mrResponse.statusCode() != 200) {
            throw new ProviderException("Failed to fetch MR metadata: " + mrResponse.body());
        }
        JsonNode mrJson = objectMapper.readTree(mrResponse.body());
        String title = mrJson.get("title").asText();
        String description = mrJson.get("description").asText("");

        String changesUrl = mrUrl + "/changes";
        HttpRequest changesRequest = HttpRequest.newBuilder()
                .uri(URI.create(changesUrl))
                .header("PRIVATE-TOKEN", authToken)
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> changesResponse = httpClient.send(changesRequest, HttpResponse.BodyHandlers.ofString());
        if (changesResponse.statusCode() != 200) {
            throw new ProviderException("Failed to fetch MR changes: " + changesResponse.body());
        }
        JsonNode changesJson = objectMapper.readTree(changesResponse.body());
        List<String> changedFiles = new ArrayList<>();
        for (JsonNode fileNode : changesJson.get("changes")) {
            changedFiles.add(fileNode.get("new_path").asText());
        }

        String diffsUrl = mrUrl + "/diffs";
        HttpRequest diffsRequest = HttpRequest.newBuilder()
                .uri(URI.create(diffsUrl))
                .header("PRIVATE-TOKEN", authToken)
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> diffsResponse = httpClient.send(diffsRequest, HttpResponse.BodyHandlers.ofString());
        if (diffsResponse.statusCode() != 200) {
            throw new ProviderException("Failed to fetch MR diffs: " + diffsResponse.body());
        }
        JsonNode diffsJson = objectMapper.readTree(diffsResponse.body());
        StringBuilder diffBuilder = new StringBuilder();
        for (JsonNode diffNode : diffsJson) {
            diffBuilder.append(diffNode.get("diff").asText()).append("\n");
        }
        String diff = diffBuilder.toString();

        return new PullRequestDetails(title, description, changedFiles, diff);
    }
} 