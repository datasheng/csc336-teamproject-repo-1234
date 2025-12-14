import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { EventDetails } from '../pages/EventDetails';
import * as eventsApi from '../api/events';
import * as ticketsApi from '../api/tickets';
import { EventDTO } from '../api/events';
import { UserTicketDTO } from '../api/tickets';

/**
 * Tests for ticket purchase state logic.
 * 
 * These tests verify that:
 * - Users who already have a ticket see "View My Ticket" instead of "Purchase Ticket"
 * - Users without a ticket see "Purchase Ticket" button
 * - Sold out events show "Sold Out" regardless of ticket ownership
 * - The correct ticket information is displayed when user has a ticket
 */

vi.mock('../api/events');
vi.mock('../api/tickets');
vi.mock('../hooks/useEventUpdates', () => ({
  useEventUpdates: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Ticket Purchase State Logic', () => {
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

  const mockUserTicket: UserTicketDTO = {
    eventId: 1,
    type: 'student',
    eventDescription: 'Introduction to Machine Learning Workshop',
    organizerName: 'Computer Science Club',
    cost: 0,
    startTime: '2025-12-20T14:00:00',
    endTime: '2025-12-20T17:00:00',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
  });

  const renderEventDetails = (eventId: string = '1') => {
    return render(
      <MemoryRouter initialEntries={[`/events/${eventId}`]}>
        <Routes>
          <Route path="/events/:id" element={<EventDetails />} />
        </Routes>
      </MemoryRouter>
    );
  };

  describe('when user does NOT have a ticket', () => {
    beforeEach(() => {
      vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
      vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([]);
    });

    it('should display "Purchase Ticket" button', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('Purchase Ticket')).toBeInTheDocument();
      });
    });

    it('should NOT display "View My Ticket" button', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.queryByText('View My Ticket')).not.toBeInTheDocument();
      });
    });

    it('should NOT display "already have a ticket" message', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.queryByText(/already have a ticket/)).not.toBeInTheDocument();
      });
    });

    it('should enable Purchase button when capacity is available', async () => {
      renderEventDetails();

      await waitFor(() => {
        const purchaseButton = screen.getByText('Purchase Ticket');
        expect(purchaseButton).not.toBeDisabled();
      });
    });
  });

  describe('when user HAS a ticket', () => {
    beforeEach(() => {
      vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
      vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([mockUserTicket]);
    });

    it('should display "View My Ticket" button instead of "Purchase Ticket"', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('View My Ticket')).toBeInTheDocument();
        expect(screen.queryByText('Purchase Ticket')).not.toBeInTheDocument();
      });
    });

    it('should display "already have a ticket" message', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText(/already have a ticket for this event/)).toBeInTheDocument();
      });
    });

    it('should display the ticket type in the message', async () => {
      renderEventDetails();

      await waitFor(() => {
        // Look for the ticket type in the "already have a ticket" message context
        expect(screen.getByText(/Ticket type:/)).toBeInTheDocument();
      });
    });

    it('should navigate to my-tickets page when View My Ticket is clicked', async () => {
      renderEventDetails();

      await waitFor(() => {
        const viewTicketButton = screen.getByText('View My Ticket');
        viewTicketButton.click();
      });

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/my-tickets');
      });
    });
  });

  describe('when user has a PAID ticket', () => {
    const paidTicket: UserTicketDTO = {
      ...mockUserTicket,
      type: 'general',
      cost: 10,
    };

    beforeEach(() => {
      vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
      vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([paidTicket]);
    });

    it('should display the paid amount in the message', async () => {
      renderEventDetails();

      await waitFor(() => {
        // Look for "Paid: $10.00" text in the ticket info message
        expect(screen.getByText(/Paid: \$10\.00/)).toBeInTheDocument();
      });
    });
  });

  describe('when event is SOLD OUT', () => {
    const soldOutEvent = {
      ...mockEvent,
      ticketsSold: 100,
      availableCapacity: 0,
    };

    describe('and user does NOT have a ticket', () => {
      beforeEach(() => {
        vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(soldOutEvent);
        vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([]);
      });

      it('should display "Sold Out" button', async () => {
        renderEventDetails();

        await waitFor(() => {
          expect(screen.getByText('Sold Out')).toBeInTheDocument();
        });
      });

      it('should disable the button', async () => {
        renderEventDetails();

        await waitFor(() => {
          const soldOutButton = screen.getByText('Sold Out');
          expect(soldOutButton).toBeDisabled();
        });
      });
    });

    describe('and user HAS a ticket', () => {
      beforeEach(() => {
        vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(soldOutEvent);
        vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([mockUserTicket]);
      });

      it('should display "View My Ticket" button (not Sold Out)', async () => {
        renderEventDetails();

        await waitFor(() => {
          expect(screen.getByText('View My Ticket')).toBeInTheDocument();
          expect(screen.queryByText('Sold Out')).not.toBeInTheDocument();
        });
      });
    });
  });

  describe('when user has tickets for OTHER events but NOT this one', () => {
    const otherEventTicket: UserTicketDTO = {
      eventId: 999, // Different event ID
      type: 'general',
      eventDescription: 'Some Other Event',
      organizerName: 'Other Club',
      cost: 5,
      startTime: '2025-12-25T10:00:00',
      endTime: '2025-12-25T12:00:00',
    };

    beforeEach(() => {
      vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
      vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([otherEventTicket]);
    });

    it('should display "Purchase Ticket" button', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('Purchase Ticket')).toBeInTheDocument();
      });
    });

    it('should NOT display "already have a ticket" message', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.queryByText(/already have a ticket/)).not.toBeInTheDocument();
      });
    });
  });

  describe('while loading ticket status', () => {
    beforeEach(() => {
      vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
      // Make getUserTickets hang to simulate loading
      vi.spyOn(ticketsApi, 'getUserTickets').mockImplementation(() => new Promise(() => {}));
    });

    it('should display loading message for button', async () => {
      renderEventDetails();

      await waitFor(() => {
        // First wait for event to load
        expect(screen.getByText('Introduction to Machine Learning Workshop')).toBeInTheDocument();
      });

      // Then check for ticket loading state
      expect(screen.getByText('Checking ticket status...')).toBeInTheDocument();
    });
  });

  describe('when ticket API fails', () => {
    beforeEach(() => {
      vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(mockEvent);
      vi.spyOn(ticketsApi, 'getUserTickets').mockRejectedValue(new Error('Network Error'));
    });

    it('should still show "Purchase Ticket" button (graceful degradation)', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('Purchase Ticket')).toBeInTheDocument();
      });
    });

    it('should NOT block the user from viewing event details', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('Introduction to Machine Learning Workshop')).toBeInTheDocument();
        expect(screen.getByText('Computer Science Club')).toBeInTheDocument();
      });
    });
  });

  describe('Free event handling', () => {
    const freeEvent: EventDTO = {
      ...mockEvent,
      costs: [],
    };

    beforeEach(() => {
      vi.spyOn(eventsApi, 'getEventById').mockResolvedValue(freeEvent);
      vi.spyOn(ticketsApi, 'getUserTickets').mockResolvedValue([]);
    });

    it('should display "Free Event" message', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('Free Event')).toBeInTheDocument();
      });
    });

    it('should display "No tickets required" message', async () => {
      renderEventDetails();

      await waitFor(() => {
        expect(screen.getByText('No tickets required')).toBeInTheDocument();
      });
    });
  });
});
