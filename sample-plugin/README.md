# Sample ReviewPilot Plugin

This is a sample plugin for [ReviewPilot](https://github.com/your-org/ReviewPilot) that demonstrates how to add a custom provider or agent adapter using the Java SPI (ServiceLoader) system.

## How It Works
- Implement the `PullRequestProvider` or `CodeReviewAgent` interface from ReviewPilot.
- Add your implementation class name to the appropriate `META-INF/services` file in your plugin JAR.
- Place your JAR on the classpath when running ReviewPilot. It will be auto-discovered and available for use via the CLI.

## Example: Custom Agent

**1. Implement the interface:**
```java
package com.example.plugin;

import com.csharma.reviewpilot.adapter.agent.CodeReviewAgent;
import com.csharma.reviewpilot.model.PullRequestDetails;

public class MyCustomAgent implements CodeReviewAgent {
    @Override
    public String reviewPullRequest(PullRequestDetails prDetails) {
        return "[MyCustomAgent] Review for: " + prDetails.getTitle();
    }
}
```

**2. Add the SPI file:**
- Create `src/main/resources/META-INF/services/com.csharma.reviewpilot.adapter.agent.CodeReviewAgent` with:
```
com.example.plugin.MyCustomAgent
```

**3. Build your plugin JAR:**
```
mvn clean package
```

**4. Use your plugin:**
- Place the JAR in the classpath when running ReviewPilot:
```
java -cp "target/ReviewPilot-1.0-SNAPSHOT.jar:sample-plugin/target/sample-plugin-1.0-SNAPSHOT.jar" \
  com.csharma.cli.App mycustomagent mycustomagent owner repo 1
```

## Example: Custom Provider
- Follow the same steps, but implement `PullRequestProvider` and add to the corresponding SPI file.

## Notes
- You can use any build tool (Maven/Gradle) as long as the SPI file and interface are correct.
- Your plugin can use its own dependencies, but avoid version conflicts with ReviewPilot core.

## License
MIT 