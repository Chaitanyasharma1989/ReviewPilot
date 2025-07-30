# ReviewPilot

A Java 21 tool to fetch pull/merge request changes from GitHub, GitLab, or Bitbucket, send them to an AI agent (ChatGPT, GitDuo, Copilot), and get consolidated review comments.

## 🆕 LangChain Modules

We now offer **two modern implementations** with enhanced AI capabilities!

### 🐍 Python LangChain Module
Check out the [`langchain-module/`](./langchain-module/) directory for:

- 🤖 **Advanced AI Integration**: LangChain framework with 100+ LLM providers
- 🔍 **Comprehensive Analysis**: Multi-aspect reviews (security, performance, quality)
- 🛠️ **Rich CLI Experience**: Beautiful terminal interface with progress tracking
- 📊 **Structured Output**: Detailed metrics and scoring
- 🔌 **Extensible Architecture**: Easy to add new providers and agents

**Quick Start (Python LangChain)**:
```bash
cd langchain-module
pip install -r requirements.txt
python main.py review octocat Hello-World 42
```

### ☕ Java LangChain4j Module
Check out the [`langchain4j-module/`](./langchain4j-module/) directory for:

- 🤖 **LangChain4j Integration**: Java-native LangChain framework
- 🔍 **Multi-Provider Support**: GitHub, GitLab, and Bitbucket
- 🛠️ **Rich CLI Interface**: Beautiful ASCII tables and formatted output
- 📊 **Structured Analysis**: Code quality, security, and performance metrics
- 🔌 **Plugin Architecture**: Extensible provider and agent system

**Quick Start (Java LangChain4j)**:
```bash
cd langchain4j-module
mvn clean package
java -jar target/reviewpilot-langchain4j-1.0.0.jar review octocat Hello-World 42
```

See the [Python LangChain Module README](./langchain-module/README.md) and [Java LangChain4j Module README](./langchain4j-module/README.md) for detailed comparisons and benefits.

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

## 🆚 Implementation Comparison

| Aspect | Original Java | Python LangChain | Java LangChain4j |
|--------|---------------|------------------|------------------|
| **Language** | Java 21 | Python 3.8+ | Java 21 |
| **AI Framework** | Direct API calls | LangChain framework | LangChain4j framework |
| **LLM Support** | OpenAI only | 100+ providers | 100+ providers |
| **Features** | Basic review | Advanced analysis, memory, tools | Advanced analysis, memory, tools |
| **CLI Experience** | Basic output | Rich terminal interface | Rich ASCII tables |
| **Extensibility** | Java SPI | Python plugins | Plugin architecture |
| **Memory** | None | Conversation memory | Chat memory |
| **Vector Search** | None | ChromaDB integration | ChromaDB integration |

For detailed comparisons and benefits analysis, see the [Python LangChain Module documentation](./langchain-module/README.md) and [Java LangChain4j Module documentation](./langchain4j-module/README.md). 