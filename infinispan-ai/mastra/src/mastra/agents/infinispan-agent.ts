import { Agent } from "@mastra/core/agent";
import { InfinispanVector } from '@infinispan/mastra';
import { createVectorQueryTool } from '@mastra/rag';
import { ModelRouterEmbeddingModel } from "@mastra/core/llm";
import { Memory } from "@mastra/memory";

const host = process.env.INFINISPAN_HOST || '127.0.0.1';
const port = parseInt(process.env.INFINISPAN_PORT || '11222');
const username = process.env.INFINISPAN_USERNAME || 'admin';
const password = process.env.INFINISPAN_PASSWORD || 'password';
const indexName = process.env.INFINISPAN_INDEX_NAME || 'scifi-movies';

const infinispanVector = new InfinispanVector({
  id: 'infinispan-vector',
  host,
  port,
  username,
  password,
});

const vectorQueryTool = createVectorQueryTool({
  vectorStore: infinispanVector,
  indexName,
  model: new ModelRouterEmbeddingModel("openai/text-embedding-3-small")
});

export const infinispanAgent = new Agent({
  id: "infinispan-agent",
  name: "Infinispan Agent",
  instructions: `You are a helpful assistant that answers questions based on the provided context.
Follow these steps for each response:

1. First, carefully analyze the retrieved context chunks and identify key information.
2. Break down your thinking process about how the retrieved information relates to the query.
3. Draw conclusions based only on the evidence in the retrieved context.
4. If the retrieved chunks don't contain enough information, explicitly state what's missing.

Format your response as:
THOUGHT PROCESS:
- Step 1: [Initial analysis of retrieved chunks]
- Step 2: [Reasoning based on chunks]

FINAL ANSWER:
[Your concise answer based on the retrieved context]

Important: When asked to answer a question, please base your answer only on the context provided in the tool.
If the context doesn't contain enough information to fully answer the question, please state that explicitly and stop it.
Do not add more information than what is present in the retrieved chunks.
Remember: Explain how you're using the retrieved information to reach your conclusions.
`,
  model: 'openai/gpt-5-nano',
  tools: { vectorQueryTool },
  memory: new Memory(),
});
