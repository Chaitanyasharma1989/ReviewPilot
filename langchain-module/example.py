#!/usr/bin/env python3
"""
Example usage of ReviewPilot LangChain Module
"""

import os
from dotenv import load_dotenv
from reviewpilot_langchain.models import ReviewConfig, ProviderType, AgentType
from reviewpilot_langchain.orchestrator import ReviewOrchestrator

def main():
    """Example usage of the LangChain module"""
    
    # Load environment variables
    load_dotenv()
    
    # Check if required environment variables are set
    if not os.getenv("GITHUB_TOKEN"):
        print("❌ GITHUB_TOKEN not found in environment variables")
        print("Please set your GitHub token: export GITHUB_TOKEN=your_token")
        return
    
    if not os.getenv("OPENAI_API_KEY"):
        print("❌ OPENAI_API_KEY not found in environment variables")
        print("Please set your OpenAI API key: export OPENAI_API_KEY=your_key")
        return
    
    # Create configuration
    config = ReviewConfig(
        provider=ProviderType.GITHUB,
        agent=AgentType.OPENAI,
        model_name="gpt-4",
        temperature=0.1,
        max_tokens=4000,
        include_security_analysis=True,
        include_performance_analysis=True,
        include_code_quality_analysis=True,
        enable_memory=True,
        enable_vector_search=True
    )
    
    print("🚀 ReviewPilot LangChain Example")
    print("=" * 50)
    
    try:
        # Initialize orchestrator
        print("📦 Initializing ReviewPilot...")
        orchestrator = ReviewOrchestrator(config)
        
        # Example repository and PR (you can change these)
        repo_owner = "octocat"
        repo_name = "Hello-World"
        pr_number = 1  # This should be a real PR number
        
        print(f"🔍 Reviewing PR #{pr_number} in {repo_owner}/{repo_name}")
        print("⏳ This may take a few moments...")
        
        # Run the review
        result = orchestrator.run_review(repo_owner, repo_name, pr_number)
        
        # Display results
        print("\n" + "=" * 50)
        print("📊 Review Results")
        print("=" * 50)
        
        pr = result["pull_request"]
        review = result["review"]
        
        print(f"📝 Pull Request: {pr.title}")
        print(f"👤 Author: {pr.author or 'Unknown'}")
        print(f"📁 Files changed: {len(pr.changed_files)}")
        print(f"⏱️  Review time: {review.review_time:.2f}s")
        print(f"🧠 Tokens used: {review.tokens_used}")
        print(f"🤖 Model: {review.model_used}")
        
        print(f"\n📈 Quality Score: {review.code_quality_score:.1f}/100")
        print(f"🎯 Confidence Score: {review.confidence_score:.1f}/100")
        
        print(f"\n📋 Summary:")
        print(review.summary)
        
        if review.issues:
            print(f"\n❌ Issues Found:")
            for issue in review.issues:
                print(f"  • {issue}")
        
        if review.suggestions:
            print(f"\n💡 Suggestions:")
            for suggestion in review.suggestions:
                print(f"  • {suggestion}")
        
        if review.security_concerns:
            print(f"\n🔒 Security Concerns:")
            for concern in review.security_concerns:
                print(f"  • {concern}")
        
        if review.performance_issues:
            print(f"\n⚡ Performance Issues:")
            for issue in review.performance_issues:
                print(f"  • {issue}")
        
        print("\n✅ Review completed successfully!")
        
    except Exception as e:
        print(f"❌ Error during review: {str(e)}")
        print("\n💡 Troubleshooting tips:")
        print("  • Make sure your API tokens are valid")
        print("  • Check that the repository and PR number exist")
        print("  • Verify your internet connection")
        print("  • Check the LangChain module README for more details")

if __name__ == "__main__":
    main() 