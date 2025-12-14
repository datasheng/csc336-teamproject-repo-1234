import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface EventUpdateMessage {
  type: 'EVENT_CREATED' | 'EVENT_UPDATED' | 'CAPACITY_UPDATED';
  eventId?: number;
  campusId?: number;
  data?: any;
}

export const useEventUpdates = (topic: string, onMessage: (message: EventUpdateMessage) => void) => {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      debug: (str) => {
        console.log('[WebSocket]', str);
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
          onMessage(parsedMessage);
        } catch (error) {
          console.error('[WebSocket] Error parsing message:', error);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('[WebSocket] Broker reported error:', frame.headers['message']);
      console.error('[WebSocket] Additional details:', frame.body);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [topic, onMessage]);
};
