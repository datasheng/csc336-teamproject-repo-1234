import { useState, useEffect } from 'react';
import { getEvents, EventDTO, EventFilters as EventFiltersType } from '../api/events';
import { EventCard } from '../components/EventCard';
import { EventFilters } from '../components/EventFilters';
import { Pagination } from '../components/Pagination';

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
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl text-gray-600">Loading events...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl text-red-600">{error}</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Campus Events</h1>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          <div className="lg:col-span-1">
            <EventFilters onFilterChange={handleFilterChange} />
          </div>

          <div className="lg:col-span-3">
            {currentEvents.length === 0 ? (
              <div className="bg-white rounded-lg shadow-md p-8 text-center">
                <p className="text-gray-600">No events found matching your filters.</p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                  {currentEvents.map(event => (
                    <EventCard key={event.id} event={event} />
                  ))}
                </div>

                <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  onPageChange={setCurrentPage}
                />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
