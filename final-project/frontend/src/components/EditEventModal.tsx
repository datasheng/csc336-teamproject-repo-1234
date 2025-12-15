import { useState, useEffect } from 'react';
import { EventDTO, UpdateEventRequest, updateEvent, ErrorResponse, getAllTags } from '../api/events';

interface EditEventModalProps {
  isOpen: boolean;
  onClose: (updated?: boolean) => void;
  event: EventDTO;
}

export const EditEventModal = ({ isOpen, onClose, event }: EditEventModalProps) => {
  const [formData, setFormData] = useState<UpdateEventRequest>({
    capacity: event.capacity,
    description: event.description,
    startTime: '',
    endTime: '',
    tags: event.tags || [],
  });
  const [availableTags, setAvailableTags] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && event) {
      // Format dates for datetime-local input
      const formatForInput = (dateString: string) => {
        const date = new Date(dateString);
        return date.toISOString().slice(0, 16);
      };

      setFormData({
        capacity: event.capacity,
        description: event.description.replace('[CANCELLED] ', ''),
        startTime: formatForInput(event.startTime),
        endTime: formatForInput(event.endTime),
        tags: event.tags || [],
      });
      setError(null);

      // Fetch available tags
      getAllTags().then(setAvailableTags).catch(console.error);
    }
  }, [isOpen, event]);

  const toggleTag = (tag: string) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags?.includes(tag)
        ? prev.tags.filter(t => t !== tag)
        : [...(prev.tags || []), tag]
    }));
  };

  const handleClose = () => {
    setError(null);
    onClose();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.capacity || formData.capacity < 1) {
      setError('Capacity must be at least 1');
      return;
    }

    if (!formData.description?.trim()) {
      setError('Description is required');
      return;
    }

    if (!formData.startTime || !formData.endTime) {
      setError('Start and end times are required');
      return;
    }

    const startDate = new Date(formData.startTime);
    const endDate = new Date(formData.endTime);

    if (endDate <= startDate) {
      setError('End time must be after start time');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      await updateEvent(event.id, {
        capacity: formData.capacity,
        description: formData.description.trim(),
        startTime: formData.startTime,
        endTime: formData.endTime,
        tags: formData.tags,
      });

      onClose(true);
    } catch (err) {
      const errorResponse = err as ErrorResponse;
      setError(errorResponse.message || 'Failed to update event');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="bg-orange-600 text-white p-4 rounded-t-lg">
          <h2 className="text-xl font-bold">Edit Event</h2>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-red-700">
              {error}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">
              Event Description
            </label>
            <input
              type="text"
              value={formData.description || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              className="w-full border border-stone-300 rounded-lg p-2 focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              placeholder="Event description"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">
              Capacity
            </label>
            <input
              type="number"
              min="1"
              value={formData.capacity || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, capacity: parseInt(e.target.value) || 0 }))}
              className="w-full border border-stone-300 rounded-lg p-2 focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              required
            />
            {event.ticketsSold > 0 && (
              <p className="text-sm text-stone-500 mt-1">
                Note: {event.ticketsSold} tickets already sold. Capacity cannot be less than tickets sold.
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">
              Start Time
            </label>
            <input
              type="datetime-local"
              value={formData.startTime || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, startTime: e.target.value }))}
              className="w-full border border-stone-300 rounded-lg p-2 focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">
              End Time
            </label>
            <input
              type="datetime-local"
              value={formData.endTime || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, endTime: e.target.value }))}
              className="w-full border border-stone-300 rounded-lg p-2 focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">
              Event Tags
            </label>
            <div className="flex flex-wrap gap-2 mt-2">
              {availableTags.map((tag) => (
                <button
                  key={tag}
                  type="button"
                  onClick={() => toggleTag(tag)}
                  className={`px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
                    formData.tags?.includes(tag)
                      ? 'bg-orange-500 text-white'
                      : 'bg-stone-100 text-stone-700 hover:bg-orange-100 hover:text-orange-700'
                  }`}
                >
                  {tag}
                </button>
              ))}
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={handleClose}
              className="flex-1 py-2 px-4 border border-stone-300 rounded-lg text-stone-700 hover:bg-stone-50 transition-colors"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-2 px-4 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
