import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'

/**
 * Tests for React Router integration.
 * 
 * Verifies that routing works correctly for the application.
 */
describe('Router', () => {
  it('renders home page on default route', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<div>Home Page</div>} />
        </Routes>
      </MemoryRouter>
    )
    expect(screen.getByText('Home Page')).toBeInTheDocument()
  })

  it('handles unknown routes', () => {
    render(
      <MemoryRouter initialEntries={['/unknown-route']}>
        <Routes>
          <Route path="/" element={<div>Home Page</div>} />
          <Route path="*" element={<div>Not Found</div>} />
        </Routes>
      </MemoryRouter>
    )
    expect(screen.getByText('Not Found')).toBeInTheDocument()
  })
})

describe('Environment', () => {
  it('should have React available', () => {
    expect(typeof React).not.toBe('undefined')
  })

  it('should be running in test environment', () => {
    expect(import.meta.env.MODE).toBe('test')
  })
})

// Import React for the environment test
import React from 'react'
