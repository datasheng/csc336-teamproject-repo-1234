import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'

/**
 * Tests for API utilities and HTTP client configuration.
 * 
 * These tests verify that axios is properly configured for API calls.
 */
describe('API Configuration', () => {
  it('axios should be available', () => {
    expect(axios).toBeDefined()
  })

  it('axios should have request methods', () => {
    expect(typeof axios.get).toBe('function')
    expect(typeof axios.post).toBe('function')
    expect(typeof axios.put).toBe('function')
    expect(typeof axios.delete).toBe('function')
  })

  it('axios should support creating instances', () => {
    const instance = axios.create({
      baseURL: 'http://localhost:8080/api',
      timeout: 10000,
    })
    expect(instance).toBeDefined()
    expect(typeof instance.get).toBe('function')
  })
})

describe('API Client Factory', () => {
  it('should create an API client with base URL', () => {
    const apiClient = axios.create({
      baseURL: '/api',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    expect(apiClient.defaults.baseURL).toBe('/api')
    expect(apiClient.defaults.headers['Content-Type']).toBe('application/json')
  })

  it('should allow setting authorization header', () => {
    const apiClient = axios.create({
      baseURL: '/api',
    })

    apiClient.defaults.headers.common['Authorization'] = 'Bearer test-token'
    expect(apiClient.defaults.headers.common['Authorization']).toBe('Bearer test-token')
  })
})

describe('API Error Handling', () => {
  it('should handle network errors gracefully', async () => {
    const mockError = new Error('Network Error')
    vi.spyOn(axios, 'get').mockRejectedValueOnce(mockError)

    await expect(axios.get('/api/nonexistent')).rejects.toThrow('Network Error')
    
    vi.restoreAllMocks()
  })
})

describe('Health Check API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should call health endpoint correctly', async () => {
    const mockResponse = { data: { status: 'ok' } }
    vi.spyOn(axios, 'get').mockResolvedValueOnce(mockResponse)

    const response = await axios.get('/api/health')
    
    expect(axios.get).toHaveBeenCalledWith('/api/health')
    expect(response.data.status).toBe('ok')
  })

  it('should handle health check failure', async () => {
    vi.spyOn(axios, 'get').mockRejectedValueOnce({
      response: { status: 503, data: { error: 'Service Unavailable' } }
    })

    await expect(axios.get('/api/health')).rejects.toMatchObject({
      response: { status: 503 }
    })
  })
})
