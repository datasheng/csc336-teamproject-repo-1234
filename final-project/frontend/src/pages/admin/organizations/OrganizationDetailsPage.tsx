import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { organizationsApi, Organization } from '../../../api/organizations';
import { getEvents, EventDTO } from '../../../api/events';
import { useEventUpdates, EventUpdateMessage } from '../../../hooks/useEventUpdates';

export default function OrganizationDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const [organization, setOrganization] = useState<Organization | null>(null);
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [eventsLoading, setEventsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [notification, setNotification] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetchOrganization();
      fetchEvents();
    }
  }, [id, location.key]); // Add location.key to refetch on navigation

  const showNotification = (message: string) => {
    setNotification(message);
    setTimeout(() => setNotification(null), 4000);
  };

  const handleEventUpdate = useCallback((message: EventUpdateMessage) => {
    switch (message.type) {
      case 'EVENT_CREATED':
        if (message.event) {
          const newEvent: EventDTO = {
            id: message.event.id,
            organizerId: message.event.organizerId,
            organizerName: message.event.organizerName,
            campusId: message.event.campusId,
            campusName: message.event.campusName,
            capacity: message.event.capacity,
            description: message.event.description,
            startTime: message.event.startTime,
            endTime: message.event.endTime,
            costs: [],
            ticketsSold: message.event.ticketsSold,
            availableCapacity: message.event.availableCapacity,
          };
          setEvents(prev => [newEvent, ...prev]);
          showNotification('New event created!');
        }
        break;

      case 'EVENT_UPDATED':
        if (message.event) {
          setEvents(prev => prev.map(e =>
            e.id === message.eventId
              ? { ...e, ...message.event, costs: e.costs }
              : e
          ));
          showNotification('Event updated');
        }
        break;

      case 'EVENT_DELETED':
        setEvents(prev => prev.filter(e => e.id !== message.eventId));
        showNotification('Event deleted');
        break;

      case 'EVENT_CANCELLED':
        setEvents(prev => prev.map(e =>
          e.id === message.eventId
            ? { ...e, description: `[CANCELLED] ${e.description}` }
            : e
        ));
        showNotification('Event cancelled');
        break;

      case 'CAPACITY_UPDATED':
        setEvents(prev => prev.map(e =>
          e.id === message.eventId
            ? {
                ...e,
                ticketsSold: message.ticketsSold ?? e.ticketsSold,
                availableCapacity: message.availableCapacity ?? e.availableCapacity
              }
            : e
        ));
        break;

      case 'ORGANIZATION_UPDATED':
        fetchOrganization();
        break;
    }
  }, []);

  // Subscribe to organization-specific topic
  useEventUpdates(
    id ? `/topic/organization/${id}` : '',
    handleEventUpdate,
    !!id
  );

  const fetchOrganization = async () => {
    try {
      setLoading(true);
      const data = await organizationsApi.getOrganization(id!);
      setOrganization(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load organization');
    } finally {
      setLoading(false);
    }
  };

  const fetchEvents = async () => {
    try {
      setEventsLoading(true);
      const data = await getEvents({ organizerId: parseInt(id!) });
      setEvents(data);
    } catch (err: any) {
      console.error('Failed to load events:', err);
    } finally {
      setEventsLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600"></div>
      </div>
    );
  }

  if (error || !organization) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error || 'Organization not found'}
        </div>
        <button
          onClick={() => navigate('/admin/organizations')}
          className="mt-4 text-orange-600 hover:underline"
        >
          ← Back to Organizations
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* Real-time notification toast */}
      {notification && (
        <div className="fixed top-4 right-4 z-50 bg-orange-600 text-white px-6 py-3 rounded-lg shadow-lg animate-pulse">
          {notification}
        </div>
      )}

      <button
        onClick={() => navigate('/admin/organizations')}
        className="text-orange-600 hover:underline mb-4 flex items-center gap-1"
      >
        ← Back to Organizations
      </button>

      <div className="bg-white border border-stone-200 rounded-lg p-6 mb-6">
        <h1 className="text-3xl font-bold text-stone-900 mb-3">{organization.name}</h1>
        <p className="text-stone-600 whitespace-pre-wrap">
          {organization.description || 'No description provided.'}
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <button
          onClick={() => navigate(`/admin/organizations/${id}/edit`)}
          className="bg-white border border-stone-200 rounded-lg p-4 hover:shadow-md hover:border-orange-300 transition-all text-left"
        >
          <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center mb-3">
            <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
            </svg>
          </div>
          <h3 className="font-semibold text-stone-900">Edit Organization</h3>
          <p className="text-sm text-stone-600">Update name and description</p>
        </button>

        <button
          onClick={() => navigate(`/admin/organizations/${id}/leaders`)}
          className="bg-white border border-stone-200 rounded-lg p-4 hover:shadow-md hover:border-orange-300 transition-all text-left"
        >
          <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center mb-3">
            <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
          <h3 className="font-semibold text-stone-900">Manage Leaders</h3>
          <p className="text-sm text-stone-600">Invite or remove leaders</p>
        </button>

        <button
          onClick={() => navigate(`/admin/organizations/${id}/create-event`)}
          className="bg-orange-600 text-white rounded-lg p-4 hover:bg-orange-700 transition-all text-left"
        >
          <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center mb-3">
            <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <h3 className="font-semibold">Create Event</h3>
          <p className="text-sm opacity-90">Host a new event</p>
        </button>
      </div>

      {/* Live Events Section */}
      <div className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-stone-900">
            Organization Events
            <span className="ml-2 text-sm font-normal text-green-600">● Live</span>
          </h2>
          <span className="text-sm text-stone-500">{events.length} event(s)</span>
        </div>

        {eventsLoading ? (
          <div className="bg-white border border-stone-200 rounded-lg p-8 text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-orange-600 mx-auto"></div>
            <p className="mt-2 text-stone-500">Loading events...</p>
          </div>
        ) : events.length === 0 ? (
          <div className="bg-white border border-stone-200 rounded-lg p-8 text-center">
            <div className="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <svg className="w-8 h-8 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p className="text-stone-600">No events yet.</p>
            <button
              onClick={() => navigate(`/admin/organizations/${id}/create-event`)}
              className="mt-4 text-orange-600 hover:underline"
            >
              Create your first event →
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {events.map(event => {
              const isCancelled = event.description.startsWith('[CANCELLED]');
              return (
                <div
                  key={event.id}
                  className={`bg-white border rounded-lg p-4 hover:shadow-md transition-all cursor-pointer ${
                    isCancelled ? 'border-red-200 opacity-75' : 'border-stone-200'
                  }`}
                  onClick={() => navigate(`/events/${event.id}`)}
                >
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-stone-900">
                          {event.description.replace('[CANCELLED] ', '')}
                        </h3>
                        {isCancelled && (
                          <span className="bg-red-100 text-red-700 text-xs px-2 py-0.5 rounded">
                            Cancelled
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-stone-500 mt-1">
                        {formatDate(event.startTime)} • {event.campusName}
                      </p>
                    </div>
                    <div className="text-right">
                      <div className="text-sm font-semibold text-stone-700">
                        {event.ticketsSold} / {event.capacity}
                      </div>
                      <div className="text-xs text-stone-500">tickets sold</div>
                      <div className="w-24 bg-stone-200 rounded-full h-2 mt-1">
                        <div
                          className={`h-2 rounded-full ${isCancelled ? 'bg-red-400' : 'bg-orange-500'}`}
                          style={{ width: `${Math.min(100, (event.ticketsSold / event.capacity) * 100)}%` }}
                        />
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}