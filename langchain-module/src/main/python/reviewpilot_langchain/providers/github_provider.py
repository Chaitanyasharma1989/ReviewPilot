"""
GitHub provider implementation using PyGithub
"""

from typing import Optional, List
from datetime import datetime
from github import Github, GithubException
from .base_provider import BaseProvider
from ..models import PullRequestDetails


class GitHubProvider(BaseProvider):
    """GitHub provider implementation"""
    
    def _setup_client(self):
        """Setup GitHub client"""
        self.client = Github(self.auth_token)
    
    def fetch_pull_request_details(
        self, 
        repo_owner: str, 
        repo_name: str, 
        pr_number: int
    ) -> PullRequestDetails:
        """Fetch pull request details from GitHub"""
        try:
            repo = self.client.get_repo(f"{repo_owner}/{repo_name}")
            pr = repo.get_pull(pr_number)
            
            # Get changed files
            changed_files = [file.filename for file in pr.get_files()]
            
            # Get diff
            diff = pr.get_files().get_page(0)[0].patch if pr.get_files().totalCount > 0 else ""
            
            return PullRequestDetails(
                title=pr.title,
                description=pr.body or "",
                changed_files=changed_files,
                diff=diff,
                pr_number=pr.number,
                repo_owner=repo_owner,
                repo_name=repo_name,
                base_branch=pr.base.ref,
                head_branch=pr.head.ref,
                created_at=pr.created_at,
                updated_at=pr.updated_at,
                author=pr.user.login if pr.user else None,
                labels=[label.name for label in pr.get_labels()],
                assignees=[assignee.login for assignee in pr.assignees]
            )
        except GithubException as e:
            raise Exception(f"GitHub API error: {e}")
    
    def get_repository_info(self, repo_owner: str, repo_name: str) -> dict:
        """Get repository information"""
        try:
            repo = self.client.get_repo(f"{repo_owner}/{repo_name}")
            return {
                "name": repo.name,
                "full_name": repo.full_name,
                "description": repo.description,
                "language": repo.language,
                "stars": repo.stargazers_count,
                "forks": repo.forks_count,
                "default_branch": repo.default_branch,
                "topics": repo.get_topics(),
                "created_at": repo.created_at,
                "updated_at": repo.updated_at
            }
        except GithubException as e:
            raise Exception(f"GitHub API error: {e}")
    
    def get_pull_request_history(self, repo_owner: str, repo_name: str, pr_number: int) -> list:
        """Get pull request history and comments"""
        try:
            repo = self.client.get_repo(f"{repo_owner}/{repo_name}")
            pr = repo.get_pull(pr_number)
            
            history = []
            
            # Get comments
            for comment in pr.get_issue_comments():
                history.append({
                    "type": "comment",
                    "author": comment.user.login,
                    "body": comment.body,
                    "created_at": comment.created_at
                })
            
            # Get review comments
            for comment in pr.get_review_comments():
                history.append({
                    "type": "review_comment",
                    "author": comment.user.login,
                    "body": comment.body,
                    "path": comment.path,
                    "line": comment.line,
                    "created_at": comment.created_at
                })
            
            return history
        except GithubException as e:
            raise Exception(f"GitHub API error: {e}") 