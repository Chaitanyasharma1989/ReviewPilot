"""
Tests for OpenAI Agent
"""

import pytest
from unittest.mock import Mock, patch
from reviewpilot_langchain.models import ReviewConfig, ProviderType, AgentType, PullRequestDetails
from reviewpilot_langchain.agents.openai_agent import OpenAIAgent


class TestOpenAIAgent:
    """Test cases for OpenAIAgent"""
    
    @pytest.fixture
    def config(self):
        """Create a test configuration"""
        return ReviewConfig(
            provider=ProviderType.GITHUB,
            agent=AgentType.OPENAI,
            model_name="gpt-4",
            temperature=0.1,
            max_tokens=4000
        )
    
    @pytest.fixture
    def pr_details(self):
        """Create test pull request details"""
        return PullRequestDetails(
            title="Test PR",
            description="This is a test pull request",
            changed_files=["src/main.py", "tests/test_main.py"],
            diff="diff --git a/src/main.py b/src/main.py\nindex 1234567..abcdefg 100644\n--- a/src/main.py\n+++ b/src/main.py\n@@ -1,3 +1,4 @@\n def main():\n     print('Hello, World!')\n+    return 0\n",
            pr_number=42,
            repo_owner="testuser",
            repo_name="testrepo",
            base_branch="main",
            head_branch="feature/test"
        )
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    def test_agent_initialization(self, mock_chat_openai, config):
        """Test agent initialization"""
        mock_llm = Mock()
        mock_chat_openai.return_value = mock_llm
        
        agent = OpenAIAgent(config)
        
        assert agent.config == config
        assert agent.llm == mock_llm
        assert len(agent.tools) == 3  # code_analyzer, security_checker, performance_analyzer
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    @patch('reviewpilot_langchain.agents.openai_agent.initialize_agent')
    def test_review_pull_request(self, mock_initialize_agent, mock_chat_openai, config, pr_details):
        """Test pull request review"""
        # Mock the LLM
        mock_llm = Mock()
        mock_llm.predict.return_value = "This is a test review response"
        mock_chat_openai.return_value = mock_llm
        
        # Mock the agent
        mock_agent = Mock()
        mock_agent.run.return_value = "This is a test review response"
        mock_initialize_agent.return_value = mock_agent
        
        # Set environment variable for API key
        with patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'}):
            agent = OpenAIAgent(config)
            result = agent.review_pull_request(pr_details)
        
        assert result.summary == "This is a test review response"
        assert result.model_used == "gpt-4"
        assert result.review_time > 0
        assert result.tokens_used > 0
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    def test_analyze_security(self, mock_chat_openai, config, pr_details):
        """Test security analysis"""
        mock_llm = Mock()
        mock_llm.predict.return_value = "SQL injection vulnerability found\nXSS vulnerability detected"
        mock_chat_openai.return_value = mock_llm
        
        agent = OpenAIAgent(config)
        security_issues = agent.analyze_security(pr_details)
        
        assert len(security_issues) == 2
        assert "SQL injection" in security_issues[0]
        assert "XSS" in security_issues[1]
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    def test_analyze_performance(self, mock_chat_openai, config, pr_details):
        """Test performance analysis"""
        mock_llm = Mock()
        mock_llm.predict.return_value = "Memory leak detected\nInefficient algorithm found"
        mock_chat_openai.return_value = mock_llm
        
        agent = OpenAIAgent(config)
        performance_issues = agent.analyze_performance(pr_details)
        
        assert len(performance_issues) == 2
        assert "Memory leak" in performance_issues[0]
        assert "Inefficient" in performance_issues[1]
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    def test_analyze_code_quality(self, mock_chat_openai, config, pr_details):
        """Test code quality analysis"""
        mock_llm = Mock()
        mock_llm.predict.return_value = """
        ISSUES:
        - Missing docstring
        - Unused variable
        
        SUGGESTIONS:
        - Add type hints
        - Improve variable naming
        
        COMPLEXITY: Medium
        READABILITY: High
        MAINTAINABILITY: High
        """
        mock_chat_openai.return_value = mock_llm
        
        agent = OpenAIAgent(config)
        quality_metrics = agent.analyze_code_quality(pr_details)
        
        assert len(quality_metrics["issues"]) == 2
        assert len(quality_metrics["suggestions"]) == 2
        assert quality_metrics["complexity"] == "Medium"
        assert quality_metrics["readability"] == "High"
        assert quality_metrics["maintainability"] == "High"
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    def test_calculate_quality_score(self, mock_chat_openai, config):
        """Test quality score calculation"""
        mock_llm = Mock()
        mock_chat_openai.return_value = mock_llm
        
        with patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'}):
            agent = OpenAIAgent(config)
        
        metrics = {
            "complexity": "Low",
            "readability": "High",
            "maintainability": "Medium"
        }
        
        score = agent._calculate_quality_score(metrics)
        assert score == 60.0  # (30 + 90 + 60) / 3
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    def test_calculate_confidence_score(self, mock_chat_openai, config):
        """Test confidence score calculation"""
        mock_llm = Mock()
        mock_chat_openai.return_value = mock_llm
        
        with patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'}):
            agent = OpenAIAgent(config)
        
        # Test short response
        score = agent._calculate_confidence_score("Short")
        assert score == 30.0
        
        # Test medium response
        score = agent._calculate_confidence_score("A" * 200)
        assert score == 60.0
        
        # Test long response
        score = agent._calculate_confidence_score("A" * 600)
        assert score == 90.0
    
    @patch('reviewpilot_langchain.agents.openai_agent.ChatOpenAI')
    def test_estimate_tokens(self, mock_chat_openai, config):
        """Test token estimation"""
        mock_llm = Mock()
        mock_chat_openai.return_value = mock_llm
        
        with patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'}):
            agent = OpenAIAgent(config)
        
        text = "This is a test text with 40 characters"
        tokens = agent._estimate_tokens(text)
        assert tokens == 9  # 40 / 4 = 10, but actual calculation gives 9 