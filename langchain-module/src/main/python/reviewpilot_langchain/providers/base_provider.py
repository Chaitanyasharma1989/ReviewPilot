"""
Base provider interface for Git providers
"""

from abc import ABC, abstractmethod
from typing import Optional
from ..models import PullRequestDetails


class BaseProvider(ABC):
    """Base class for Git providers"""
    
    def __init__(self, auth_token: str, api_url: Optional[str] = None):
        self.auth_token = auth_token
        self.api_url = api_url
        self._setup_client()
    
    @abstractmethod
    def _setup_client(self):
        """Setup the API client"""
        pass
    
    @abstractmethod
    def fetch_pull_request_details(
        self, 
        repo_owner: str, 
        repo_name: str, 
        pr_number: int
    ) -> PullRequestDetails:
        """Fetch pull request details from the provider"""
        pass
    
    @abstractmethod
    def get_repository_info(self, repo_owner: str, repo_name: str) -> dict:
        """Get repository information"""
        pass
    
    @abstractmethod
    def get_pull_request_history(self, repo_owner: str, repo_name: str, pr_number: int) -> list:
        """Get pull request history and comments"""
        pass 