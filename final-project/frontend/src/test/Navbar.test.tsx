import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { AuthProvider } from '../context/AuthContext';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{component}</AuthProvider>
    </BrowserRouter>
  );
};

describe('Navbar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should render login/signup navbar when user is not authenticated', () => {
    const { getByText } = renderWithProviders(<Navbar />);
    expect(getByText('Sign In')).toBeInTheDocument();
    expect(getByText('Get Started')).toBeInTheDocument();
    expect(getByText('Campus Events')).toBeInTheDocument();
  });

  it('should render when user is authenticated', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    expect(getByText('Campus Events')).toBeInTheDocument();
  });

  it('should display Browse Events link', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    expect(getByText('Browse Events')).toBeInTheDocument();
  });

  it('should display My Tickets link', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    expect(getByText('My Tickets')).toBeInTheDocument();
  });

  it('should display Profile link', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    expect(getByText('Profile')).toBeInTheDocument();
  });

  it('should display Logout button', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    expect(getByText('Logout')).toBeInTheDocument();
  });

  it('should display user name', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    expect(getByText('John Doe')).toBeInTheDocument();
  });

  it('should navigate to login and clear storage on logout', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    const logoutButton = getByText('Logout');

    fireEvent.click(logoutButton);

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('should have correct link to Browse Events', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    const browseLink = getByText('Browse Events').closest('a');
    expect(browseLink?.getAttribute('href')).toBe('/events');
  });

  it('should have correct link to My Tickets', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    const ticketsLink = getByText('My Tickets').closest('a');
    expect(ticketsLink?.getAttribute('href')).toBe('/my-tickets');
  });

  it('should have correct link to Profile', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    const profileLink = getByText('Profile').closest('a');
    expect(profileLink?.getAttribute('href')).toBe('/profile');
  });

  it('should have correct link to home page', () => {
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));

    const { getByText } = renderWithProviders(<Navbar />);
    const homeLink = getByText('Campus Events').closest('a');
    expect(homeLink?.getAttribute('href')).toBe('/');
  });
});
