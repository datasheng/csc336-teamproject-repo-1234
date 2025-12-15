import { useState } from 'react';
import { deleteEvent, ErrorResponse } from '../api/events';

interface DeleteEventModalProps {
  isOpen: boolean;
  onClose: (deleted?: boolean) => void;
  eventId: number;
  eventDescription: string;
}

export const DeleteEventModal = ({ isOpen, onClose, eventId, eventDescription }: DeleteEventModalProps) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleClose = () => {
    setError(null);
    onClose();
  };

  const handleDelete = async () => {
    try {
      setLoading(true);
      setError(null);
      await deleteEvent(eventId);
      onClose(true);
    } catch (err) {
      const errorResponse = err as ErrorResponse;
      setError(errorResponse.message || 'Failed to delete event');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md">
        <div className="bg-red-600 text-white p-4 rounded-t-lg">
          <h2 className="text-xl font-bold">Delete Event</h2>
        </div>

        <div className="p-6">
          {error && (
            <div className="mb-4 bg-red-50 border border-red-200 rounded-lg p-3 text-red-700">
              {error}
            </div>
          )}

          <div className="mb-6">
            <div className="text-center mb-4">
              <div className="text-red-600 text-5xl mb-3">⚠️</div>
              <p className="text-stone-700 text-lg font-semibold mb-2">
                Are you sure you want to delete this event?
              </p>
              <p className="text-stone-600 bg-stone-100 rounded-lg p-3 break-words">
                "{eventDescription}"
              </p>
            </div>
            <p className="text-red-600 text-sm text-center">
              This action cannot be undone. All tickets for this event will also be deleted.
            </p>
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={handleClose}
              className="flex-1 py-2 px-4 border border-stone-300 rounded-lg text-stone-700 hover:bg-stone-50 transition-colors"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleDelete}
              disabled={loading}
              className="flex-1 py-2 px-4 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Deleting...' : 'Delete Event'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
