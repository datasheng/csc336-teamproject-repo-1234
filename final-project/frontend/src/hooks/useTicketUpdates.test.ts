import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { ReactNode } from 'react';

// Mock the auth context
const mockUser = { id: 123, firstName: 'Test', lastName: 'User', email: 'test@test.com', campusId: 1 };

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(() => ({
    user: mockUser,
    token: 'test-token',
    isAuthenticated: true,
    isLoading: false,
    login: vi.fn(),
    logout: vi.fn(),
  })),
}));

// Mock SockJS and STOMP with working pattern
const mockSubscribe = vi.fn();
const mockActivate = vi.fn();
const mockDeactivate = vi.fn();

vi.mock('@stomp/stompjs', () => {
  return {
    Client: function MockClient(_config: any) {
      return {
        onConnect: null,
        onStompError: null,
        onDisconnect: null,
        activate() {
          mockActivate();
          const self = this;
          setTimeout(() => {
            if (self.onConnect) {
              self.onConnect();
            }
          }, 0);
        },
        deactivate() {
          mockDeactivate();
          return Promise.resolve();
        },
        subscribe(topic: string, callback: (message: any) => void) {
          mockSubscribe(topic, callback);
          return { unsubscribe: vi.fn() };
        },
      };
    },
  };
});

vi.mock('sockjs-client', () => ({
  default: vi.fn(() => ({})),
}));

// Import hook AFTER mocking
import { useTicketUpdates, TicketConfirmationMessage } from './useTicketUpdates';

describe('useTicketUpdates', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should connect and subscribe to user-specific ticket queue', async () => {
    const onMessage = vi.fn();
    renderHook(() => useTicketUpdates(onMessage, true));

    expect(mockActivate).toHaveBeenCalled();

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalledWith(
        `/user/${mockUser.id}/queue/tickets`,
        expect.any(Function)
      );
    });
  });

  it('should not connect when disabled', () => {
    const onMessage = vi.fn();
    renderHook(() => useTicketUpdates(onMessage, false));

    expect(mockActivate).not.toHaveBeenCalled();
  });

  it('should call onMessage when TICKET_PURCHASED is received', async () => {
    const onMessage = vi.fn();
    renderHook(() => useTicketUpdates(onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    const messageCallback = mockSubscribe.mock.calls[0][1];
    const testMessage: TicketConfirmationMessage = {
      type: 'TICKET_PURCHASED',
      eventId: 1,
      ticketType: 'General',
      status: 'confirmed',
    };

    act(() => {
      messageCallback({ body: JSON.stringify(testMessage) });
    });

    expect(onMessage).toHaveBeenCalledWith(testMessage);
  });

  it('should call onMessage when TICKET_CANCELLED is received', async () => {
    const onMessage = vi.fn();
    renderHook(() => useTicketUpdates(onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    const messageCallback = mockSubscribe.mock.calls[0][1];
    const testMessage: TicketConfirmationMessage = {
      type: 'TICKET_CANCELLED',
      eventId: 1,
      ticketType: 'General',
      status: 'cancelled',
    };

    act(() => {
      messageCallback({ body: JSON.stringify(testMessage) });
    });

    expect(onMessage).toHaveBeenCalledWith(testMessage);
  });

  it('should call onMessage when TICKET_REFUNDED is received', async () => {
    const onMessage = vi.fn();
    renderHook(() => useTicketUpdates(onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    const messageCallback = mockSubscribe.mock.calls[0][1];
    const testMessage: TicketConfirmationMessage = {
      type: 'TICKET_REFUNDED',
      eventId: 1,
      ticketType: 'VIP',
      status: 'refunded',
    };

    act(() => {
      messageCallback({ body: JSON.stringify(testMessage) });
    });

    expect(onMessage).toHaveBeenCalledWith(testMessage);
  });

  it('should deactivate on unmount', async () => {
    const onMessage = vi.fn();
    const { unmount } = renderHook(() => useTicketUpdates(onMessage, true));

    await waitFor(() => {
      expect(mockActivate).toHaveBeenCalled();
    });

    unmount();

    expect(mockDeactivate).toHaveBeenCalled();
  });

  it('should handle malformed JSON gracefully', async () => {
    const onMessage = vi.fn();
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    renderHook(() => useTicketUpdates(onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    const messageCallback = mockSubscribe.mock.calls[0][1];

    act(() => {
      messageCallback({ body: 'invalid json' });
    });

    expect(onMessage).not.toHaveBeenCalled();
    expect(consoleSpy).toHaveBeenCalled();

    consoleSpy.mockRestore();
  });
});

// Import the mocked useAuth to modify it for the no-user tests
import { useAuth } from '../context/AuthContext';

describe('useTicketUpdates without user', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Override the mock to return no user
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });
  });

  it('should not connect when user is not authenticated', () => {
    const onMessage = vi.fn();
    renderHook(() => useTicketUpdates(onMessage, true));

    expect(mockActivate).not.toHaveBeenCalled();
  });
});
