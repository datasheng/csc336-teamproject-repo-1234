import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { MyTicketsPage } from '../pages/MyTicketsPage';
import * as ticketsApi from '../api/tickets';

vi.mock('../api/tickets');

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('MyTicketsPage', () => {
  const mockTickets = [
    {
      eventId: 1,
      type: 'student',
      eventDescription: 'Machine Learning Workshop',
      organizerName: 'CS Club',
      cost: 0,
      startTime: '2025-12-20T14:00:00',
      endTime: '2025-12-20T17:00:00',
    },
    {
      eventId: 2,
      type: 'general',
      eventDescription: 'Winter Concert',
      organizerName: 'Music Society',
      cost: 15,
      startTime: '2025-11-15T19:00:00',
      endTime: '2025-11-15T22:00:00',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should display loading state initially', () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockImplementation(() => new Promise(() => {}));
    const { getByText } = renderWithRouter(<MyTicketsPage />);
    expect(getByText('Loading tickets...')).toBeInTheDocument();
  });

  it('should display tickets after loading', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('Machine Learning Workshop')).toBeInTheDocument();
      expect(getByText('Winter Concert')).toBeInTheDocument();
    });
  });

  it('should display error message on fetch failure', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockRejectedValue(new Error('Network Error'));
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('Failed to load tickets. Please try again later.')).toBeInTheDocument();
    });
  });

  it('should display no tickets message when list is empty', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([]);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('No tickets yet')).toBeInTheDocument();
      expect(getByText(/Start by browsing/)).toBeInTheDocument();
    });
  });

  it('should display Browse Events button when no tickets', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([]);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('Browse Events')).toBeInTheDocument();
    });
  });

  it('should navigate to events when clicking Browse Events', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([]);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('Browse Events')).toBeInTheDocument();
    });

    const browseButton = getByText('Browse Events');
    fireEvent.click(browseButton);

    expect(mockNavigate).toHaveBeenCalledWith('/events');
  });

  it('should display ticket details', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText(/CS Club/)).toBeInTheDocument();
      expect(getByText(/Music Society/)).toBeInTheDocument();
      expect(getByText('student')).toBeInTheDocument();
      expect(getByText('general')).toBeInTheDocument();
    });
  });

  it('should display free ticket correctly', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getAllByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      const freeLabels = getAllByText('Free');
      expect(freeLabels.length).toBeGreaterThan(0);
    });
  });

  it('should display paid ticket correctly', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('$15.00')).toBeInTheDocument();
    });
  });

  it('should mark past events', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('Past Event')).toBeInTheDocument();
    });
  });

  it('should navigate to event details when clicking ticket', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('Machine Learning Workshop')).toBeInTheDocument();
    });

    const ticketCard = getByText('Machine Learning Workshop').closest('div[class*="cursor-pointer"]');
    fireEvent.click(ticketCard!);

    expect(mockNavigate).toHaveBeenCalledWith('/events/1');
  });

  it('should display page title', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText('My Tickets')).toBeInTheDocument();
    });
  });

  it('should sort tickets by event date', async () => {
    const unsortedTickets = [
      {
        eventId: 1,
        type: 'student',
        eventDescription: 'Future Event',
        organizerName: 'CS Club',
        cost: 0,
        startTime: '2025-12-20T14:00:00',
        endTime: '2025-12-20T17:00:00',
      },
      {
        eventId: 2,
        type: 'general',
        eventDescription: 'Past Event',
        organizerName: 'Music Society',
        cost: 15,
        startTime: '2025-11-15T19:00:00',
        endTime: '2025-11-15T22:00:00',
      },
    ];

    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(unsortedTickets);
    const { container } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      const tickets = container.querySelectorAll('h2');
      expect(tickets[0]).toHaveTextContent('Past Event');
      expect(tickets[1]).toHaveTextContent('Future Event');
    });
  });

  it('should display View Details link', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getAllByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      const viewDetailsLinks = getAllByText(/View Details/);
      expect(viewDetailsLinks.length).toBe(mockTickets.length);
    });
  });

  it('should display organizer names', async () => {
    vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue(mockTickets);
    const { getByText } = renderWithRouter(<MyTicketsPage />);

    await waitFor(() => {
      expect(getByText(/Organized by CS Club/)).toBeInTheDocument();
      expect(getByText(/Organized by Music Society/)).toBeInTheDocument();
    });
  });
});
