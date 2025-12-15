import { useState, useEffect, useCallback } from 'react';
import { getEvents, EventDTO, EventFilters as EventFiltersType } from '../api/events';
import { EventCard } from '../components/EventCard';
import { EventFilters } from '../components/EventFilters';
import { Pagination } from '../components/Pagination';
import { useEventUpdates, EventUpdateMessage } from '../hooks/useEventUpdates';

const ITEMS_PER_PAGE = 9;

export const Events = () => {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [filters, setFilters] = useState<EventFiltersType>({});
  const [notification, setNotification] = useState<string | null>(null);

  useEffect(() => {
    fetchEvents();
  }, []);

  // Re-fetch events when filters change to use backend filtering
  useEffect(() => {
    fetchEvents();
  }, [filters]);

  const showNotification = (message: string) => {
    setNotification(message);
    setTimeout(() => setNotification(null), 4000);
  };

  const handleEventUpdate = useCallback((message: EventUpdateMessage) => {
    switch (message.type) {
      case 'EVENT_CREATED':
        // Check if event matches current filters before adding
        if (message.event) {
          const eventData = message.event;
          // Check campus filter
          if (filters.campusId && eventData.campusId !== filters.campusId) {
            return;
          }
          // Check organizer filter
          if (filters.organizerId && eventData.organizerId !== filters.organizerId) {
            return;
          }
          
          // Add new event to list with animation-friendly approach
          const newEvent: EventDTO = {
            id: eventData.id,
            organizerId: eventData.organizerId,
            organizerName: eventData.organizerName,
            campusId: eventData.campusId,
            campusName: eventData.campusName,
            capacity: eventData.capacity,
            description: eventData.description,
            startTime: eventData.startTime,
            endTime: eventData.endTime,
            costs: eventData.costs || [],
            ticketsSold: eventData.ticketsSold,
            availableCapacity: eventData.availableCapacity,
            tags: eventData.tags || [],
          };
          setEvents(prev => [newEvent, ...prev]);
          showNotification('New event added!');
        } else {
          // Fallback: refetch if no event data
          fetchEvents();
        }
        break;

      case 'EVENT_UPDATED':
        if (message.event) {
          setEvents(prev => prev.map(e => 
            e.id === message.eventId 
              ? { 
                  ...e, 
                  ...message.event, 
                  costs: message.event?.costs || e.costs,
                  tags: message.event?.tags || e.tags 
                }
              : e
          ));
        } else {
          // Fallback: refetch if no event data
          fetchEvents();
        }
        break;

      case 'EVENT_DELETED':
        setEvents(prev => prev.filter(e => e.id !== message.eventId));
        showNotification('An event has been removed');
        break;

      case 'EVENT_CANCELLED':
        // Mark as cancelled by updating description (or could add cancelled field)
        setEvents(prev => prev.map(e => 
          e.id === message.eventId
            ? { ...e, description: `[CANCELLED] ${e.description}` }
            : e
        ));
        showNotification('An event has been cancelled');
        break;

      case 'CAPACITY_UPDATED':
        // Update ticket counts for a specific event
        setEvents(prev => prev.map(e => 
          e.id === message.eventId
            ? { 
                ...e, 
                ticketsSold: message.ticketsSold ?? e.ticketsSold,
                availableCapacity: message.availableCapacity ?? message.remainingCapacity ?? e.availableCapacity
              }
            : e
        ));
        break;

      case 'ORGANIZATION_UPDATED':
        // Organization name/details changed - could refetch to get updated names
        // but only if we have events from this organization
        if (message.organizationId) {
          const hasEventsFromOrg = events.some(e => e.organizerId === message.organizationId);
          if (hasEventsFromOrg) {
            fetchEvents();
          }
        }
        break;

      case 'ANALYTICS_UPDATED':
        // Analytics updates are for org dashboards, not the events list
        // No action needed on the events page
        break;

      default:
        // Log unknown message types for debugging but don't refetch
        console.log('[Events] Received unhandled message type:', message.type);
    }
  }, [filters, events]);

  useEventUpdates('/topic/events', handleEventUpdate);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      // Pass filters to backend for server-side filtering
      const data = await getEvents(filters);
      setEvents(data);
    } catch (err) {
      setError('Failed to load events. Please try again later.');
      console.error('Error fetching events:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (newFilters: EventFiltersType) => {
    setFilters(newFilters);
    setCurrentPage(1);
  };

  const totalPages = Math.ceil(events.length / ITEMS_PER_PAGE);
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const endIndex = startIndex + ITEMS_PER_PAGE;
  const currentEvents = events.slice(startIndex, endIndex);

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="text-xl text-stone-600">Loading events...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="text-xl text-red-600">{error}</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-stone-50 via-orange-50/20 to-stone-50 py-12">
      {/* Real-time notification toast */}
      {notification && (
        <div className="fixed top-4 right-4 z-50 bg-orange-600 text-white px-6 py-3 rounded-lg shadow-lg animate-pulse">
          {notification}
        </div>
      )}
      
      <div className="container mx-auto px-4 max-w-7xl">
        <div className="mb-12">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-stone-800 to-stone-600 bg-clip-text text-transparent mb-3">
            CampusTix
          </h1>
          <p className="text-lg text-stone-600">
            Discover amazing events happening across campus
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-[400px_1fr] gap-8">
          <div className="lg:col-span-1">
            <div className="sticky top-8">
              <EventFilters onFilterChange={handleFilterChange} />
            </div>
          </div>

          <div>
            {currentEvents.length === 0 ? (
              <div className="bg-white border border-stone-200 rounded-2xl shadow-md p-12 text-center">
                <div className="text-stone-400 mb-4">
                  <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <p className="text-xl font-semibold text-stone-700 mb-2">No events found</p>
                <p className="text-stone-500">Try adjusting your filters to see more events</p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                  {currentEvents.map(event => (
                    <EventCard key={event.id} event={event} />
                  ))}
                </div>

                <div className="mt-12">
                  <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                  />
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
