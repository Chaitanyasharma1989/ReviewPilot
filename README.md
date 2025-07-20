# ReviewPilot

A Java 21 tool to fetch pull/merge request changes from GitHub, GitLab, or Bitbucket, send them to an AI agent (ChatGPT, GitDuo, Copilot), and get consolidated review comments.

## Features
- Adapter-based: Easily switch between source control providers and AI agents
- Plugin system: Add new providers/agents without changing core code (Java SPI)
- Fetches PR/MR title, description, changed files, and diff
- Sends code changes to AI for review
- Configurable via CLI, environment, or config file
- Hybrid prompt system (CLI, env, properties)

## Requirements
- Java 21+
- Maven
- API tokens for your chosen provider/agent

## Setup
1. Clone the repo
2. Run `mvn clean package` to build
3. Set up your API keys as environment variables or in `src/main/resources/application.conf`:
   - `GITHUB_TOKEN`, `GITLAB_TOKEN`, `BITBUCKET_TOKEN`, `OPENAI_API_KEY`, `GITDUO_API_KEY`, `COPILOT_API_KEY`

## Usage
```
java -jar target/ReviewPilot-1.0-SNAPSHOT.jar <provider> <agent> <repoOwner> <repoName> <prNumber> [authToken] [prompt]
```
- `provider`: `github` | `gitlab` | `bitbucket` | `<plugin>`
- `agent`: `chatgpt` | `gitduo` | `copilot` | `<plugin>`
- `repoOwner`: Repository owner/org/user
- `repoName`: Repository name
- `prNumber`: Pull/Merge request number
- `authToken`: (optional) API token (else use config/env)
- `prompt`: (optional) Custom prompt (else use env/config/default)

### Example
```
java -jar target/ReviewPilot-1.0-SNAPSHOT.jar github chatgpt octocat Hello-World 42
```

## Configuration
- All config is in `src/main/resources/application.conf` (Typesafe Config)
- Supports environment variable overrides
- Example:
```
reviewpilot {
  provider = "github"
  agent = "chatgpt"
  github.token = "${?GITHUB_TOKEN}"
  openai.api-key = "${?OPENAI_API_KEY}"
  github.api-url = "https://api.github.com/repos"
}
```

## Plugin System (Java SPI)
- Add new provider/agent adapters by implementing the `PullRequestProvider` or `CodeReviewAgent` interface in a separate JAR.
- Add your implementation class name to the appropriate `META-INF/services` file in your JAR.
- Drop your JAR in the classpath; ReviewPilot will auto-discover it.
- Example for a new provider:
  - Implement `com.csharma.reviewpilot.adapter.provider.PullRequestProvider`
  - Add `META-INF/services/com.csharma.reviewpilot.adapter.provider.PullRequestProvider` with your class name

## Prompt System
- Prompt can be provided via CLI, environment variable (`REVIEWPILOT_PROMPT`), or `reviewpilot.properties` file.
- Placeholders: `{{title}}`, `{{description}}`, `{{changedFiles}}`, `{{diff}}`

## Testing
- Run `mvn test` to execute all unit and integration tests.

## Extending
- Add new adapters as plugins (see above)
- Add new config keys in `application.conf`

## License
MIT 