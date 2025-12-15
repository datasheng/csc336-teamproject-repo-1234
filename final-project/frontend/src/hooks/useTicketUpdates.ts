import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '../context/AuthContext';

export interface TicketConfirmationMessage {
  type: 'TICKET_PURCHASED' | 'TICKET_CANCELLED' | 'TICKET_REFUNDED';
  eventId: number;
  ticketType: string;
  status: 'confirmed' | 'cancelled' | 'refunded';
  message?: string;
}

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

/**
 * Hook for receiving user-specific ticket confirmations via WebSocket.
 * Subscribes to /user/{userId}/queue/tickets for real-time ticket status updates.
 */
export const useTicketUpdates = (
  onMessage: (message: TicketConfirmationMessage) => void,
  enabled: boolean = true
) => {
  const { user } = useAuth();
  const clientRef = useRef<Client | null>(null);
  const onMessageRef = useRef(onMessage);

  // Keep callback ref updated
  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!enabled || !user?.id) {
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('[WebSocket:Tickets]', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      const topic = `/user/${user.id}/queue/tickets`;
      console.log('[WebSocket:Tickets] Connected to', topic);

      client.subscribe(topic, (message) => {
        try {
          const parsedMessage: TicketConfirmationMessage = JSON.parse(message.body);
          console.log('[WebSocket:Tickets] Received:', parsedMessage);
          onMessageRef.current(parsedMessage);
        } catch (error) {
          console.error('[WebSocket:Tickets] Error parsing message:', error);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('[WebSocket:Tickets] Error:', frame.headers['message']);
    };

    client.onDisconnect = () => {
      console.log('[WebSocket:Tickets] Disconnected');
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, [user?.id, enabled]);

  return clientRef;
};
