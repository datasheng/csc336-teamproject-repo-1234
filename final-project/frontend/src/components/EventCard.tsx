import { useNavigate } from 'react-router-dom';
import { EventDTO } from '../api/events';

interface EventCardProps {
  event: EventDTO;
}

export const EventCard = ({ event }: EventCardProps) => {
  const navigate = useNavigate();

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
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

  const getLowestPrice = () => {
    if (event.costs.length === 0) return 'Free';
    const minCost = Math.min(...event.costs.map(c => c.cost));
    return minCost === 0 ? 'Free' : `$${minCost.toFixed(2)}`;
  };

  const price = getLowestPrice();
  const percentSold = (event.ticketsSold / event.capacity) * 100;

  return (
    <div
      onClick={() => navigate(`/events/${event.id}`)}
      className="group relative bg-white rounded-2xl shadow-md hover:shadow-2xl transition-all duration-500 cursor-pointer overflow-hidden border border-stone-200 hover:border-orange-400 hover:-translate-y-2"
    >
      <div className="absolute top-0 left-0 w-2 h-full bg-gradient-to-b from-orange-500 via-orange-600 to-orange-700"></div>

      <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-orange-500/5 to-transparent rounded-bl-full"></div>

      <div className="p-8">
        <div className="flex justify-between items-start gap-6 mb-6">
          <h3 className="text-2xl font-bold text-stone-900 line-clamp-2 flex-1 group-hover:text-orange-600 transition-colors leading-tight">
            {event.description}
          </h3>
          <div className={`px-5 py-2.5 rounded-xl text-base font-bold whitespace-nowrap shadow-sm ${
            price === 'Free'
              ? 'bg-gradient-to-r from-green-500 to-green-600 text-white'
              : 'bg-gradient-to-r from-orange-500 to-orange-600 text-white'
          }`}>
            {price}
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 mb-6">
          <div className="flex items-center gap-3">
            <div className="flex-shrink-0 w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-xs font-semibold text-stone-400 uppercase tracking-wider mb-0.5">Campus</div>
              <div className="text-base font-semibold text-stone-700 truncate">{event.campusName}</div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex-shrink-0 w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-xs font-semibold text-stone-400 uppercase tracking-wider mb-0.5">Host</div>
              <div className="text-base font-semibold text-stone-700 truncate">{event.organizerName}</div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex-shrink-0 w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-xs font-semibold text-stone-400 uppercase tracking-wider mb-0.5">Date</div>
              <div className="text-base font-semibold text-stone-700">{formatDate(event.startTime)}</div>
              <div className="text-xs text-stone-500 mt-0.5">{formatTime(event.startTime)} - {formatTime(event.endTime)}</div>
            </div>
          </div>
        </div>

        {/* Tags */}
        {event.tags && event.tags.length > 0 && (
          <div className="pt-4 border-t border-stone-100">
            <div className="flex flex-wrap gap-1.5">
              {event.tags.slice(0, 4).map((tag, index) => (
                <span
                  key={index}
                  className="px-2.5 py-1 bg-orange-50 text-orange-700 text-xs font-semibold rounded-full border border-orange-200"
                >
                  {tag}
                </span>
              ))}
              {event.tags.length > 4 && (
                <span className="px-2.5 py-1 bg-stone-100 text-stone-600 text-xs font-semibold rounded-full">
                  +{event.tags.length - 4} more
                </span>
              )}
            </div>
          </div>
        )}

        <div className="pt-5 border-t-2 border-stone-100">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold text-stone-500 uppercase tracking-widest">Availability</span>
            <span className="text-lg font-bold text-stone-800">
              {event.availableCapacity}<span className="text-stone-400 text-sm font-medium"> / {event.capacity}</span>
            </span>
          </div>
          <div className="relative w-full h-3 bg-stone-100 rounded-full overflow-hidden shadow-inner">
            <div
              className="absolute top-0 left-0 h-full bg-gradient-to-r from-orange-500 via-orange-600 to-orange-500 rounded-full transition-all duration-700 shadow-sm"
              style={{ width: `${percentSold}%` }}
            />
          </div>
          <div className="flex justify-between items-center mt-3">
            <p className="text-sm font-medium text-stone-600">
              {event.availableCapacity} spots remaining
            </p>
            {percentSold >= 80 && (
              <span className="bg-red-100 text-red-700 text-xs font-bold px-2.5 py-1 rounded-full">
                Filling Fast
              </span>
            )}
          </div>
        </div>

        <div className="mt-5 pt-5 border-t border-stone-100">
          <div className="text-orange-600 font-bold text-sm group-hover:text-orange-700 flex items-center gap-2">
            View Details
            <svg className="w-4 h-4 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
};
