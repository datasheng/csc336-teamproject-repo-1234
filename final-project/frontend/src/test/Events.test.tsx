import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Events } from '../pages/Events';
import * as eventsApi from '../api/events';
import { EventDTO } from '../api/events';

vi.mock('../api/events');
vi.mock('../hooks/useEventUpdates', () => ({
  useEventUpdates: vi.fn(),
}));

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('Events Page', () => {
  const mockEvents: EventDTO[] = [
    {
      id: 1,
      organizerId: 1,
      organizerName: 'Computer Science Club',
      campusId: 1,
      campusName: 'Harvard University',
      capacity: 100,
      description: 'Introduction to Machine Learning Workshop',
      startTime: '2025-12-20T14:00:00',
      endTime: '2025-12-20T17:00:00',
      costs: [{ type: 'student', cost: 0 }],
      ticketsSold: 25,
      availableCapacity: 75,
    },
    {
      id: 2,
      organizerId: 2,
      organizerName: 'Music Society',
      campusId: 2,
      campusName: 'Stanford University',
      capacity: 200,
      description: 'Winter Concert',
      startTime: '2025-12-21T19:00:00',
      endTime: '2025-12-21T22:00:00',
      costs: [{ type: 'student', cost: 12 }],
      ticketsSold: 50,
      availableCapacity: 150,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should display loading state initially', () => {
    vi.spyOn(eventsApi, 'getEvents').mockImplementation(() => new Promise(() => {}));
    const { getByText } = renderWithRouter(<Events />);
    expect(getByText('Loading events...')).toBeInTheDocument();
  });

  it('should display events after loading', async () => {
    vi.spyOn(eventsApi, 'getEvents').mockResolvedValue(mockEvents);
    const { getByText } = renderWithRouter(<Events />);

    await waitFor(() => {
      expect(getByText('Introduction to Machine Learning Workshop')).toBeInTheDocument();
      expect(getByText('Winter Concert')).toBeInTheDocument();
    });
  });

  it('should display error message on fetch failure', async () => {
    vi.spyOn(eventsApi, 'getEvents').mockRejectedValue(new Error('Network Error'));
    const { getByText } = renderWithRouter(<Events />);

    await waitFor(() => {
      expect(getByText('Failed to load events. Please try again later.')).toBeInTheDocument();
    });
  });

  it('should display no events message when list is empty', async () => {
    vi.spyOn(eventsApi, 'getEvents').mockResolvedValue([]);
    const { getByText } = renderWithRouter(<Events />);

    await waitFor(() => {
      expect(getByText('No events found matching your filters.')).toBeInTheDocument();
    });
  });

  it('should render page title', async () => {
    vi.spyOn(eventsApi, 'getEvents').mockResolvedValue(mockEvents);
    const { getByText } = renderWithRouter(<Events />);

    await waitFor(() => {
      expect(getByText('Campus Events')).toBeInTheDocument();
    });
  });

  it('should render EventFilters component', async () => {
    vi.spyOn(eventsApi, 'getEvents').mockResolvedValue(mockEvents);
    const { getByText } = renderWithRouter(<Events />);

    await waitFor(() => {
      expect(getByText('Filter Events')).toBeInTheDocument();
    });
  });

  it('should call getEvents on mount', async () => {
    const getEventsSpy = vi.spyOn(eventsApi, 'getEvents').mockResolvedValue(mockEvents);
    renderWithRouter(<Events />);

    await waitFor(() => {
      expect(getEventsSpy).toHaveBeenCalled();
    });
  });
});
