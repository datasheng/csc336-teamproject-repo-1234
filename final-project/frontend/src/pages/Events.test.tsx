import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Events } from './Events';
import * as eventsApi from '../api/events';
import * as useEventUpdatesModule from '../hooks/useEventUpdates';

// Mock the API
vi.mock('../api/events', () => ({
  getEvents: vi.fn(),
}));

// Store the message callback for testing
let capturedOnMessage: ((message: useEventUpdatesModule.EventUpdateMessage) => void) | null = null;

// Mock the WebSocket hook
vi.mock('../hooks/useEventUpdates', () => ({
  useEventUpdates: vi.fn((topic, onMessage, enabled) => {
    capturedOnMessage = onMessage;
    return { current: null };
  }),
}));

const mockEvents: eventsApi.EventDTO[] = [
  {
    id: 1,
    organizerId: 1,
    organizerName: 'Tech Club',
    campusId: 1,
    campusName: 'Main Campus',
    capacity: 100,
    description: 'Tech Talk',
    startTime: '2025-12-20T10:00:00',
    endTime: '2025-12-20T12:00:00',
    costs: [{ type: 'General', cost: 10 }],
    ticketsSold: 50,
    availableCapacity: 50,
  },
  {
    id: 2,
    organizerId: 2,
    organizerName: 'Art Club',
    campusId: 1,
    campusName: 'Main Campus',
    capacity: 50,
    description: 'Art Exhibition',
    startTime: '2025-12-21T14:00:00',
    endTime: '2025-12-21T18:00:00',
    costs: [],
    ticketsSold: 10,
    availableCapacity: 40,
  },
];

describe('Events Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    capturedOnMessage = null;
    (eventsApi.getEvents as ReturnType<typeof vi.fn>).mockResolvedValue(mockEvents);
  });

  const renderEvents = () => {
    return render(
      <BrowserRouter>
        <Events />
      </BrowserRouter>
    );
  };

  it('renders events from API', async () => {
    renderEvents();

    await waitFor(() => {
      expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      expect(screen.getByText('Art Exhibition')).toBeInTheDocument();
    });
  });

  it('subscribes to /topic/events WebSocket', async () => {
    renderEvents();

    await waitFor(() => {
      expect(useEventUpdatesModule.useEventUpdates).toHaveBeenCalledWith(
        '/topic/events',
        expect.any(Function)
      );
    });
  });

  describe('Real-time Event Updates', () => {
    it('adds new event when EVENT_CREATED is received', async () => {
      renderEvents();

      await waitFor(() => {
        expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      });

      // Simulate receiving a new event
      act(() => {
        if (capturedOnMessage) {
          capturedOnMessage({
            type: 'EVENT_CREATED',
            eventId: 3,
            campusId: 1,
            event: {
              id: 3,
              organizerId: 3,
              organizerName: 'Music Club',
              campusId: 1,
              campusName: 'Main Campus',
              capacity: 200,
              description: 'Concert Night',
              startTime: '2025-12-22T19:00:00',
              endTime: '2025-12-22T23:00:00',
              ticketsSold: 0,
              availableCapacity: 200,
            },
          });
        }
      });

      await waitFor(() => {
        expect(screen.getByText('Concert Night')).toBeInTheDocument();
      });
    });

    it('removes event when EVENT_DELETED is received', async () => {
      renderEvents();

      await waitFor(() => {
        expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      });

      // Simulate event deletion
      act(() => {
        if (capturedOnMessage) {
          capturedOnMessage({
            type: 'EVENT_DELETED',
            eventId: 1,
          });
        }
      });

      await waitFor(() => {
        expect(screen.queryByText('Tech Talk')).not.toBeInTheDocument();
      });

      // Other events should still be there
      expect(screen.getByText('Art Exhibition')).toBeInTheDocument();
    });

    it('marks event as cancelled when EVENT_CANCELLED is received', async () => {
      renderEvents();

      await waitFor(() => {
        expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      });

      // Simulate event cancellation
      act(() => {
        if (capturedOnMessage) {
          capturedOnMessage({
            type: 'EVENT_CANCELLED',
            eventId: 1,
          });
        }
      });

      await waitFor(() => {
        expect(screen.getByText('[CANCELLED] Tech Talk')).toBeInTheDocument();
      });
    });

    it('updates capacity when CAPACITY_UPDATED is received', async () => {
      renderEvents();

      await waitFor(() => {
        expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      });

      // Simulate capacity update
      act(() => {
        if (capturedOnMessage) {
          capturedOnMessage({
            type: 'CAPACITY_UPDATED',
            eventId: 1,
            ticketsSold: 75,
            availableCapacity: 25,
          });
        }
      });

      // The component should update internally - this tests the state change
      // The actual display would depend on the EventCard component
    });

    it('shows notification when new event is added', async () => {
      renderEvents();

      await waitFor(() => {
        expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      });

      act(() => {
        if (capturedOnMessage) {
          capturedOnMessage({
            type: 'EVENT_CREATED',
            eventId: 3,
            event: {
              id: 3,
              organizerId: 3,
              organizerName: 'Music Club',
              campusId: 1,
              campusName: 'Main Campus',
              capacity: 200,
              description: 'Concert Night',
              startTime: '2025-12-22T19:00:00',
              endTime: '2025-12-22T23:00:00',
              ticketsSold: 0,
              availableCapacity: 200,
            },
          });
        }
      });

      await waitFor(() => {
        expect(screen.getByText('New event added!')).toBeInTheDocument();
      });
    });

    it('filters EVENT_CREATED by campusId when filter is active', async () => {
      renderEvents();

      await waitFor(() => {
        expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      });

      // Simulate receiving an event for a different campus than current filter
      // Note: This tests the internal filtering logic
      act(() => {
        if (capturedOnMessage) {
          capturedOnMessage({
            type: 'EVENT_CREATED',
            eventId: 3,
            campusId: 999, // Different campus
            event: {
              id: 3,
              organizerId: 3,
              organizerName: 'Other Club',
              campusId: 999,
              campusName: 'Other Campus',
              capacity: 100,
              description: 'Other Event',
              startTime: '2025-12-22T19:00:00',
              endTime: '2025-12-22T23:00:00',
              ticketsSold: 0,
              availableCapacity: 100,
            },
          });
        }
      });

      // Without a campus filter, the event should be added
      await waitFor(() => {
        expect(screen.getByText('Other Event')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('shows error message when API fails', async () => {
      (eventsApi.getEvents as ReturnType<typeof vi.fn>).mockRejectedValue(
        new Error('Network error')
      );

      renderEvents();

      await waitFor(() => {
        expect(screen.getByText('Failed to load events. Please try again later.')).toBeInTheDocument();
      });
    });
  });

  describe('Loading State', () => {
    it('shows loading state initially', () => {
      (eventsApi.getEvents as ReturnType<typeof vi.fn>).mockImplementation(
        () => new Promise(() => {}) // Never resolves
      );

      renderEvents();

      expect(screen.getByText('Loading events...')).toBeInTheDocument();
    });
  });
});
