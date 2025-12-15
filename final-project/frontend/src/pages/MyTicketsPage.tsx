import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUserTickets, UserTicketDTO } from '../api/tickets';
import { useTicketUpdates, TicketConfirmationMessage } from '../hooks/useTicketUpdates';

export const MyTicketsPage = () => {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<UserTicketDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [notification, setNotification] = useState<string | null>(null);

  useEffect(() => {
    fetchTickets();
  }, []);

  const showNotification = (message: string) => {
    setNotification(message);
    setTimeout(() => setNotification(null), 4000);
  };

  const handleTicketUpdate = useCallback((message: TicketConfirmationMessage) => {
    switch (message.type) {
      case 'TICKET_PURCHASED':
        // Refetch to get the new ticket with full details
        fetchTickets();
        showNotification(`Ticket confirmed for event!`);
        break;
      case 'TICKET_CANCELLED':
      case 'TICKET_REFUNDED':
        // Remove the ticket from the list
        setTickets(prev => prev.filter(t => t.eventId !== message.eventId));
        showNotification(message.type === 'TICKET_CANCELLED' ? 'Ticket cancelled' : 'Ticket refunded');
        break;
    }
  }, []);

  useTicketUpdates(handleTicketUpdate);

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
    <div className="min-h-screen bg-gradient-to-br from-stone-50 via-orange-50/20 to-stone-50 py-12">
      {/* Real-time notification toast */}
      {notification && (
        <div className="fixed top-4 right-4 z-50 bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg animate-pulse">
          ✓ {notification}
        </div>
      )}

      <div className="container mx-auto px-4 max-w-6xl">
        <div className="mb-12">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-stone-800 to-stone-600 bg-clip-text text-transparent mb-3">
            My Tickets
          </h1>
          <p className="text-lg text-stone-600">
            Your upcoming and past event tickets
            <span className="ml-2 text-sm text-green-600">● Live updates</span>
          </p>
        </div>

        {tickets.length === 0 ? (
          <div className="bg-white border border-stone-200 rounded-3xl shadow-lg p-16 text-center">
            <div className="w-24 h-24 bg-gradient-to-br from-orange-500 to-orange-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-lg">
              <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
              </svg>
            </div>
            <p className="text-2xl font-bold text-stone-800 mb-3">No tickets yet</p>
            <p className="text-stone-500 mb-8 text-lg">Start by browsing and purchasing tickets for upcoming events</p>
            <button
              onClick={() => navigate('/events')}
              className="bg-gradient-to-r from-orange-600 to-orange-500 text-white px-10 py-4 rounded-xl font-bold hover:shadow-lg transition-all duration-300 hover:scale-105 inline-flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
              </svg>
              Browse Events
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {tickets.map((ticket, index) => (
              <div
                key={`${ticket.eventId}-${index}`}
                className={`group relative bg-white border-2 rounded-2xl shadow-md overflow-hidden transition-all duration-500 cursor-pointer hover:-translate-y-1 ${
                  isUpcoming(ticket.endTime)
                    ? 'border-stone-200 hover:border-orange-400 hover:shadow-xl'
                    : 'border-stone-200 opacity-70 hover:opacity-100'
                }`}
                onClick={() => navigate(`/events/${ticket.eventId}`)}
              >
                <div className="absolute top-0 left-0 w-2 h-full bg-gradient-to-b from-orange-500 via-orange-600 to-orange-700"></div>

                {!isUpcoming(ticket.endTime) && (
                  <div className="absolute top-6 right-6 bg-stone-100 text-stone-600 text-xs font-bold px-4 py-2 rounded-full border-2 border-stone-200">
                    Past Event
                  </div>
                )}

                <div className="p-8">
                  <div className="flex justify-between items-start mb-6">
                    <div className="flex-1 pr-8">
                      <h2 className="text-2xl font-bold text-stone-900 mb-3 group-hover:text-orange-600 transition-colors">
                        {ticket.eventDescription}
                      </h2>
                      <div className="flex items-center gap-2 text-stone-600">
                        <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                        </svg>
                        <span className="font-medium">Organized by {ticket.organizerName}</span>
                      </div>
                    </div>
                    <div className={`px-6 py-3 rounded-xl text-base font-bold shadow-md ${
                      ticket.cost === 0
                        ? 'bg-gradient-to-r from-green-500 to-green-600 text-white'
                        : 'bg-gradient-to-r from-orange-500 to-orange-600 text-white'
                    }`}>
                      {ticket.cost === 0 ? 'Free' : `$${ticket.cost.toFixed(2)}`}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                    <div className="flex items-start gap-3">
                      <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center flex-shrink-0">
                        <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                      <div className="flex-1">
                        <div className="text-xs font-bold text-stone-500 uppercase tracking-wider mb-1">
                          Start Time
                        </div>
                        <div className="text-base font-semibold text-stone-700">
                          {formatDateTime(ticket.startTime)}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center flex-shrink-0">
                        <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                      </div>
                      <div className="flex-1">
                        <div className="text-xs font-bold text-stone-500 uppercase tracking-wider mb-1">
                          End Time
                        </div>
                        <div className="text-base font-semibold text-stone-700">
                          {formatDateTime(ticket.endTime)}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center flex-shrink-0">
                        <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                        </svg>
                      </div>
                      <div className="flex-1">
                        <div className="text-xs font-bold text-stone-500 uppercase tracking-wider mb-1">
                          Ticket Type
                        </div>
                        <div className="text-base font-semibold text-stone-700 capitalize">
                          {ticket.type}
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-between items-center pt-6 border-t-2 border-stone-100">
                    <div className="text-base font-medium text-stone-600">
                      Amount Paid: <span className="font-bold text-stone-800 text-lg">
                        {ticket.cost === 0 ? 'Free' : `$${ticket.cost.toFixed(2)}`}
                      </span>
                    </div>
                    <div className="text-orange-600 font-bold text-base group-hover:text-orange-700 flex items-center gap-2">
                      View Event Details
                      <svg className="w-5 h-5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
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
