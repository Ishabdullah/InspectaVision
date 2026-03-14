import * as fs from 'fs/promises';
import { vectorStore } from './vector_store.js';
import { Section } from './scraper.js';

async function ingest() {
  const data = await fs.readFile('internachi_sop.json', 'utf-8');
  const sections: Section[] = JSON.parse(data);

  const chunks = sections.flatMap((section, sectionIdx) => {
    return section.content.map((text, textIdx) => ({
      id: `section-${sectionIdx}-chunk-${textIdx}`,
      text: `${section.title}: ${text}`,
      metadata: {
        sectionId: section.id,
        title: section.title,
      }
    }));
  });

  console.log(`Ingesting ${chunks.length} chunks into ChromaDB...`);
  await vectorStore.addChunks(chunks);
  console.log('Ingestion complete.');
}

ingest().catch(console.error);
