"""
OpenAI agent implementation using LangChain
"""

import time
from typing import List, Dict, Any
from langchain_openai import ChatOpenAI
from langchain.agents import initialize_agent, AgentType, Tool
from langchain.memory import ConversationBufferMemory
from langchain.prompts import PromptTemplate
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.vectorstores import Chroma
from langchain.embeddings import OpenAIEmbeddings
from langchain.schema import Document
from langchain.chains import LLMChain
from .base_agent import BaseAgent
from ..models import PullRequestDetails, CodeReviewResult, ReviewConfig


class OpenAIAgent(BaseAgent):
    """OpenAI agent implementation using LangChain"""
    
    def _setup_llm(self):
        """Setup OpenAI language model"""
        self.llm = ChatOpenAI(
            model_name=self.config.model_name,
            temperature=self.config.temperature,
            max_tokens=self.config.max_tokens
        )
    
    def _setup_tools(self):
        """Setup tools for code analysis"""
        self.tools = [
            Tool(
                name="code_analyzer",
                func=self._analyze_code_snippet,
                description="Analyze a specific code snippet for issues and improvements"
            ),
            Tool(
                name="security_checker",
                func=self._check_security_issues,
                description="Check for common security vulnerabilities in code"
            ),
            Tool(
                name="performance_analyzer",
                func=self._analyze_performance,
                description="Analyze code for performance issues and optimization opportunities"
            )
        ]
    
    def _setup_memory(self):
        """Setup conversation memory"""
        if self.config.enable_memory:
            self.memory = ConversationBufferMemory(
                memory_key="chat_history",
                return_messages=True
            )
        else:
            self.memory = None
    
    def review_pull_request(self, pr_details: PullRequestDetails) -> CodeReviewResult:
        """Review a pull request using LangChain agent"""
        start_time = time.time()
        
        # Initialize agent
        agent = initialize_agent(
            tools=self.tools,
            llm=self.llm,
            agent=AgentType.ZERO_SHOT_REACT_DESCRIPTION,
            memory=self.memory,
            verbose=True
        )
        
        # Create review prompt
        review_prompt = self._create_review_prompt(pr_details)
        
        # Run the review
        response = agent.run(review_prompt)
        
        # Analyze different aspects
        security_issues = self.analyze_security(pr_details)
        performance_issues = self.analyze_performance(pr_details)
        quality_metrics = self.analyze_code_quality(pr_details)
        
        # Calculate scores
        quality_score = self._calculate_quality_score(quality_metrics)
        confidence_score = self._calculate_confidence_score(response)
        
        review_time = time.time() - start_time
        
        return CodeReviewResult(
            summary=response,
            issues=quality_metrics.get("issues", []),
            suggestions=quality_metrics.get("suggestions", []),
            security_concerns=security_issues,
            performance_issues=performance_issues,
            code_quality_score=quality_score,
            confidence_score=confidence_score,
            review_time=review_time,
            tokens_used=self._estimate_tokens(response),
            model_used=self.config.model_name
        )
    
    def analyze_security(self, pr_details: PullRequestDetails) -> List[str]:
        """Analyze security aspects of the code"""
        security_prompt = f"""
        Analyze the following code changes for security vulnerabilities:
        
        Title: {pr_details.title}
        Description: {pr_details.description}
        Changed Files: {pr_details.changed_files}
        Diff: {pr_details.diff}
        
        Focus on:
        1. SQL injection vulnerabilities
        2. XSS vulnerabilities
        3. Authentication/authorization issues
        4. Input validation problems
        5. Sensitive data exposure
        6. Insecure dependencies
        
        Provide specific security concerns found:
        """
        
        response = self.llm.predict(security_prompt)
        return [line.strip() for line in response.split('\n') if line.strip()]
    
    def analyze_performance(self, pr_details: PullRequestDetails) -> List[str]:
        """Analyze performance aspects of the code"""
        performance_prompt = f"""
        Analyze the following code changes for performance issues:
        
        Title: {pr_details.title}
        Description: {pr_details.description}
        Changed Files: {pr_details.changed_files}
        Diff: {pr_details.diff}
        
        Focus on:
        1. Algorithm efficiency
        2. Database query optimization
        3. Memory usage
        4. Network calls optimization
        5. Caching opportunities
        6. Resource leaks
        
        Provide specific performance issues found:
        """
        
        response = self.llm.predict(performance_prompt)
        return [line.strip() for line in response.split('\n') if line.strip()]
    
    def analyze_code_quality(self, pr_details: PullRequestDetails) -> Dict[str, Any]:
        """Analyze code quality and return metrics"""
        quality_prompt = f"""
        Analyze the following code changes for code quality:
        
        Title: {pr_details.title}
        Description: {pr_details.description}
        Changed Files: {pr_details.changed_files}
        Diff: {pr_details.diff}
        
        Provide analysis in the following format:
        
        ISSUES:
        - [List specific issues found]
        
        SUGGESTIONS:
        - [List improvement suggestions]
        
        COMPLEXITY: [Low/Medium/High]
        READABILITY: [Low/Medium/High]
        MAINTAINABILITY: [Low/Medium/High]
        """
        
        response = self.llm.predict(quality_prompt)
        
        # Parse response
        issues = []
        suggestions = []
        complexity = "Medium"
        readability = "Medium"
        maintainability = "Medium"
        
        lines = response.split('\n')
        current_section = None
        
        for line in lines:
            line = line.strip()
            if line.startswith('ISSUES:'):
                current_section = 'issues'
            elif line.startswith('SUGGESTIONS:'):
                current_section = 'suggestions'
            elif line.startswith('COMPLEXITY:'):
                complexity = line.split(':')[1].strip()
            elif line.startswith('READABILITY:'):
                readability = line.split(':')[1].strip()
            elif line.startswith('MAINTAINABILITY:'):
                maintainability = line.split(':')[1].strip()
            elif line.startswith('-') and current_section:
                item = line[1:].strip()
                if current_section == 'issues':
                    issues.append(item)
                elif current_section == 'suggestions':
                    suggestions.append(item)
        
        return {
            "issues": issues,
            "suggestions": suggestions,
            "complexity": complexity,
            "readability": readability,
            "maintainability": maintainability
        }
    
    def _create_review_prompt(self, pr_details: PullRequestDetails) -> str:
        """Create a comprehensive review prompt"""
        if self.config.custom_prompt:
            prompt = self.config.custom_prompt
        else:
            prompt = """
            You are an expert code reviewer. Analyze the following pull request and provide a comprehensive review.
            
            Pull Request Details:
            - Title: {title}
            - Description: {description}
            - Changed Files: {changed_files}
            - Diff: {diff}
            
            Please provide:
            1. Overall assessment of the changes
            2. Code quality analysis
            3. Potential issues and concerns
            4. Suggestions for improvement
            5. Security considerations
            6. Performance implications
            
            Be specific and actionable in your feedback.
            """
        
        return prompt.format(
            title=pr_details.title,
            description=pr_details.description,
            changed_files=pr_details.changed_files,
            diff=pr_details.diff
        )
    
    def _analyze_code_snippet(self, code: str) -> str:
        """Analyze a specific code snippet"""
        prompt = f"Analyze this code snippet for issues and improvements:\n\n{code}"
        return self.llm.predict(prompt)
    
    def _check_security_issues(self, code: str) -> str:
        """Check for security issues in code"""
        prompt = f"Check this code for security vulnerabilities:\n\n{code}"
        return self.llm.predict(prompt)
    
    def _analyze_performance(self, code: str) -> str:
        """Analyze performance aspects of code"""
        prompt = f"Analyze this code for performance issues:\n\n{code}"
        return self.llm.predict(prompt)
    
    def _calculate_quality_score(self, metrics: Dict[str, Any]) -> float:
        """Calculate code quality score"""
        scores = {
            "Low": 30,
            "Medium": 60,
            "High": 90
        }
        
        complexity_score = scores.get(metrics.get("complexity", "Medium"), 60)
        readability_score = scores.get(metrics.get("readability", "Medium"), 60)
        maintainability_score = scores.get(metrics.get("maintainability", "Medium"), 60)
        
        return (complexity_score + readability_score + maintainability_score) / 3
    
    def _calculate_confidence_score(self, response: str) -> float:
        """Calculate confidence score based on response quality"""
        # Simple heuristic based on response length and structure
        if len(response) < 100:
            return 30.0
        elif len(response) < 500:
            return 60.0
        else:
            return 90.0
    
    def _estimate_tokens(self, text: str) -> int:
        """Estimate token count"""
        # Rough estimation: 1 token ≈ 4 characters
        return len(text) // 4 