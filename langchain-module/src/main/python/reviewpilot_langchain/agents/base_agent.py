"""
Base agent interface for AI agents
"""

from abc import ABC, abstractmethod
from typing import Optional, Dict, Any
from ..models import PullRequestDetails, CodeReviewResult, ReviewConfig


class BaseAgent(ABC):
    """Base class for AI agents"""
    
    def __init__(self, config: ReviewConfig):
        self.config = config
        self._setup_llm()
        self._setup_tools()
        self._setup_memory()
    
    @abstractmethod
    def _setup_llm(self):
        """Setup the language model"""
        pass
    
    @abstractmethod
    def _setup_tools(self):
        """Setup tools for the agent"""
        pass
    
    @abstractmethod
    def _setup_memory(self):
        """Setup memory for conversation history"""
        pass
    
    @abstractmethod
    def review_pull_request(self, pr_details: PullRequestDetails) -> CodeReviewResult:
        """Review a pull request and return results"""
        pass
    
    @abstractmethod
    def analyze_security(self, pr_details: PullRequestDetails) -> list:
        """Analyze security aspects of the code"""
        pass
    
    @abstractmethod
    def analyze_performance(self, pr_details: PullRequestDetails) -> list:
        """Analyze performance aspects of the code"""
        pass
    
    @abstractmethod
    def analyze_code_quality(self, pr_details: PullRequestDetails) -> Dict[str, Any]:
        """Analyze code quality and return metrics"""
        pass 