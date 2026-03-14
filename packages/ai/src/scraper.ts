import axios from 'axios';
import * as cheerio from 'cheerio';
import { z } from 'zod';

const SOP_URL = 'https://www.nachi.org/sop.htm';

const SectionSchema = z.object({
  title: z.string(),
  id: z.string().optional(),
  content: z.array(z.string()),
  subsections: z.array(z.any()),
});

export type Section = z.infer<typeof SectionSchema>;

async function scrapeSOP(): Promise<Section[]> {
  const { data } = await axios.get(SOP_URL);
  const $ = cheerio.load(data);
  const sections: Section[] = [];

  // Finding the main section content
  // Based on the nachi.org/sop.htm structure
  $('h3, h2').each((i, el) => {
    const title = $(el).text().trim();
    if (!title) return;

    const section: Section = {
      title,
      id: $(el).attr('id') || `section-${i}`,
      content: [],
      subsections: [],
    };

    let next = $(el).next();
    while (next.length && !next.is('h3, h2')) {
      if (next.is('p, ul, ol')) {
        section.content.push(next.text().trim());
      }
      next = next.next();
    }

    if (section.content.length > 0) {
      sections.push(section);
    }
  });

  return sections;
}

export { scrapeSOP };
