#!/usr/bin/env python3
"""
Setup script for ReviewPilot LangChain Module
"""

from setuptools import setup, find_packages

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

with open("requirements.txt", "r", encoding="utf-8") as fh:
    requirements = [line.strip() for line in fh if line.strip() and not line.startswith("#")]

setup(
    name="reviewpilot-langchain",
    version="1.0.0",
    author="ReviewPilot Team",
    author_email="team@reviewpilot.dev",
    description="A modern, AI-powered code review tool built with LangChain",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/your-org/ReviewPilot",
    packages=find_packages(where="src/main/python"),
    package_dir={"": "src/main/python"},
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "Topic :: Software Development :: Quality Assurance",
        "Topic :: Software Development :: Testing",
    ],
    python_requires=">=3.8",
    install_requires=requirements,
    entry_points={
        "console_scripts": [
            "reviewpilot-langchain=reviewpilot_langchain.cli:cli",
        ],
    },
    include_package_data=True,
    zip_safe=False,
) 