"""
Data models for ReviewPilot LangChain implementation
"""

from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from datetime import datetime
from enum import Enum


class ProviderType(str, Enum):
    """Supported Git providers"""
    GITHUB = "github"
    GITLAB = "gitlab"
    BITBUCKET = "bitbucket"


class AgentType(str, Enum):
    """Supported AI agents"""
    OPENAI = "openai"
    ANTHROPIC = "anthropic"
    COHERE = "cohere"
    LOCAL = "local"


class PullRequestDetails(BaseModel):
    """Pull request details model"""
    title: str = Field(..., description="Pull request title")
    description: str = Field(..., description="Pull request description")
    changed_files: List[str] = Field(..., description="List of changed files")
    diff: str = Field(..., description="Git diff content")
    pr_number: int = Field(..., description="Pull request number")
    repo_owner: str = Field(..., description="Repository owner")
    repo_name: str = Field(..., description="Repository name")
    base_branch: str = Field(..., description="Base branch")
    head_branch: str = Field(..., description="Head branch")
    created_at: Optional[datetime] = Field(None, description="Creation timestamp")
    updated_at: Optional[datetime] = Field(None, description="Last update timestamp")
    author: Optional[str] = Field(None, description="PR author")
    labels: List[str] = Field(default_factory=list, description="PR labels")
    assignees: List[str] = Field(default_factory=list, description="PR assignees")


class CodeReviewResult(BaseModel):
    """Code review result model"""
    summary: str = Field(..., description="Review summary")
    issues: List[Dict[str, Any]] = Field(default_factory=list, description="Found issues")
    suggestions: List[str] = Field(default_factory=list, description="Improvement suggestions")
    security_concerns: List[str] = Field(default_factory=list, description="Security issues")
    performance_issues: List[str] = Field(default_factory=list, description="Performance issues")
    code_quality_score: Optional[float] = Field(None, description="Code quality score (0-100)")
    confidence_score: Optional[float] = Field(None, description="Review confidence score (0-100)")
    review_time: Optional[float] = Field(None, description="Review time in seconds")
    tokens_used: Optional[int] = Field(None, description="Tokens consumed")
    model_used: Optional[str] = Field(None, description="LLM model used")


class ReviewConfig(BaseModel):
    """Review configuration model"""
    provider: ProviderType = Field(..., description="Git provider")
    agent: AgentType = Field(..., description="AI agent")
    model_name: str = Field(default="gpt-4", description="LLM model name")
    temperature: float = Field(default=0.1, description="LLM temperature")
    max_tokens: int = Field(default=4000, description="Maximum tokens")
    include_security_analysis: bool = Field(default=True, description="Include security analysis")
    include_performance_analysis: bool = Field(default=True, description="Include performance analysis")
    include_code_quality_analysis: bool = Field(default=True, description="Include code quality analysis")
    custom_prompt: Optional[str] = Field(None, description="Custom review prompt")
    enable_memory: bool = Field(default=True, description="Enable conversation memory")
    enable_vector_search: bool = Field(default=True, description="Enable vector search for similar PRs")
    chunk_size: int = Field(default=2000, description="Text chunk size for processing")
    chunk_overlap: int = Field(default=200, description="Text chunk overlap") 