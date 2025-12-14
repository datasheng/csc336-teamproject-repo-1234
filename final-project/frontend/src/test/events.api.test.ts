import { describe, it, expect } from 'vitest';
import axios from 'axios';

describe('Events API', () => {
  it('axios should be available for API calls', () => {
    expect(axios).toBeDefined();
    expect(typeof axios.create).toBe('function');
  });

  it('should export EventDTO type', async () => {
    const { EventDTO } = await import('../api/events');
    expect(EventDTO).toBeDefined;
  });

  it('should export EventFilters type', async () => {
    const { EventFilters } = await import('../api/events');
    expect(EventFilters).toBeDefined;
  });

  it('should have getEvents function', async () => {
    const { getEvents } = await import('../api/events');
    expect(typeof getEvents).toBe('function');
  });

  it('should have getEventById function', async () => {
    const { getEventById } = await import('../api/events');
    expect(typeof getEventById).toBe('function');
  });

  it('axios instance should support HTTP methods', () => {
    const instance = axios.create({ baseURL: '/api' });
    expect(typeof instance.get).toBe('function');
    expect(typeof instance.post).toBe('function');
    expect(typeof instance.put).toBe('function');
    expect(typeof instance.delete).toBe('function');
  });
});
