import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export type EventMessageType = 
  | 'EVENT_CREATED' 
  | 'EVENT_UPDATED' 
  | 'EVENT_DELETED'
  | 'EVENT_CANCELLED'
  | 'CAPACITY_UPDATED'
  | 'ANALYTICS_UPDATED'
  | 'ORGANIZATION_UPDATED';

export interface EventData {
  id: number;
  organizerId: number;
  organizerName: string;
  campusId: number;
  campusName: string;
  capacity: number;
  description: string;
  startTime: string;
  endTime: string;
  ticketsSold: number;
  availableCapacity: number;
}

export interface EventUpdateMessage {
  type: EventMessageType;
  eventId?: number;
  campusId?: number;
  organizerId?: number;
  organizationId?: number;
  ticketsSold?: number;
  remainingCapacity?: number;
  availableCapacity?: number;
  event?: EventData;
}

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

export const useEventUpdates = (
  topic: string, 
  onMessage: (message: EventUpdateMessage) => void,
  enabled: boolean = true
) => {
  const clientRef = useRef<Client | null>(null);
  const onMessageRef = useRef(onMessage);

  // Keep callback ref updated without triggering reconnects
  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!enabled || !topic) {
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('[WebSocket]', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('[WebSocket] Connected to', topic);

      client.subscribe(topic, (message) => {
        try {
          const parsedMessage: EventUpdateMessage = JSON.parse(message.body);
          onMessageRef.current(parsedMessage);
        } catch (error) {
          console.error('[WebSocket] Error parsing message:', error);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('[WebSocket] Broker reported error:', frame.headers['message']);
      console.error('[WebSocket] Additional details:', frame.body);
    };

    client.onDisconnect = () => {
      console.log('[WebSocket] Disconnected from', topic);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, [topic, enabled]);

  return clientRef;
};

/**
 * Hook to subscribe to multiple topics at once.
 * Useful for pages that need updates from multiple sources.
 */
export const useMultiTopicUpdates = (
  topics: string[],
  onMessage: (message: EventUpdateMessage, topic: string) => void,
  enabled: boolean = true
) => {
  const clientRef = useRef<Client | null>(null);
  const onMessageRef = useRef(onMessage);

  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!enabled || topics.length === 0) {
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('[WebSocket]', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('[WebSocket] Connected to multiple topics:', topics);

      topics.forEach(topic => {
        client.subscribe(topic, (message) => {
          try {
            const parsedMessage: EventUpdateMessage = JSON.parse(message.body);
            onMessageRef.current(parsedMessage, topic);
          } catch (error) {
            console.error('[WebSocket] Error parsing message:', error);
          }
        });
      });
    };

    client.onStompError = (frame) => {
      console.error('[WebSocket] Broker reported error:', frame.headers['message']);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, [topics.join(','), enabled]);

  return clientRef;
};
