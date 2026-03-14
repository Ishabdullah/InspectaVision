import { GoogleGenerativeAI, Part } from '@google/generative-ai';
import dotenv from 'dotenv';
import { z } from 'zod';

dotenv.config();

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || '');
const model = genAI.getGenerativeModel({ model: "gemini-1.5-pro" });

const FindingOutputSchema = z.object({
  findings: z.array(z.object({
    location: z.string(),
    severity: z.enum(['safety', 'major', 'minor', 'maintenance', 'info']),
    description: z.string(),
    recommendation: z.string(),
  }))
});

export async function analyzeInspectionCategory(
  categoryName: string,
  imageUrls: string[]
) {
  const prompt = `
    You are a professional InterNACHI certified home inspector.
    Analyze the attached photos for the category: ${categoryName}.
    Identify any defects or issues visible in the photos.
    For each finding, provide:
    1. Precise location
    2. Severity (safety, major, minor, maintenance, or info)
    3. Detailed professional description of the issue
    4. A clear recommendation for repair or further evaluation.

    Return the result strictly as a JSON object matching this schema:
    {
      "findings": [
        { "location": string, "severity": string, "description": string, "recommendation": string }
      ]
    }
  `;

  // Note: In production, we'd fetch the images and convert them to base64 Parts
  // For now, this is the core logic.
  const parts: Part[] = imageUrls.map(url => ({
    // This part requires actual image data in a real environment
    // For the scaffold, we focus on the prompt and logic
    inlineData: {
      data: "", // Placeholder
      mimeType: "image/jpeg"
    }
  }));

  const result = await model.generateContent([prompt, ...parts]);
  const response = await result.response;
  const text = response.text();
  
  // Basic cleaning of JSON response
  const cleanJson = text.replace(/```json|```/g, '').trim();
  return FindingOutputSchema.parse(JSON.parse(cleanJson));
}
