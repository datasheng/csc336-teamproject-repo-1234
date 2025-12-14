import { useState } from 'react';
import { EventFilters as EventFiltersType } from '../api/events';

interface EventFiltersProps {
  onFilterChange: (filters: EventFiltersType) => void;
}

export const EventFilters = ({ onFilterChange }: EventFiltersProps) => {
  const [campusId, setCampusId] = useState<string>('');
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');

  const handleApplyFilters = () => {
    const filters: EventFiltersType = {};

    if (campusId) {
      filters.campusId = parseInt(campusId);
    }
    if (startDate) {
      filters.startDate = startDate;
    }
    if (endDate) {
      filters.endDate = endDate;
    }

    onFilterChange(filters);
  };

  const handleClearFilters = () => {
    setCampusId('');
    setStartDate('');
    setEndDate('');
    onFilterChange({});
  };

  return (
    <div className="bg-white border border-stone-200 rounded-lg shadow-sm p-6">
      <h2 className="text-lg font-bold text-stone-700 mb-4">Filter Events</h2>

      <div className="space-y-4">
        <div>
          <label htmlFor="campusId" className="block text-sm font-medium text-stone-700 mb-1">
            Campus ID
          </label>
          <input
            type="number"
            id="campusId"
            value={campusId}
            onChange={(e) => setCampusId(e.target.value)}
            className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600 placeholder:text-stone-400"
            placeholder="Enter campus ID"
          />
        </div>

        <div>
          <label htmlFor="startDate" className="block text-sm font-medium text-stone-700 mb-1">
            Start Date
          </label>
          <input
            type="date"
            id="startDate"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
          />
        </div>

        <div>
          <label htmlFor="endDate" className="block text-sm font-medium text-stone-700 mb-1">
            End Date
          </label>
          <input
            type="date"
            id="endDate"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
          />
        </div>

        <div className="flex gap-2 pt-2">
          <button
            onClick={handleApplyFilters}
            className="flex-1 bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors"
          >
            Apply Filters
          </button>
          <button
            onClick={handleClearFilters}
            className="flex-1 bg-white text-stone-700 border border-stone-200 px-6 py-3 rounded-lg font-medium hover:bg-stone-50 transition-colors"
          >
            Clear
          </button>
        </div>
      </div>
    </div>
  );
};
