import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { EventDetails } from './EventDetails';
import * as eventsApi from '../api/events';
import * as ticketsApi from '../api/tickets';
import * as useEventUpdatesModule from '../hooks/useEventUpdates';

// Mock the APIs
vi.mock('../api/events', () => ({
  getEventById: vi.fn(),
}));

vi.mock('../api/tickets', () => ({
  getUserTickets: vi.fn(),
}));

// Store the message callback for testing
let capturedOnMessage: ((message: useEventUpdatesModule.EventUpdateMessage) => void) | null = null;
let capturedTopic: string = '';

// Mock the WebSocket hook
vi.mock('../hooks/useEventUpdates', () => ({
  useEventUpdates: vi.fn((topic, onMessage) => {
    capturedTopic = topic;
    capturedOnMessage = onMessage;
  }),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockEvent: eventsApi.EventDTO = {
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
  ticketsSold: 30,
  availableCapacity: 70,
};

describe('EventDetails Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    capturedOnMessage = null;
    capturedTopic = '';
    (eventsApi.getEventById as ReturnType<typeof vi.fn>).mockResolvedValue(mockEvent);
    (ticketsApi.getUserTickets as ReturnType<typeof vi.fn>).mockResolvedValue([]);
  });

  const renderEventDetails = (eventId: string = '1') => {
    return render(
      <MemoryRouter initialEntries={[`/events/${eventId}`]}>
        <Routes>
          <Route path="/events/:id" element={<EventDetails />} />
          <Route path="/events" element={<div>Events Page</div>} />
        </Routes>
      </MemoryRouter>
    );
  };

  it('renders event details from API', async () => {
    renderEventDetails();

    await waitFor(() => {
      expect(screen.getByText('Tech Talk')).toBeInTheDocument();
    });
  });

  it('subscribes to event-specific WebSocket topic', async () => {
    renderEventDetails('1');

    await waitFor(() => {
      expect(capturedTopic).toBe('/topic/event/1');
    });
  });

  describe('Real-time Event Updates', () => {
    it('updates capacity when CAPACITY_UPDATED is received', async () => {
      renderEventDetails();

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

      // Verify updated values appear
      await waitFor(() => {
        expect(screen.getByText('75')).toBeInTheDocument();
      });
    });

    it('shows deleted banner when EVENT_DELETED is received', async () => {
      renderEventDetails();

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
        expect(screen.getByText('Event Deleted')).toBeInTheDocument();
      });
    });

    it('shows cancelled banner when EVENT_CANCELLED is received', async () => {
      renderEventDetails();

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
        // Check for the descriptive text in the banner to avoid ambiguity with button
        expect(screen.getByText('This event has been cancelled by the organizer. Ticket purchases are disabled.')).toBeInTheDocument();
      });
    });

    it('refetches event on EVENT_UPDATED', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('Tech Talk')).toBeInTheDocument();
      });

      // Reset mock to track new calls
      (eventsApi.getEventById as ReturnType<typeof vi.fn>).mockClear();

      // Simulate event update
      act(() => {
        if (capturedOnMessage) {
          capturedOnMessage({
            type: 'EVENT_UPDATED',
            eventId: 1,
          });
        }
      });

      await waitFor(() => {
        expect(eventsApi.getEventById).toHaveBeenCalled();
      });
    });
  });
});
