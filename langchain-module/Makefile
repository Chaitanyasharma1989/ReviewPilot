# ReviewPilot LangChain Module Makefile

.PHONY: help install test clean run-example setup-env

help: ## Show this help message
	@echo "ReviewPilot LangChain Module - Available commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

install: ## Install dependencies
	pip install -r requirements.txt

install-dev: ## Install development dependencies
	pip install -r requirements.txt
	pip install pytest pytest-cov black flake8 mypy

setup-env: ## Setup environment variables (copy example file)
	cp env.example .env
	@echo "✅ Environment file created. Please edit .env with your API keys."

test: ## Run tests
	python -m pytest tests/ -v

test-coverage: ## Run tests with coverage
	python -m pytest tests/ --cov=reviewpilot_langchain --cov-report=html

lint: ## Run linting
	black src/main/python/reviewpilot_langchain/
	flake8 src/main/python/reviewpilot_langchain/
	mypy src/main/python/reviewpilot_langchain/

format: ## Format code
	black src/main/python/reviewpilot_langchain/

clean: ## Clean up generated files
	rm -rf __pycache__/
	rm -rf .pytest_cache/
	rm -rf htmlcov/
	rm -rf .coverage
	rm -rf build/
	rm -rf dist/
	rm -rf *.egg-info/

run-example: ## Run the example script
	python example.py

run-cli: ## Run the CLI tool
	python main.py --help

build: ## Build the package
	python setup.py sdist bdist_wheel

install-package: ## Install the package in development mode
	pip install -e .

uninstall: ## Uninstall the package
	pip uninstall reviewpilot-langchain -y

check-env: ## Check environment variables
	@echo "Checking environment variables..."
	@python -c "import os; from dotenv import load_dotenv; load_dotenv(); print('GITHUB_TOKEN:', '✅ Set' if os.getenv('GITHUB_TOKEN') else '❌ Not set'); print('OPENAI_API_KEY:', '✅ Set' if os.getenv('OPENAI_API_KEY') else '❌ Not set')"

demo: check-env ## Run a demo review (requires environment setup)
	@echo "Running demo review..."
	python main.py review octocat Hello-World 1

all: install test lint ## Install, test, and lint
	@echo "✅ All checks passed!" 