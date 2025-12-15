import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getEventById, EventDTO } from '../api/events';
import { getUserTickets, UserTicketDTO } from '../api/tickets';
import { organizationsApi } from '../api/organizations';
import { TicketPurchaseModal } from '../components/TicketPurchaseModal';
import { EditEventModal } from '../components/EditEventModal';
import { DeleteEventModal } from '../components/DeleteEventModal';
import { useEventUpdates, EventUpdateMessage } from '../hooks/useEventUpdates';
import { useAuth } from '../context/AuthContext';

export const EventDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [event, setEvent] = useState<EventDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isPurchaseModalOpen, setIsPurchaseModalOpen] = useState(false);
  const [purchaseSuccess, setPurchaseSuccess] = useState(false);
  const [userTicket, setUserTicket] = useState<UserTicketDTO | null>(null);
  const [ticketsLoading, setTicketsLoading] = useState(true);
  const [eventCancelled, setEventCancelled] = useState(false);
  const [eventDeleted, setEventDeleted] = useState(false);
  const [isOrganizer, setIsOrganizer] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  useEffect(() => {
    if (!id) {
      setError('Event ID is required');
      setLoading(false);
      return;
    }

    fetchEvent(parseInt(id));
    fetchUserTicketForEvent(parseInt(id));
  }, [id]);

  // Check if the current user is an organizer/leader for this event
  useEffect(() => {
    const checkIfOrganizer = async () => {
      if (!isAuthenticated || !event) {
        setIsOrganizer(false);
        return;
      }

      try {
        const myOrgs = await organizationsApi.getMyOrganizations();
        const isLeader = myOrgs.some(org => org.id === event.organizerId);
        setIsOrganizer(isLeader);
      } catch (err) {
        console.error('Error checking organizer status:', err);
        setIsOrganizer(false);
      }
    };

    checkIfOrganizer();
  }, [isAuthenticated, event]);

  const handleEventUpdate = useCallback((message: EventUpdateMessage) => {
    switch (message.type) {
      case 'EVENT_UPDATED':
        // Refetch full event data for updated details
        if (id) {
          fetchEvent(parseInt(id));
        }
        break;
      
      case 'CAPACITY_UPDATED':
        // Update capacity in place without full refetch
        setEvent(prev => prev ? {
          ...prev,
          ticketsSold: message.ticketsSold ?? prev.ticketsSold,
          availableCapacity: message.availableCapacity ?? message.remainingCapacity ?? prev.availableCapacity
        } : null);
        break;

      case 'EVENT_DELETED':
        setEventDeleted(true);
        // Redirect after brief notification
        setTimeout(() => navigate('/events'), 3000);
        break;

      case 'EVENT_CANCELLED':
        setEventCancelled(true);
        // Update event description to show cancellation
        setEvent(prev => prev ? {
          ...prev,
          description: prev.description.startsWith('[CANCELLED]') 
            ? prev.description 
            : `[CANCELLED] ${prev.description}`
        } : null);
        break;

      default:
        // For other message types, refetch
        if (id) {
          fetchEvent(parseInt(id));
        }
    }
  }, [id, navigate]);

  useEventUpdates(id ? `/topic/event/${id}` : '', handleEventUpdate, !!id);

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

  const fetchUserTicketForEvent = async (eventId: number) => {
    try {
      setTicketsLoading(true);
      const tickets = await getUserTickets();
      const existingTicket = tickets.find(ticket => ticket.eventId === eventId);
      setUserTicket(existingTicket || null);
    } catch (err) {
      console.error('Error checking user tickets:', err);
      // Don't block the UI if we can't check tickets
      setUserTicket(null);
    } finally {
      setTicketsLoading(false);
    }
  };

  const handleModalClose = (purchased?: boolean) => {
    setIsPurchaseModalOpen(false);
    if (purchased) {
      setPurchaseSuccess(true);
      setTimeout(() => setPurchaseSuccess(false), 5000);
      // Refresh user's ticket status after purchase
      if (id) {
        fetchUserTicketForEvent(parseInt(id));
      }
    }
    if (id) {
      fetchEvent(parseInt(id));
    }
  };

  const handleEditModalClose = (updated?: boolean) => {
    setIsEditModalOpen(false);
    if (updated && id) {
      fetchEvent(parseInt(id));
    }
  };

  const handleDeleteModalClose = (deleted?: boolean) => {
    setIsDeleteModalOpen(false);
    if (deleted) {
      navigate('/events');
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

  // Show deleted notification and redirect
  if (eventDeleted) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="text-center bg-red-50 border border-red-200 rounded-lg p-8 max-w-md">
          <div className="text-red-600 text-5xl mb-4">🗑️</div>
          <div className="text-xl text-red-700 font-bold mb-2">Event Deleted</div>
          <p className="text-red-600 mb-4">This event has been removed by the organizer.</p>
          <p className="text-stone-500 text-sm">Redirecting to events page...</p>
        </div>
      </div>
    );
  }

  const isCancelled = eventCancelled || event.description.startsWith('[CANCELLED]');

  return (
    <div className="min-h-screen bg-stone-50 py-8">
      <div className="container mx-auto px-4 max-w-4xl">
        <button
          onClick={() => navigate('/events')}
          className="mb-6 text-orange-600 hover:underline flex items-center gap-2 font-medium"
        >
          ← Back to Events
        </button>

        {/* Cancelled event banner */}
        {isCancelled && (
          <div className="mb-6 bg-red-50 border-2 border-red-300 rounded-lg p-4 flex items-center gap-4">
            <div className="text-red-600 text-3xl">⚠️</div>
            <div>
              <p className="text-red-800 font-bold text-lg">Event Cancelled</p>
              <p className="text-red-600">This event has been cancelled by the organizer. Ticket purchases are disabled.</p>
            </div>
          </div>
        )}

        {/* Organizer controls */}
        {isOrganizer && (
          <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                <div>
                  <p className="text-blue-800 font-semibold">You're an organizer of this event</p>
                  <p className="text-blue-600 text-sm">You can edit or delete this event</p>
                </div>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => setIsEditModalOpen(true)}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                  Edit
                </button>
                <button
                  onClick={() => setIsDeleteModalOpen(true)}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors flex items-center gap-2"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  Delete
                </button>
              </div>
            </div>
          </div>
        )}

        <div className={`bg-white border rounded-lg shadow-sm overflow-hidden ${isCancelled ? 'border-red-200 opacity-75' : 'border-stone-200'}`}>
          <div className={`${isCancelled ? 'bg-red-600' : 'bg-orange-600'} text-white p-8`}>
            <h1 className="text-3xl font-bold mb-2">
              {event.description.replace('[CANCELLED] ', '')}
            </h1>
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

            {/* Tags Section */}
            {event.tags && event.tags.length > 0 && (
              <div className="mb-8">
                <h2 className="text-lg font-bold text-stone-700 mb-4">Tags</h2>
                <div className="flex flex-wrap gap-2">
                  {event.tags.map((tag, index) => (
                    <span
                      key={index}
                      className="px-3 py-1.5 bg-orange-50 text-orange-700 text-sm font-semibold rounded-full border border-orange-200"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            )}

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

            {/* Purchase success message */}
            {purchaseSuccess && (
              <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4">
                <p className="text-green-800 font-semibold">🎉 Ticket purchased successfully!</p>
                <p className="text-green-600 text-sm">Check your tickets in "My Tickets" page.</p>
              </div>
            )}

            {/* Already has ticket message */}
            {userTicket && !purchaseSuccess && (
              <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex items-center gap-3">
                  <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div>
                    <p className="text-blue-800 font-semibold">You already have a ticket for this event!</p>
                    <p className="text-blue-600 text-sm">
                      Ticket type: <span className="capitalize font-medium">{userTicket.type}</span>
                      {userTicket.cost > 0 && ` • Paid: $${userTicket.cost.toFixed(2)}`}
                    </p>
                  </div>
                </div>
              </div>
            )}

            <div className="flex gap-4">
              {ticketsLoading ? (
                <div className="flex-1 py-3 px-6 rounded-lg font-medium bg-stone-100 text-stone-400 text-center">
                  Checking ticket status...
                </div>
              ) : userTicket ? (
                <button
                  onClick={() => navigate('/my-tickets')}
                  className="flex-1 py-3 px-6 rounded-lg font-medium transition-colors bg-blue-600 text-white hover:bg-blue-700 flex items-center justify-center gap-2"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                  </svg>
                  View My Ticket
                </button>
              ) : (
                <button
                  onClick={() => setIsPurchaseModalOpen(true)}
                  disabled={event.availableCapacity === 0 || isCancelled}
                  className={`flex-1 py-3 px-6 rounded-lg font-medium transition-colors ${
                    event.availableCapacity === 0 || isCancelled
                      ? 'bg-stone-100 text-stone-400 cursor-not-allowed opacity-50'
                      : 'bg-orange-600 text-white hover:bg-orange-700'
                  }`}
                >
                  {isCancelled ? 'Event Cancelled' : event.availableCapacity === 0 ? 'Sold Out' : 'Purchase Ticket'}
                </button>
              )}
            </div>
          </div>
        </div>

        <TicketPurchaseModal
          isOpen={isPurchaseModalOpen}
          onClose={handleModalClose}
          eventId={event.id}
          eventDescription={event.description}
          costs={event.costs}
          availableCapacity={event.availableCapacity}
        />

        <EditEventModal
          isOpen={isEditModalOpen}
          onClose={handleEditModalClose}
          event={event}
        />

        <DeleteEventModal
          isOpen={isDeleteModalOpen}
          onClose={handleDeleteModalClose}
          eventId={event.id}
          eventDescription={event.description}
        />
      </div>
    </div>
  );
};
