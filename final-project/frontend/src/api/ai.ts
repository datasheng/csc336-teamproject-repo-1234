/**
 * AI-powered event generation using OpenAI API
 */

export interface GeneratedEventData {
  description: string;
  capacity: number;
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  costs: { type: string; cost: number }[];
  tags: string[];
}

const OPENAI_API_KEY = import.meta.env.VITE_OPENAI_API_KEY;

export async function generateEventFromDescription(
  prompt: string,
  availableTags: string[]
): Promise<GeneratedEventData> {
  if (!OPENAI_API_KEY) {
    throw new Error('OpenAI API key is not configured. Please add VITE_OPENAI_API_KEY to your .env file.');
  }

  const systemPrompt = `You are an assistant that generates structured event data for a campus events platform. 
Given a user's description of an event, generate a complete event with all necessary details.

Available tags to choose from: ${availableTags.join(', ')}

You must respond with ONLY valid JSON in this exact format (no markdown, no code blocks):
{
  "description": "A clear, professional event title and description (1-2 sentences)",
  "capacity": <number between 20 and 1000>,
  "startDate": "YYYY-MM-DD",
  "startTime": "HH:MM",
  "endDate": "YYYY-MM-DD", 
  "endTime": "HH:MM",
  "costs": [
    {"type": "General", "cost": <number>},
    {"type": "Student", "cost": <number>}
  ],
  "tags": ["tag1", "tag2"]
}

Guidelines:
- Set reasonable dates in the near future (within 1-6 months from today)
- Event duration should be realistic (1-4 hours typically)
- Include 1-3 ticket types with reasonable pricing (free events have cost: 0)
- Select 1-4 relevant tags from the available list
- Make the description professional and engaging`;

  const response = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${OPENAI_API_KEY}`,
    },
    body: JSON.stringify({
      model: 'gpt-4o-mini',
      messages: [
        { role: 'system', content: systemPrompt },
        { role: 'user', content: `Generate event data for: ${prompt}` },
      ],
      temperature: 0.7,
      max_tokens: 500,
    }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error?.message || `OpenAI API error: ${response.status}`);
  }

  const data = await response.json();
  const content = data.choices?.[0]?.message?.content;

  if (!content) {
    throw new Error('No response from OpenAI');
  }

  try {
    // Parse the JSON response, handling potential markdown code blocks
    let jsonString = content.trim();
    if (jsonString.startsWith('```')) {
      jsonString = jsonString.replace(/```json?\n?/g, '').replace(/```$/g, '').trim();
    }
    
    const parsed = JSON.parse(jsonString) as GeneratedEventData;
    
    // Validate required fields
    if (!parsed.description || !parsed.startDate || !parsed.startTime || 
        !parsed.endDate || !parsed.endTime || !parsed.costs || !Array.isArray(parsed.costs)) {
      throw new Error('Invalid response structure');
    }
    
    // Filter tags to only include valid ones
    parsed.tags = (parsed.tags || []).filter(tag => availableTags.includes(tag));
    
    return parsed;
  } catch (parseError) {
    console.error('Failed to parse AI response:', content);
    throw new Error('Failed to parse AI response. Please try again.');
  }
}
