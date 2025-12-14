import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { EventDetails } from '../pages/EventDetails';
import * as eventsApi from '../api/events';
import { EventDTO } from '../api/events';

vi.mock('../api/events');
vi.mock('../hooks/useEventUpdates', () => ({
  useEventUpdates: vi.fn(),
}));

describe('EventDetails Page', () => {
  const mockEvent: EventDTO = {
    id: 1,
    organizerId: 1,
    organizerName: 'Computer Science Club',
    campusId: 1,
    campusName: 'Harvard University',
    capacity: 100,
    description: 'Introduction to Machine Learning Workshop',
    startTime: '2025-12-20T14:00:00',
    endTime: '2025-12-20T17:00:00',
    costs: [
      { type: 'student', cost: 0 },
      { type: 'general', cost: 10 },
    ],
    ticketsSold: 25,
    availableCapacity: 75,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderWithRouter = (eventId: string = '1') => {
    return render(
      <MemoryRouter initialEntries={[`/events/${eventId}`]}>
        <Routes>
          <Route path="/events/:id" element={<EventDetails />} />
        </Routes>
      </MemoryRouter>
    );
  };

  it('should display loading state initially', () => {
    vi.spyOn(eventsApi, 'getEventById').mockImplementation(() => new Promise(() => {}));
    const { getByText } = renderWithRouter();
    expect(getByText('Loading event details...')).toBeInTheDocument();
  });

  it('should display event details after loading', async () => {
    vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
    const { getByText, getAllByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText('Introduction to Machine Learning Workshop')).toBeInTheDocument();
      expect(getByText('Computer Science Club')).toBeInTheDocument();
      expect(getAllByText('Harvard University').length).toBeGreaterThan(0);
    });
  });

  it('should display error message on fetch failure', async () => {
    vi.spyOn(eventsApi, 'getEventById').mockRejectedValue(new Error('Network Error'));
    const { getByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText('Failed to load event details. Please try again later.')).toBeInTheDocument();
    });
  });

  it('should display capacity information', async () => {
    vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
    const { getByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText('100')).toBeInTheDocument();
      expect(getByText('25')).toBeInTheDocument();
      expect(getByText('75')).toBeInTheDocument();
    });
  });

  it('should display ticket types with prices', async () => {
    vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
    const { getByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText('student')).toBeInTheDocument();
      expect(getByText('general')).toBeInTheDocument();
      expect(getByText('Free')).toBeInTheDocument();
      expect(getByText('$10.00')).toBeInTheDocument();
    });
  });

  it('should display Purchase Ticket button', async () => {
    vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
    const { getByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText('Purchase Ticket')).toBeInTheDocument();
    });
  });

  it('should display Sold Out when no capacity available', async () => {
    const soldOutEvent = {
      ...mockEvent,
      ticketsSold: 100,
      availableCapacity: 0,
    };
    vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(soldOutEvent);
    const { getByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText('Sold Out')).toBeInTheDocument();
    });
  });

  it('should display free event message when no costs', async () => {
    const freeEvent = {
      ...mockEvent,
      costs: [],
    };
    vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(freeEvent);
    const { getByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText('Free Event')).toBeInTheDocument();
      expect(getByText('No tickets required')).toBeInTheDocument();
    });
  });

  it('should display Back to Events button', async () => {
    vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
    const { getByText } = renderWithRouter();

    await waitFor(() => {
      expect(getByText(/Back to Events/)).toBeInTheDocument();
    });
  });

  it('should call getEventById with correct id', async () => {
    const getEventByIdSpy = vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
    renderWithRouter('123');

    await waitFor(() => {
      expect(getEventByIdSpy).toHaveBeenCalledWith(123);
    });
  });
});
