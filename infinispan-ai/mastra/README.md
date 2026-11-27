# Infinispan + Mastra RAG Agent

This example shows how to build an agentic RAG application using [Infinispan](https://infinispan.org) as the vector store and [Mastra](https://mastra.ai) as the AI framework.

An agent queries a corpus of 500 sci-fi movies stored in Infinispan using vector search, then answers questions using the retrieved context.

## Prerequisites

- Node.js 22+
- Docker or Podman
- An OpenAI API key

## Setup

Start Infinispan:

```bash
docker compose up -d
```

Install dependencies:

```bash
npm install
```

Configure environment variables:

```bash
cp .env.example .env
```

Edit `.env` and add your OpenAI API key:

```
OPENAI_API_KEY=sk-...
INFINISPAN_HOST=127.0.0.1
INFINISPAN_PORT=11222
INFINISPAN_USERNAME=admin
INFINISPAN_PASSWORD=password
INFINISPAN_INDEX_NAME=scifi-movies
```

## Ingest the dataset

Load the 500 sci-fi movies into Infinispan:

```bash
npm run ingest
```

This chunks each movie (title + description), generates embeddings with `openai/text-embedding-3-small` (dimension 1536), and stores them in Infinispan with cosine similarity indexing.

## Run the agent

```bash
npm run dev
```

This starts Mastra Studio at `localhost:4111`. Open **Agents > Infinispan Agent** and ask questions like:

- *Find 5 movies about UFOs*
- *What sci-fi movies involve time travel?*
- *Recommend movies similar to Back to the Future*

The agent uses `createVectorQueryTool` to perform semantic search over Infinispan, then generates an answer using the retrieved context.

## How it works

1. **Vector conversion**: The user's question is converted into an embedding using OpenAI's `text-embedding-3-small` model
2. **Vector search**: The embedding is used to query Infinispan via kNN search (cosine similarity)
3. **Result retrieval**: Infinispan returns the most relevant movies
4. **Answer generation**: The retrieved movies and the original question are sent to `openai/gpt-5-nano` to generate the final answer

The RAG architecture allows using a smaller, cheaper LLM for the final answer because Infinispan handles the heavy lifting of finding relevant context.

## Project structure

```
src/
├── mastra/
│   ├── agents/
│   │   └── infinispan-agent.ts    # RAG agent with vector query tool
│   └── index.ts                   # Mastra configuration
└── utility/
    └── store.ts                   # Movie ingestion script
data/
└── 500_scifi_movies.jsonl         # Dataset
```

## Cleanup

```bash
docker compose down
```
