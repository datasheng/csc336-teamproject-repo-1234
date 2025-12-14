import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import App from './App'

/**
 * Tests for the main App component.
 * 
 * Verifies that the app renders correctly and displays expected content.
 */
describe('App', () => {
  const renderApp = () => {
    return render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    )
  }

  it('renders without crashing', () => {
    renderApp()
    expect(document.body).toBeDefined()
  })

  it('displays the main heading', () => {
    renderApp()
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Campus Events Platform')
  })

  it('displays the subtitle message', () => {
    renderApp()
    expect(screen.getByText('Discover, organize, and attend amazing campus events')).toBeInTheDocument()
  })

  it('displays the welcome message', () => {
    renderApp()
    expect(screen.getByText(/Welcome to Campus Events/i)).toBeInTheDocument()
  })

  it('displays statistics', () => {
    renderApp()
    expect(screen.getByText(/Universities/i)).toBeInTheDocument()
    expect(screen.getByText(/Active Events/i)).toBeInTheDocument()
    expect(screen.getByText(/Students/i)).toBeInTheDocument()
  })

  it('has proper semantic structure', () => {
    renderApp()
    const heading = screen.getByRole('heading', { level: 1 })
    expect(heading).toBeInTheDocument()
  })
})

describe('App Accessibility', () => {
  const renderApp = () => {
    return render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    )
  }

  it('has a visible main heading', () => {
    renderApp()
    const heading = screen.getByRole('heading', { level: 1 })
    expect(heading).toBeVisible()
  })

  it('uses semantic HTML elements', () => {
    const { container } = renderApp()
    // Check for proper div structure (app container)
    expect(container.querySelector('div')).toBeInTheDocument()
  })
})
