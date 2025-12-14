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
    <div className="bg-white border border-stone-200 rounded-2xl shadow-md p-8 w-full">
      <div className="flex items-center gap-4 mb-8 pb-6 border-b-2 border-stone-100">
        <div className="w-14 h-14 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center shadow-lg">
          <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
          </svg>
        </div>
        <div>
          <h2 className="text-2xl font-bold text-stone-800">Filter Events</h2>
          <p className="text-sm text-stone-500 mt-1">Refine your search</p>
        </div>
      </div>

      <div className="space-y-6">
        <div className="bg-stone-50 rounded-xl p-5 border border-stone-200">
          <label htmlFor="campusId" className="flex items-center gap-2 text-xs font-bold text-stone-600 uppercase tracking-wider mb-3">
            <svg className="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
            </svg>
            Campus ID
          </label>
          <input
            type="number"
            id="campusId"
            value={campusId}
            onChange={(e) => setCampusId(e.target.value)}
            className="w-full px-5 py-3.5 border-2 border-stone-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 placeholder:text-stone-400 transition-all bg-white text-base font-medium"
            placeholder="Enter campus ID"
          />
        </div>

        <div className="bg-stone-50 rounded-xl p-5 border border-stone-200">
          <label htmlFor="startDate" className="flex items-center gap-2 text-xs font-bold text-stone-600 uppercase tracking-wider mb-3">
            <svg className="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            Start Date
          </label>
          <input
            type="date"
            id="startDate"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="w-full px-5 py-3.5 border-2 border-stone-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition-all bg-white text-base font-medium"
          />
        </div>

        <div className="bg-stone-50 rounded-xl p-5 border border-stone-200">
          <label htmlFor="endDate" className="flex items-center gap-2 text-xs font-bold text-stone-600 uppercase tracking-wider mb-3">
            <svg className="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            End Date
          </label>
          <input
            type="date"
            id="endDate"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="w-full px-5 py-3.5 border-2 border-stone-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition-all bg-white text-base font-medium"
          />
        </div>

        <div className="flex flex-col gap-3 pt-4">
          <button
            onClick={handleApplyFilters}
            className="w-full bg-gradient-to-r from-orange-600 to-orange-500 text-white px-8 py-4 rounded-xl font-bold hover:shadow-lg transition-all duration-300 hover:scale-[1.02] flex items-center justify-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            Apply Filters
          </button>
          <button
            onClick={handleClearFilters}
            className="w-full bg-white text-stone-700 border-2 border-stone-200 px-8 py-4 rounded-xl font-semibold hover:border-orange-500 hover:text-orange-600 transition-all duration-300 flex items-center justify-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
            Clear Filters
          </button>
        </div>
      </div>
    </div>
  );
};
