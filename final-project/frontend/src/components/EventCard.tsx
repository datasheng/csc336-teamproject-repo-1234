import { EventDTO } from '../api/events';

interface EventCardProps {
  event: EventDTO;
}

export const EventCard = ({ event }: EventCardProps) => {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getLowestPrice = () => {
    if (event.costs.length === 0) return 'Free';
    const minCost = Math.min(...event.costs.map(c => c.cost));
    return minCost === 0 ? 'Free' : `$${minCost.toFixed(2)}`;
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
      <div className="flex justify-between items-start mb-4">
        <h3 className="text-xl font-bold text-gray-900">{event.description}</h3>
        <span className="text-lg font-semibold text-blue-600">{getLowestPrice()}</span>
      </div>

      <div className="space-y-2 text-sm text-gray-600">
        <div className="flex items-center">
          <span className="font-medium">Organizer:</span>
          <span className="ml-2">{event.organizerName}</span>
        </div>

        <div className="flex items-center">
          <span className="font-medium">Campus:</span>
          <span className="ml-2">{event.campusName}</span>
        </div>

        <div className="flex items-center">
          <span className="font-medium">Start:</span>
          <span className="ml-2">{formatDate(event.startTime)}</span>
        </div>

        <div className="flex items-center">
          <span className="font-medium">End:</span>
          <span className="ml-2">{formatDate(event.endTime)}</span>
        </div>

        <div className="flex items-center">
          <span className="font-medium">Capacity:</span>
          <span className="ml-2">
            {event.ticketsSold} / {event.capacity} sold
          </span>
        </div>

        <div className="mt-4">
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className="bg-blue-600 h-2 rounded-full"
              style={{ width: `${(event.ticketsSold / event.capacity) * 100}%` }}
            />
          </div>
          <p className="text-xs text-gray-500 mt-1">
            {event.availableCapacity} spots remaining
          </p>
        </div>
      </div>
    </div>
  );
};
