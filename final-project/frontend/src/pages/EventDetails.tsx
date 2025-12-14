import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getEventById, EventDTO } from '../api/events';

export const EventDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [event, setEvent] = useState<EventDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setError('Event ID is required');
      setLoading(false);
      return;
    }

    fetchEvent(parseInt(id));
  }, [id]);

  const fetchEvent = async (eventId: number) => {
    try {
      setLoading(true);
      setError(null);
      const data = await getEventById(eventId);
      setEvent(data);
    } catch (err) {
      setError('Failed to load event details. Please try again later.');
      console.error('Error fetching event:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="text-xl text-stone-600">Loading event details...</div>
      </div>
    );
  }

  if (error || !event) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-xl text-red-600 mb-4">{error || 'Event not found'}</div>
          <button
            onClick={() => navigate('/events')}
            className="text-orange-600 hover:underline"
          >
            Back to Events
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-stone-50 py-8">
      <div className="container mx-auto px-4 max-w-4xl">
        <button
          onClick={() => navigate('/events')}
          className="mb-6 text-orange-600 hover:underline flex items-center gap-2 font-medium"
        >
          ← Back to Events
        </button>

        <div className="bg-white border border-stone-200 rounded-lg shadow-sm overflow-hidden">
          <div className="bg-orange-600 text-white p-8">
            <h1 className="text-3xl font-bold mb-2">{event.description}</h1>
            <div className="flex items-center gap-4 text-orange-50">
              <span>{formatDate(event.startTime)}</span>
              <span>•</span>
              <span>{event.campusName}</span>
            </div>
          </div>

          <div className="p-8">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
              <div>
                <h2 className="text-lg font-bold text-stone-700 mb-4">Event Details</h2>
                <div className="space-y-3">
                  <div>
                    <div className="text-sm font-medium text-stone-500 uppercase tracking-wide">Start Time</div>
                    <div className="text-stone-700">{formatDateTime(event.startTime)}</div>
                  </div>
                  <div>
                    <div className="text-sm font-medium text-stone-500 uppercase tracking-wide">End Time</div>
                    <div className="text-stone-700">{formatDateTime(event.endTime)}</div>
                  </div>
                  <div>
                    <div className="text-sm font-medium text-stone-500 uppercase tracking-wide">Location</div>
                    <div className="text-stone-700">{event.campusName}</div>
                  </div>
                </div>
              </div>

              <div>
                <h2 className="text-lg font-bold text-stone-700 mb-4">Organization</h2>
                <div className="space-y-3">
                  <div>
                    <div className="text-sm font-medium text-stone-500 uppercase tracking-wide">Organized By</div>
                    <div className="text-stone-700">{event.organizerName}</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="mb-8">
              <h2 className="text-lg font-bold text-stone-700 mb-4">Capacity</h2>
              <div className="space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-stone-600">Total Capacity:</span>
                  <span className="font-semibold text-stone-700">{event.capacity}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-stone-600">Tickets Sold:</span>
                  <span className="font-semibold text-stone-700">{event.ticketsSold}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-stone-600">Available:</span>
                  <span className="font-semibold text-green-600">{event.availableCapacity}</span>
                </div>
                <div className="w-full bg-stone-200 rounded-full h-3 mt-2">
                  <div
                    className="bg-orange-600 h-3 rounded-full transition-all"
                    style={{ width: `${(event.ticketsSold / event.capacity) * 100}%` }}
                  />
                </div>
              </div>
            </div>

            <div className="mb-8">
              <h2 className="text-lg font-bold text-stone-700 mb-4">Ticket Types</h2>
              {event.costs.length === 0 ? (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <p className="text-green-800 font-semibold">Free Event</p>
                  <p className="text-green-600 text-sm">No tickets required</p>
                </div>
              ) : (
                <div className="grid gap-3">
                  {event.costs.map((cost, index) => (
                    <div
                      key={index}
                      className="border border-stone-200 rounded-lg p-4 flex justify-between items-center hover:border-orange-300 transition-colors"
                    >
                      <div>
                        <div className="font-semibold text-stone-700 capitalize">{cost.type}</div>
                        <div className="text-sm text-stone-500">Per ticket</div>
                      </div>
                      <div className="text-2xl font-bold text-orange-600">
                        {cost.cost === 0 ? 'Free' : `$${cost.cost.toFixed(2)}`}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="flex gap-4">
              <button
                disabled={event.availableCapacity === 0}
                className={`flex-1 py-3 px-6 rounded-lg font-medium transition-colors ${
                  event.availableCapacity === 0
                    ? 'bg-stone-100 text-stone-400 cursor-not-allowed opacity-50'
                    : 'bg-orange-600 text-white hover:bg-orange-700'
                }`}
              >
                {event.availableCapacity === 0 ? 'Sold Out' : 'Purchase Ticket'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
