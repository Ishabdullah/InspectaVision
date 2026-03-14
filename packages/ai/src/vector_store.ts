import { ChromaClient, OpenAIEmbeddingFunction } from 'chromadb';
import { generateEmbedding } from './embeddings.js';

const CHROMA_URL = process.env.CHROMA_URL || 'http://localhost:8000';

class VectorStore {
  private client: ChromaClient;
  private collectionName = 'internachi_sop';

  constructor() {
    this.client = new ChromaClient({ path: CHROMA_URL });
  }

  async getOrCreateCollection() {
    return await this.client.getOrCreateCollection({
      name: this.collectionName,
    });
  }

  async addChunks(chunks: Array<{ id: string; text: string; metadata: any }>) {
    const collection = await this.getOrCreateCollection();
    
    const ids = chunks.map(c => c.id);
    const documents = chunks.map(c => c.text);
    const metadatas = chunks.map(c => c.metadata);
    
    // We'll generate embeddings in batches
    const embeddings = await Promise.all(
      documents.map(doc => generateEmbedding(doc))
    );

    await collection.add({
      ids,
      metadatas,
      documents,
      embeddings,
    });
  }

  async query(text: string, nResults: number = 5) {
    const collection = await this.getOrCreateCollection();
    const queryEmbedding = await generateEmbedding(text);

    return await collection.query({
      queryEmbeddings: [queryEmbedding],
      nResults,
    });
  }
}

export const vectorStore = new VectorStore();
