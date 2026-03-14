import { scrapeSOP } from './scraper.js';
import * as fs from 'fs/promises';
import * as path from 'path';

async function main() {
  console.log('Scraping InterNACHI SOP...');
  try {
    const sections = await scrapeSOP();
    const outputPath = path.resolve('internachi_sop.json');
    await fs.writeFile(outputPath, JSON.stringify(sections, null, 2));
    console.log(`Successfully scraped ${sections.length} sections.`);
    console.log(`Data saved to ${outputPath}`);
  } catch (error) {
    console.error('Error during scraping:', error);
  }
}

main();
