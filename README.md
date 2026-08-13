News Explorer

A searchable news repository for retrieving, organizing, and recommending articles at scale.

Team Members
Name	ID Number
K Sai Anshu	2520030440
Moksha Depuru	2520030515
U Pranav Varma	2520030351
Supervisor

Dr. S. Vinay Kumar, Associate Professor, Department of Computer Science and Engineering

Abstract

News Explorer is a searchable news repository designed to help readers manage the overwhelming volume of news published daily across countless sources and topics. The project addresses three core challenges faced by readers: information overload, ineffective keyword search that misses related coverage, and the absence of scalable organization for articles. News Explorer retrieves relevant articles by keyword, topic, or phrase; organizes them into coherent, browsable categories; and recommends related stories based on content similarity and reader interest. The system is built using Python, employing custom data structures and algorithms such as inverted indexing, fuzzy matching through edit distance, and document similarity measures like TF-IDF and cosine similarity for ranking. Articles are continuously ingested through a dedicated pipeline and stored in a lightweight SQLite database, accessible via a command-line or web interface. The expected outcome is a working prototype demonstrating fast, sub-second retrieval, organized results, and relevant recommendations, offering readers a faster and smarter way to navigate daily news.

Setup Instructions
Prerequisites
Python 3.10 or higher
pip (Python package manager)
SQLite3 (bundled with Python's standard library)
Git
Installation
Clone the repository:
bash
   git clone <repository-url>
   cd news-explorer
Create and activate a virtual environment:
bash
   python -m venv venv
   source venv/bin/activate   # On Windows: venv\Scripts\activate
Install dependencies:
bash
   pip install -r requirements.txt
Initialize the database:
bash
   python scripts/init_db.py
Execution Instructions
Ingest the news article corpus into the repository:
bash
   python ingest.py --source data/articles/
Run the search and recommendation engine:
bash
   python main.py
(If a web interface is implemented) Start the server and open it in a browser:
bash
   python app.py
   # Visit http://localhost:5000
Query the repository by keyword or topic through the command-line prompt or web search bar to view retrieved, organized, and recommended articles.

Update the commands above as the actual scripts and entry points are finalized during development.

Current Phase Status

Phase: Proposal / Review-1 — Design and Planning

 Problem statement defined
 Proposed functionalities finalized (search, organization, recommendations, ingestion pipeline)
 Tools and technology stack selected (Python, inverted index, TF-IDF/cosine similarity, SQLite)
 Article ingestion pipeline implementation
 Search and indexing module
 Recommendation engine
 Interface (CLI/web) integration
 Testing and evaluation on real news corpus
Repository Notes
Commits are made individually by each team member under their own GitHub account to ensure verifiable, per-member contribution history.
At least one meaningful commit is made per team member per week throughout the project phase.
Each phase deliverable is tagged in the repository (e.g., review-1, review-2, final).
Repository access is granted to the Supervisor and the Course Coordinator, and will remain active until the final project evaluation is complete.
No credentials, API keys, licensed datasets, or confidential institutional data are committed to this repository (see .gitignore).
