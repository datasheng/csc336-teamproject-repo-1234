import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';

// Create a controllable mock for the Client class
const mockSubscribe = vi.fn();
const mockActivate = vi.fn();
const mockDeactivate = vi.fn();
let mockOnConnect: (() => void) | null = null;

vi.mock('@stomp/stompjs', () => {
  return {
    Client: function MockClient(_config: any) {
      return {
        onConnect: null,
        onStompError: null,
        onDisconnect: null,
        activate() {
          mockActivate();
          // Store the setter so we can trigger it
          const self = this;
          setTimeout(() => {
            if (self.onConnect) {
              mockOnConnect = self.onConnect;
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
import { useEventUpdates, useMultiTopicUpdates, EventUpdateMessage } from './useEventUpdates';

describe('useEventUpdates', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockOnConnect = null;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should not connect when topic is empty', () => {
    const onMessage = vi.fn();
    renderHook(() => useEventUpdates('', onMessage));

    expect(mockActivate).not.toHaveBeenCalled();
  });

  it('should not connect when disabled', () => {
    const onMessage = vi.fn();
    renderHook(() => useEventUpdates('/topic/events', onMessage, false));

    expect(mockActivate).not.toHaveBeenCalled();
  });

  it('should connect and subscribe when topic is provided', async () => {
    const onMessage = vi.fn();
    renderHook(() => useEventUpdates('/topic/events', onMessage, true));

    expect(mockActivate).toHaveBeenCalled();

    // Wait for onConnect to be called and subscription to happen
    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalledWith('/topic/events', expect.any(Function));
    });
  });

  it('should deactivate on unmount', async () => {
    const onMessage = vi.fn();
    const { unmount } = renderHook(() => useEventUpdates('/topic/events', onMessage, true));

    await waitFor(() => {
      expect(mockActivate).toHaveBeenCalled();
    });

    unmount();

    expect(mockDeactivate).toHaveBeenCalled();
  });

  it('should call onMessage callback when message is received', async () => {
    const onMessage = vi.fn();
    renderHook(() => useEventUpdates('/topic/events', onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    // Get the callback that was registered
    const subscribeCall = mockSubscribe.mock.calls[0];
    const messageCallback = subscribeCall[1];

    // Simulate receiving a message
    const testMessage: EventUpdateMessage = {
      type: 'EVENT_CREATED',
      eventId: 1,
      campusId: 2,
    };

    act(() => {
      messageCallback({ body: JSON.stringify(testMessage) });
    });

    expect(onMessage).toHaveBeenCalledWith(testMessage);
  });

  it('should handle malformed JSON gracefully', async () => {
    const onMessage = vi.fn();
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    renderHook(() => useEventUpdates('/topic/events', onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    const subscribeCall = mockSubscribe.mock.calls[0];
    const messageCallback = subscribeCall[1];

    // Simulate receiving malformed message
    act(() => {
      messageCallback({ body: 'not valid json' });
    });

    expect(onMessage).not.toHaveBeenCalled();
    expect(consoleSpy).toHaveBeenCalled();

    consoleSpy.mockRestore();
  });

  it('should handle all message types correctly', async () => {
    const onMessage = vi.fn();
    renderHook(() => useEventUpdates('/topic/events', onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    const messageCallback = mockSubscribe.mock.calls[0][1];

    const messageTypes: EventUpdateMessage['type'][] = [
      'EVENT_CREATED',
      'EVENT_UPDATED',
      'EVENT_DELETED',
      'EVENT_CANCELLED',
      'CAPACITY_UPDATED',
      'ANALYTICS_UPDATED',
      'ORGANIZATION_UPDATED',
    ];

    for (const type of messageTypes) {
      const message: EventUpdateMessage = { type, eventId: 1 };
      act(() => {
        messageCallback({ body: JSON.stringify(message) });
      });
    }

    expect(onMessage).toHaveBeenCalledTimes(messageTypes.length);
  });
});

describe('useMultiTopicUpdates', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockOnConnect = null;
  });

  it('should not connect when topics array is empty', () => {
    const onMessage = vi.fn();
    renderHook(() => useMultiTopicUpdates([], onMessage));

    expect(mockActivate).not.toHaveBeenCalled();
  });

  it('should subscribe to all topics', async () => {
    const onMessage = vi.fn();
    const topics = ['/topic/events', '/topic/event/1', '/topic/campus/2'];

    renderHook(() => useMultiTopicUpdates(topics, onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalledTimes(3);
    });

    topics.forEach(topic => {
      expect(mockSubscribe).toHaveBeenCalledWith(topic, expect.any(Function));
    });
  });

  it('should pass topic to callback', async () => {
    const onMessage = vi.fn();
    const topics = ['/topic/events'];

    renderHook(() => useMultiTopicUpdates(topics, onMessage, true));

    await waitFor(() => {
      expect(mockSubscribe).toHaveBeenCalled();
    });

    const messageCallback = mockSubscribe.mock.calls[0][1];
    const testMessage: EventUpdateMessage = { type: 'EVENT_CREATED', eventId: 1 };

    act(() => {
      messageCallback({ body: JSON.stringify(testMessage) });
    });

    expect(onMessage).toHaveBeenCalledWith(testMessage, '/topic/events');
  });
});
