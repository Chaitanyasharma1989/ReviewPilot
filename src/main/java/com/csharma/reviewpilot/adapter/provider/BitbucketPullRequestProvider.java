package com.csharma.reviewpilot.adapter.provider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.csharma.reviewpilot.model.PullRequestDetails;
import com.csharma.reviewpilot.exception.ProviderException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class BitbucketPullRequestProvider implements PullRequestProvider {
    private final String apiUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BitbucketPullRequestProvider() {
        Config config = ConfigFactory.load();
        this.apiUrl = config.getString("reviewpilot.bitbucket.api-url");
    }

    @Override
    public PullRequestDetails fetchPullRequestDetails(String repoOwner, String repoName, String prNumber, String authToken) throws Exception {
        String prUrl = String.format("%s/%s/%s/pullrequests/%s", apiUrl, repoOwner, repoName, prNumber);
        HttpRequest prRequest = HttpRequest.newBuilder()
                .uri(URI.create(prUrl))
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> prResponse = httpClient.send(prRequest, HttpResponse.BodyHandlers.ofString());
        if (prResponse.statusCode() != 200) {
            throw new ProviderException("Failed to fetch PR metadata: " + prResponse.body());
        }
        JsonNode prJson = objectMapper.readTree(prResponse.body());
        String title = prJson.get("title").asText();
        String description = prJson.get("description").asText("");

        String filesUrl = prUrl + "/diffstat";
        HttpRequest filesRequest = HttpRequest.newBuilder()
                .uri(URI.create(filesUrl))
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> filesResponse = httpClient.send(filesRequest, HttpResponse.BodyHandlers.ofString());
        if (filesResponse.statusCode() != 200) {
            throw new ProviderException("Failed to fetch PR files: " + filesResponse.body());
        }
        JsonNode filesJson = objectMapper.readTree(filesResponse.body());
        List<String> changedFiles = new ArrayList<>();
        for (JsonNode fileNode : filesJson.get("values")) {
            changedFiles.add(fileNode.get("new") != null && fileNode.get("new").get("path") != null
                ? fileNode.get("new").get("path").asText()
                : fileNode.get("old").get("path").asText());
        }

        String diffUrl = prUrl + "/diff";
        HttpRequest diffRequest = HttpRequest.newBuilder()
                .uri(URI.create(diffUrl))
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "text/plain")
                .build();
        HttpResponse<String> diffResponse = httpClient.send(diffRequest, HttpResponse.BodyHandlers.ofString());
        if (diffResponse.statusCode() != 200) {
            throw new ProviderException("Failed to fetch PR diff: " + diffResponse.body());
        }
        String diff = diffResponse.body();

        return new PullRequestDetails(title, description, changedFiles, diff);
    }
} 