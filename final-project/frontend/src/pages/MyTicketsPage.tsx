import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUserTickets, UserTicketDTO } from '../api/tickets';

export const MyTicketsPage = () => {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<UserTicketDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getUserTickets();

      const sortedTickets = data.sort((a, b) => {
        return new Date(a.startTime).getTime() - new Date(b.startTime).getTime();
      });

      setTickets(sortedTickets);
    } catch (err) {
      setError('Failed to load tickets. Please try again later.');
      console.error('Error fetching tickets:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const isUpcoming = (endTime: string) => {
    return new Date(endTime) > new Date();
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="text-xl text-stone-600">Loading tickets...</div>
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
    <div className="min-h-screen bg-stone-50 py-8">
      <div className="container mx-auto px-4 max-w-5xl">
        <h1 className="text-3xl font-bold text-stone-700 mb-8">My Tickets</h1>

        {tickets.length === 0 ? (
          <div className="bg-white border border-stone-200 rounded-lg shadow-sm p-12 text-center">
            <p className="text-xl text-stone-600 mb-2">No tickets yet</p>
            <p className="text-stone-500 mb-6">Start by browsing and purchasing tickets for upcoming events</p>
            <button
              onClick={() => navigate('/events')}
              className="bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors"
            >
              Browse Events
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {tickets.map((ticket, index) => (
              <div
                key={`${ticket.eventId}-${index}`}
                className={`bg-white border rounded-lg shadow-sm overflow-hidden transition-all hover:shadow-md cursor-pointer ${
                  isUpcoming(ticket.endTime) ? 'border-stone-200' : 'border-stone-200 opacity-60'
                }`}
                onClick={() => navigate(`/events/${ticket.eventId}`)}
              >
                <div className="p-6">
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex-1">
                      <h2 className="text-xl font-bold text-stone-700 mb-2">
                        {ticket.eventDescription}
                      </h2>
                      <p className="text-sm text-stone-500">
                        Organized by {ticket.organizerName}
                      </p>
                    </div>
                    {!isUpcoming(ticket.endTime) && (
                      <span className="bg-stone-100 text-stone-600 text-xs font-medium px-3 py-1 rounded-full">
                        Past Event
                      </span>
                    )}
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                    <div>
                      <div className="text-xs font-medium text-stone-500 uppercase tracking-wide mb-1">
                        Start Time
                      </div>
                      <div className="text-sm text-stone-700">
                        {formatDateTime(ticket.startTime)}
                      </div>
                    </div>
                    <div>
                      <div className="text-xs font-medium text-stone-500 uppercase tracking-wide mb-1">
                        End Time
                      </div>
                      <div className="text-sm text-stone-700">
                        {formatDateTime(ticket.endTime)}
                      </div>
                    </div>
                    <div>
                      <div className="text-xs font-medium text-stone-500 uppercase tracking-wide mb-1">
                        Ticket Type
                      </div>
                      <div className="text-sm text-stone-700 capitalize">
                        {ticket.type}
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-between items-center pt-4 border-t border-stone-100">
                    <div className="text-sm text-stone-600">
                      Amount Paid: <span className="font-semibold text-orange-600">
                        {ticket.cost === 0 ? 'Free' : `$${ticket.cost.toFixed(2)}`}
                      </span>
                    </div>
                    <div className="text-orange-600 text-sm font-medium hover:underline">
                      View Details →
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
