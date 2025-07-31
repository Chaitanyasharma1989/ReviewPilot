"""
CLI interface for ReviewPilot LangChain
"""

import click
import os
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn
from dotenv import load_dotenv
from .models import ReviewConfig, ProviderType, AgentType
from .orchestrator import ReviewOrchestrator

console = Console()


@click.group()
@click.version_option(version="1.0.0")
def cli():
    """ReviewPilot LangChain - AI-powered code review tool"""
    load_dotenv()


@cli.command()
@click.option('--provider', '-p', type=click.Choice(['github', 'gitlab', 'bitbucket']), 
              default='github', help='Git provider')
@click.option('--agent', '-a', type=click.Choice(['openai', 'anthropic', 'cohere']), 
              default='openai', help='AI agent')
@click.option('--model', '-m', default='gpt-4', help='LLM model name')
@click.option('--temperature', '-t', type=float, default=0.1, help='LLM temperature')
@click.option('--max-tokens', type=int, default=4000, help='Maximum tokens')
@click.option('--custom-prompt', help='Custom review prompt')
@click.option('--no-security', is_flag=True, help='Skip security analysis')
@click.option('--no-performance', is_flag=True, help='Skip performance analysis')
@click.option('--no-quality', is_flag=True, help='Skip code quality analysis')
@click.option('--no-memory', is_flag=True, help='Disable conversation memory')
@click.option('--no-vector-search', is_flag=True, help='Disable vector search')
@click.argument('repo_owner')
@click.argument('repo_name')
@click.argument('pr_number', type=int)
def review(provider, agent, model, temperature, max_tokens, custom_prompt,
           no_security, no_performance, no_quality, no_memory, no_vector_search,
           repo_owner, repo_name, pr_number):
    """Review a pull request using AI"""
    
    # Create configuration
    config = ReviewConfig(
        provider=ProviderType(provider),
        agent=AgentType(agent),
        model_name=model,
        temperature=temperature,
        max_tokens=max_tokens,
        custom_prompt=custom_prompt,
        include_security_analysis=not no_security,
        include_performance_analysis=not no_performance,
        include_code_quality_analysis=not no_quality,
        enable_memory=not no_memory,
        enable_vector_search=not no_vector_search
    )
    
    # Display configuration
    console.print(Panel(f"[bold blue]ReviewPilot LangChain Configuration[/bold blue]\n"
                       f"Provider: {provider}\n"
                       f"Agent: {agent}\n"
                       f"Model: {model}\n"
                       f"Repository: {repo_owner}/{repo_name}\n"
                       f"PR: #{pr_number}"))
    
    try:
        # Initialize orchestrator
        with Progress(
            SpinnerColumn(),
            TextColumn("[progress.description]{task.description}"),
            console=console
        ) as progress:
            task = progress.add_task("Initializing ReviewPilot...", total=None)
            
            orchestrator = ReviewOrchestrator(config)
            
            progress.update(task, description="Fetching pull request details...")
            result = orchestrator.run_review(repo_owner, repo_name, pr_number)
            
            progress.update(task, description="Review completed!", total=1)
            progress.advance(task)
        
        # Display results
        _display_review_results(result)
        
    except Exception as e:
        console.print(f"[bold red]Error:[/bold red] {str(e)}")
        raise click.Abort()


@cli.command()
@click.option('--provider', '-p', type=click.Choice(['github', 'gitlab', 'bitbucket']), 
              default='github', help='Git provider')
@click.option('--agent', '-a', type=click.Choice(['openai', 'anthropic', 'cohere']), 
              default='openai', help='AI agent')
@click.option('--model', '-m', default='gpt-4', help='LLM model name')
@click.argument('config_file')
def batch(config_file, provider, agent, model):
    """Run batch reviews from a configuration file"""
    
    if not os.path.exists(config_file):
        console.print(f"[bold red]Error:[/bold red] Configuration file {config_file} not found")
        raise click.Abort()
    
    # TODO: Implement batch review functionality
    console.print("[yellow]Batch review functionality coming soon![/yellow]")


@cli.command()
def config():
    """Show current configuration"""
    table = Table(title="ReviewPilot LangChain Configuration")
    table.add_column("Setting", style="cyan")
    table.add_column("Value", style="magenta")
    
    # Environment variables
    env_vars = {
        "GITHUB_TOKEN": os.getenv("GITHUB_TOKEN", "Not set"),
        "GITLAB_TOKEN": os.getenv("GITLAB_TOKEN", "Not set"),
        "BITBUCKET_TOKEN": os.getenv("BITBUCKET_TOKEN", "Not set"),
        "OPENAI_API_KEY": os.getenv("OPENAI_API_KEY", "Not set"),
        "ANTHROPIC_API_KEY": os.getenv("ANTHROPIC_API_KEY", "Not set"),
        "COHERE_API_KEY": os.getenv("COHERE_API_KEY", "Not set")
    }
    
    for key, value in env_vars.items():
        # Mask sensitive values
        if "TOKEN" in key or "KEY" in key:
            value = "***" + value[-4:] if value != "Not set" else value
        table.add_row(key, value)
    
    console.print(table)


def _display_review_results(result):
    """Display review results in a formatted way"""
    pr = result["pull_request"]
    review = result["review"]
    
    # Pull Request Info
    console.print(Panel(f"[bold green]Pull Request: {pr.title}[/bold green]\n"
                       f"Repository: {pr.repo_owner}/{pr.repo_name}\n"
                       f"PR #{pr.pr_number} | Author: {pr.author or 'Unknown'}\n"
                       f"Files changed: {len(pr.changed_files)}"))
    
    # Review Summary
    console.print(Panel(f"[bold blue]Review Summary[/bold blue]\n{review.summary}"))
    
    # Metrics Table
    metrics_table = Table(title="Review Metrics")
    metrics_table.add_column("Metric", style="cyan")
    metrics_table.add_column("Value", style="magenta")
    
    metrics_table.add_row("Code Quality Score", f"{review.code_quality_score:.1f}/100")
    metrics_table.add_row("Confidence Score", f"{review.confidence_score:.1f}/100")
    metrics_table.add_row("Review Time", f"{review.review_time:.2f}s")
    metrics_table.add_row("Tokens Used", str(review.tokens_used))
    metrics_table.add_row("Model Used", review.model_used)
    
    console.print(metrics_table)
    
    # Issues and Suggestions
    if review.issues:
        console.print(Panel(f"[bold red]Issues Found[/bold red]\n" + 
                           "\n".join(f"• {issue}" for issue in review.issues)))
    
    if review.suggestions:
        console.print(Panel(f"[bold yellow]Suggestions[/bold yellow]\n" + 
                           "\n".join(f"• {suggestion}" for suggestion in review.suggestions)))
    
    if review.security_concerns:
        console.print(Panel(f"[bold red]Security Concerns[/bold red]\n" + 
                           "\n".join(f"• {concern}" for concern in review.security_concerns)))
    
    if review.performance_issues:
        console.print(Panel(f"[bold orange]Performance Issues[/bold orange]\n" + 
                           "\n".join(f"• {issue}" for issue in review.performance_issues)))


if __name__ == '__main__':
    cli() 