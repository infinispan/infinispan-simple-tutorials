import { MDocument } from "@mastra/rag";
import { ModelRouterEmbeddingModel } from "@mastra/core/llm";
import { InfinispanVector } from "@infinispan/mastra";
import "dotenv/config";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const host = process.env.INFINISPAN_HOST || '127.0.0.1';
const port = parseInt(process.env.INFINISPAN_PORT || '11222');
const username = process.env.INFINISPAN_USERNAME || 'admin';
const password = process.env.INFINISPAN_PASSWORD || 'password';
const indexName = process.env.INFINISPAN_INDEX_NAME || 'scifi-movies';

const vectorStore = new InfinispanVector({
  id: "infinispan-vector",
  host,
  port,
  username,
  password,
});

try {
  await vectorStore.createIndex({
    indexName,
    dimension: 1536,
  });
  console.log(`Created index: ${indexName}`);
} catch (error) {
  const message = error instanceof Error ? error.message : String(error);
  if (message.includes("already exists") || message.includes("ISPN")) {
    console.log(`Index already exists: ${indexName}`);
  } else {
    throw error;
  }
}

const openaiEmbeddingModel = new ModelRouterEmbeddingModel("openai/text-embedding-3-small");

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const filePath = path.join(__dirname, "../../data/500_scifi_movies.jsonl");

type MovieRecord = {
  title: string;
  description: string;
  release_year?: number;
  director?: string;
  genre?: string;
};

const formatTime = (seconds: number): string => {
  const safe = Math.max(0, Math.floor(seconds));
  const mins = Math.floor(safe / 60);
  const secs = safe % 60;
  return `${mins}m ${secs}s`;
};

const progressBar = (progress: number, width = 24): string => {
  const filled = Math.round(progress * width);
  return `${"█".repeat(filled)}${"░".repeat(width - filled)}`;
};

const embedTextChunks = async (chunks: string[]): Promise<number[][]> => {
  if (chunks.length === 0) {
    return [];
  }

  const maxEmbeddingsPerCall = Number(await openaiEmbeddingModel.maxEmbeddingsPerCall) || chunks.length;
  const embeddings: number[][] = [];

  for (let i = 0; i < chunks.length; i += maxEmbeddingsPerCall) {
    const batch = chunks.slice(i, i + maxEmbeddingsPerCall);
    const { embeddings: batchEmbeddings } = await openaiEmbeddingModel.doEmbed({ values: batch });
    embeddings.push(...batchEmbeddings);
  }

  return embeddings;
};

const readMovies = async () => {
  const startedAt = Date.now();
  const data = fs.readFileSync(filePath, "utf-8");
  const lines = data
    .split("\n")
    .map(line => line.trim())
    .filter(Boolean);

  const total = lines.length;
  let processed = 0;
  let success = 0;
  let failed = 0;
  let totalChunks = 0;

  const renderProgress = (currentTitle?: string) => {
    const ratio = total === 0 ? 1 : processed / total;
    const elapsedSeconds = (Date.now() - startedAt) / 1000;
    const rate = processed > 0 ? processed / Math.max(elapsedSeconds, 1) : 0;
    const etaSeconds = rate > 0 ? (total - processed) / rate : 0;

    const line = [
      `Ingesting ${progressBar(ratio)} ${processed}/${total} (${Math.round(ratio * 100)}%)`,
      `ok:${success}`,
      `fail:${failed}`,
      `chunks:${totalChunks}`,
      `eta:${formatTime(etaSeconds)}`,
      currentTitle ? `current:${currentTitle}` : "",
    ]
      .filter(Boolean)
      .join(" | ");

    if (process.stdout.isTTY) {
      process.stdout.write(`\r${line.padEnd(180)}`);
      if (processed === total) {
        process.stdout.write("\n");
      }
    } else {
      console.log(line);
    }
  };

  console.log(`Starting ingestion of ${total} movies from ${path.basename(filePath)}...`);
  renderProgress();

  for (const line of lines) {
    try {
      const movie = JSON.parse(line) as MovieRecord;

      if (!movie.title || !movie.description) {
        throw new Error("missing title or description");
      }

      const metadata = {
        title: movie.title,
        description: movie.description,
        release_year: movie.release_year,
        director: movie.director,
        genre: movie.genre,
      };

      const doc = MDocument.fromText(`${movie.title}, ${movie.description}`, metadata);

      const chunks = await doc.chunk({
        strategy: "recursive",
        maxSize: 1000,
        overlap: 100,
      });

      const chunkTexts = chunks.map(chunk => chunk.text);
      const embeddings = await embedTextChunks(chunkTexts);
      totalChunks += chunkTexts.length;

      if (embeddings.length > 0) {
        await vectorStore.upsert({
          indexName,
          vectors: embeddings,
          metadata: doc.getMetadata(),
        });
      }

      success += 1;
      processed += 1;
      renderProgress(movie.title);
    } catch (error) {
      failed += 1;
      processed += 1;
      const message = error instanceof Error ? error.message : String(error);
      renderProgress();
      console.error(`\nFailed processing record ${processed}/${total}: ${message}`);
    }
  }

  const elapsed = (Date.now() - startedAt) / 1000;
  console.log(`Ingestion complete in ${formatTime(elapsed)}. Success: ${success}, Failed: ${failed}, Chunks: ${totalChunks}.`);
};

await readMovies();
