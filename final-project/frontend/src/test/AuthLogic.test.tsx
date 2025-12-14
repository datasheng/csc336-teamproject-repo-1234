import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { Navbar } from '../components/Navbar';
import App from '../App';

/**
 * Tests for authentication-related UI logic.
 * 
 * These tests verify that the UI correctly responds to authentication state:
 * - Navbar shows login/signup for unauthenticated users
 * - Navbar shows user info and logout for authenticated users
 * - Homepage shows appropriate content based on auth state
 * - Login/signup pages redirect authenticated users
 */

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Authentication UI Logic', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  const setAuthenticatedUser = () => {
    localStorage.setItem('token', 'test-token-123');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));
  };

  describe('Navbar Authentication States', () => {
    const renderNavbar = () => {
      return render(
        <MemoryRouter>
          <AuthProvider>
            <Navbar />
          </AuthProvider>
        </MemoryRouter>
      );
    };

    describe('when user is NOT authenticated', () => {
      it('should display the Sign In link', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('Sign In')).toBeInTheDocument();
        });
      });

      it('should display the Get Started button', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('Get Started')).toBeInTheDocument();
        });
      });

      it('should display Campus Events logo/title', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('Campus Events')).toBeInTheDocument();
        });
      });

      it('should NOT display Logout button', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.queryByText('Logout')).not.toBeInTheDocument();
        });
      });

      it('should NOT display user name', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
        });
      });

      it('should NOT display My Tickets link', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.queryByText('My Tickets')).not.toBeInTheDocument();
        });
      });

      it('should NOT display Browse Events link', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.queryByText('Browse Events')).not.toBeInTheDocument();
        });
      });
    });

    describe('when user IS authenticated', () => {
      beforeEach(() => {
        setAuthenticatedUser();
      });

      it('should display user name', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('John Doe')).toBeInTheDocument();
        });
      });

      it('should display Logout button', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('Logout')).toBeInTheDocument();
        });
      });

      it('should display Browse Events link', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('Browse Events')).toBeInTheDocument();
        });
      });

      it('should display My Tickets link', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('My Tickets')).toBeInTheDocument();
        });
      });

      it('should display Profile link', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('Profile')).toBeInTheDocument();
        });
      });

      it('should NOT display Sign In link', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.queryByText('Sign In')).not.toBeInTheDocument();
        });
      });

      it('should NOT display Get Started button', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.queryByText('Get Started')).not.toBeInTheDocument();
        });
      });

      it('should display user initials in avatar', async () => {
        renderNavbar();
        await waitFor(() => {
          expect(screen.getByText('JD')).toBeInTheDocument();
        });
      });
    });
  });

  describe('Homepage Authentication States', () => {
    const renderHomepage = () => {
      return render(
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      );
    };

    describe('when user is NOT authenticated', () => {
      it('should display "Get Started Free" button', async () => {
        renderHomepage();
        await waitFor(() => {
          expect(screen.getByText('Get Started Free')).toBeInTheDocument();
        });
      });

      it('should display "Sign In" button in hero section', async () => {
        renderHomepage();
        await waitFor(() => {
          // There may be multiple "Sign In" - one in navbar, one in hero
          const signInButtons = screen.getAllByText('Sign In');
          expect(signInButtons.length).toBeGreaterThanOrEqual(1);
        });
      });

      it('should display "Create Your Account" button', async () => {
        renderHomepage();
        await waitFor(() => {
          expect(screen.getByText('Create Your Account')).toBeInTheDocument();
        });
      });

      it('should NOT display "Welcome back" message', async () => {
        renderHomepage();
        await waitFor(() => {
          expect(screen.queryByText(/Welcome back/)).not.toBeInTheDocument();
        });
      });
    });

    describe('when user IS authenticated', () => {
      beforeEach(() => {
        setAuthenticatedUser();
      });

      it('should display "Welcome back" with user name', async () => {
        renderHomepage();
        await waitFor(() => {
          expect(screen.getByText(/Welcome back, John!/)).toBeInTheDocument();
        });
      });

      it('should display "Browse Events" button instead of "Get Started"', async () => {
        renderHomepage();
        await waitFor(() => {
          // Main CTA button should be "Browse Events"
          const browseEventsLinks = screen.getAllByText('Browse Events');
          expect(browseEventsLinks.length).toBeGreaterThanOrEqual(1);
        });
      });

      it('should display "My Tickets" button', async () => {
        renderHomepage();
        await waitFor(() => {
          const myTicketsLinks = screen.getAllByText('My Tickets');
          expect(myTicketsLinks.length).toBeGreaterThanOrEqual(1);
        });
      });

      it('should NOT display "Get Started Free" button', async () => {
        renderHomepage();
        await waitFor(() => {
          expect(screen.queryByText('Get Started Free')).not.toBeInTheDocument();
        });
      });

      it('should NOT display "Create Your Account" button', async () => {
        renderHomepage();
        await waitFor(() => {
          expect(screen.queryByText('Create Your Account')).not.toBeInTheDocument();
        });
      });

      it('should display "Explore Events" instead of "Create Your Account"', async () => {
        renderHomepage();
        await waitFor(() => {
          expect(screen.getByText('Explore Events')).toBeInTheDocument();
        });
      });
    });
  });

  describe('PublicRoute (Login/Signup) Redirect Logic', () => {
    describe('when user IS authenticated', () => {
      beforeEach(() => {
        setAuthenticatedUser();
      });

      it('should redirect from /login to /events', async () => {
        render(
          <MemoryRouter initialEntries={['/login']}>
            <App />
          </MemoryRouter>
        );

        await waitFor(() => {
          // Should not see login form
          expect(screen.queryByText('Welcome Back')).not.toBeInTheDocument();
          expect(screen.queryByPlaceholderText('you@example.com')).not.toBeInTheDocument();
        });
      });

      it('should redirect from /signup to /events', async () => {
        render(
          <MemoryRouter initialEntries={['/signup']}>
            <App />
          </MemoryRouter>
        );

        await waitFor(() => {
          // Should not see signup form
          expect(screen.queryByText('Create Account')).not.toBeInTheDocument();
        });
      });
    });

    describe('when user is NOT authenticated', () => {
      it('should show login form on /login', async () => {
        render(
          <MemoryRouter initialEntries={['/login']}>
            <App />
          </MemoryRouter>
        );

        await waitFor(() => {
          expect(screen.getByText('Welcome Back')).toBeInTheDocument();
          expect(screen.getByPlaceholderText('you@example.com')).toBeInTheDocument();
        });
      });

      it('should show signup form on /signup', async () => {
        render(
          <MemoryRouter initialEntries={['/signup']}>
            <App />
          </MemoryRouter>
        );

        await waitFor(() => {
          expect(screen.getByText('Create Account')).toBeInTheDocument();
        });
      });
    });
  });
});
