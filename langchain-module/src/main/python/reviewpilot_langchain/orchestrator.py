"""
Main orchestrator for ReviewPilot LangChain implementation
"""

import os
from typing import Optional
from dotenv import load_dotenv
from .models import ReviewConfig, ProviderType, AgentType
from .providers import GitHubProvider
from .agents import OpenAIAgent


class ReviewOrchestrator:
    """Main orchestrator for coordinating providers and agents"""
    
    def __init__(self, config: ReviewConfig):
        self.config = config
        self._setup_provider()
        self._setup_agent()
    
    def _setup_provider(self):
        """Setup the appropriate provider based on config"""
        auth_token = self._get_auth_token()
        
        if self.config.provider == ProviderType.GITHUB:
            self.provider = GitHubProvider(auth_token)
        else:
            raise ValueError(f"Provider {self.config.provider} not yet implemented")
    
    def _setup_agent(self):
        """Setup the appropriate agent based on config"""
        if self.config.agent == AgentType.OPENAI:
            self.agent = OpenAIAgent(self.config)
        else:
            raise ValueError(f"Agent {self.config.agent} not yet implemented")
    
    def _get_auth_token(self) -> str:
        """Get authentication token from environment or config"""
        token_mapping = {
            ProviderType.GITHUB: "GITHUB_TOKEN",
            ProviderType.GITLAB: "GITLAB_TOKEN",
            ProviderType.BITBUCKET: "BITBUCKET_TOKEN"
        }
        
        env_var = token_mapping.get(self.config.provider)
        if env_var:
            token = os.getenv(env_var)
            if token:
                return token
        
        raise ValueError(f"Authentication token not found for provider {self.config.provider}")
    
    def run_review(
        self, 
        repo_owner: str, 
        repo_name: str, 
        pr_number: int
    ):
        """Run a complete code review"""
        # Fetch pull request details
        pr_details = self.provider.fetch_pull_request_details(
            repo_owner, repo_name, pr_number
        )
        
        # Get repository context
        repo_info = self.provider.get_repository_info(repo_owner, repo_name)
        
        # Get PR history for context
        pr_history = self.provider.get_pull_request_history(
            repo_owner, repo_name, pr_number
        )
        
        # Run the review
        review_result = self.agent.review_pull_request(pr_details)
        
        return {
            "pull_request": pr_details,
            "repository": repo_info,
            "history": pr_history,
            "review": review_result
        }
    
    def run_batch_review(self, reviews: list):
        """Run multiple reviews in batch"""
        results = []
        for review in reviews:
            result = self.run_review(
                review["repo_owner"],
                review["repo_name"],
                review["pr_number"]
            )
            results.append(result)
        return results 