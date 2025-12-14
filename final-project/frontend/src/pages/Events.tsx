import { useState, useEffect, useCallback } from 'react';
import { getEvents, EventDTO, EventFilters as EventFiltersType } from '../api/events';
import { EventCard } from '../components/EventCard';
import { EventFilters } from '../components/EventFilters';
import { Pagination } from '../components/Pagination';
import { useEventUpdates } from '../hooks/useEventUpdates';

const ITEMS_PER_PAGE = 9;

export const Events = () => {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [filteredEvents, setFilteredEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [filters, setFilters] = useState<EventFiltersType>({});

  useEffect(() => {
    fetchEvents();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [events, filters]);

  const handleEventUpdate = useCallback(() => {
    fetchEvents();
  }, []);

  useEventUpdates('/topic/events', handleEventUpdate);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getEvents();
      setEvents(data);
      setFilteredEvents(data);
    } catch (err) {
      setError('Failed to load events. Please try again later.');
      console.error('Error fetching events:', err);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...events];

    if (filters.campusId) {
      filtered = filtered.filter(event => event.campusId === filters.campusId);
    }

    if (filters.startDate) {
      filtered = filtered.filter(
        event => new Date(event.startTime) >= new Date(filters.startDate!)
      );
    }

    if (filters.endDate) {
      filtered = filtered.filter(
        event => new Date(event.endTime) <= new Date(filters.endDate!)
      );
    }

    setFilteredEvents(filtered);
    setCurrentPage(1);
  };

  const handleFilterChange = (newFilters: EventFiltersType) => {
    setFilters(newFilters);
  };

  const totalPages = Math.ceil(filteredEvents.length / ITEMS_PER_PAGE);
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const endIndex = startIndex + ITEMS_PER_PAGE;
  const currentEvents = filteredEvents.slice(startIndex, endIndex);

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
      <div className="container mx-auto px-4 max-w-7xl">
        <div className="mb-12">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-stone-800 to-stone-600 bg-clip-text text-transparent mb-3">
            Campus Events
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
