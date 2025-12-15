import { useState, useEffect } from 'react';
import { EventFilters as EventFiltersType, getAllTags } from '../api/events';
import { getAllCampuses, CampusDTO } from '../api/campuses';

interface EventFiltersProps {
  onFilterChange: (filters: EventFiltersType) => void;
}

export const EventFilters = ({ onFilterChange }: EventFiltersProps) => {
  const [campusId, setCampusId] = useState<string>('');
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [freeOnly, setFreeOnly] = useState<boolean>(false);
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');
  const [campuses, setCampuses] = useState<CampusDTO[]>([]);
  const [loadingCampuses, setLoadingCampuses] = useState(true);
  const [availableTags, setAvailableTags] = useState<string[]>([]);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [loadingTags, setLoadingTags] = useState(true);
  const [tagsOpen, setTagsOpen] = useState(false);

  useEffect(() => {
    fetchCampuses();
    fetchTags();
  }, []);

  const fetchCampuses = async () => {
    try {
      setLoadingCampuses(true);
      const data = await getAllCampuses();
      setCampuses(data);
    } catch (error) {
      console.error('Error fetching campuses:', error);
    } finally {
      setLoadingCampuses(false);
    }
  };

  const fetchTags = async () => {
    try {
      setLoadingTags(true);
      const data = await getAllTags();
      setAvailableTags(data);
    } catch (error) {
      console.error('Error fetching tags:', error);
    } finally {
      setLoadingTags(false);
    }
  };

  const toggleTag = (tag: string) => {
    setSelectedTags(prev => 
      prev.includes(tag) 
        ? prev.filter(t => t !== tag)
        : [...prev, tag]
    );
  };

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
    if (freeOnly) {
      filters.freeOnly = true;
    }
    if (minPrice && !freeOnly) {
      filters.minPrice = parseFloat(minPrice);
    }
    if (maxPrice && !freeOnly) {
      filters.maxPrice = parseFloat(maxPrice);
    }
    if (selectedTags.length > 0) {
      filters.tags = selectedTags;
    }

    onFilterChange(filters);
  };

  const handleClearFilters = () => {
    setCampusId('');
    setStartDate('');
    setEndDate('');
    setFreeOnly(false);
    setMinPrice('');
    setMaxPrice('');
    setSelectedTags([]);
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
            Campus
          </label>
          <select
            id="campusId"
            value={campusId}
            onChange={(e) => setCampusId(e.target.value)}
            disabled={loadingCampuses}
            className="w-full px-5 py-3.5 border-2 border-stone-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition-all bg-white text-base font-medium appearance-none cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            style={{ backgroundImage: `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e")`, backgroundPosition: 'right 0.75rem center', backgroundRepeat: 'no-repeat', backgroundSize: '1.5em 1.5em', paddingRight: '2.5rem' }}
          >
            <option value="">
              {loadingCampuses ? 'Loading campuses...' : 'All Campuses'}
            </option>
            {campuses.map((campus) => (
              <option key={campus.id} value={campus.id}>
                {campus.name}
              </option>
            ))}
          </select>
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

        <div className="bg-stone-50 rounded-xl p-5 border border-stone-200">
          <label className="flex items-center gap-2 text-xs font-bold text-stone-600 uppercase tracking-wider mb-3">
            <svg className="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Price
          </label>
          
          <div className="flex items-center gap-3 mb-4">
            <input
              type="checkbox"
              id="freeOnly"
              checked={freeOnly}
              onChange={(e) => setFreeOnly(e.target.checked)}
              className="w-5 h-5 text-orange-600 border-2 border-stone-300 rounded focus:ring-orange-500 cursor-pointer"
            />
            <label htmlFor="freeOnly" className="text-base font-medium text-stone-700 cursor-pointer">
              Free events only
            </label>
          </div>

          {!freeOnly && (
            <div className="flex gap-3">
              <div className="flex-1">
                <label htmlFor="minPrice" className="block text-xs text-stone-500 mb-1">Min Price</label>
                <input
                  type="number"
                  id="minPrice"
                  value={minPrice}
                  onChange={(e) => setMinPrice(e.target.value)}
                  placeholder="$0"
                  min="0"
                  step="0.01"
                  className="w-full px-4 py-2.5 border-2 border-stone-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition-all bg-white text-sm font-medium"
                />
              </div>
              <div className="flex-1">
                <label htmlFor="maxPrice" className="block text-xs text-stone-500 mb-1">Max Price</label>
                <input
                  type="number"
                  id="maxPrice"
                  value={maxPrice}
                  onChange={(e) => setMaxPrice(e.target.value)}
                  placeholder="Any"
                  min="0"
                  step="0.01"
                  className="w-full px-4 py-2.5 border-2 border-stone-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition-all bg-white text-sm font-medium"
                />
              </div>
            </div>
          )}
        </div>

        {/* Tags Filter */}
        <div className="border-b border-stone-200 pb-4">
          <button
            onClick={() => setTagsOpen(!tagsOpen)}
            className="w-full flex items-center justify-between text-left group"
          >
            <div className="flex items-center gap-3">
              <div className="p-2 bg-gradient-to-br from-orange-100 to-orange-50 rounded-lg group-hover:from-orange-200 group-hover:to-orange-100 transition-all">
                <svg className="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
              </div>
              <div>
                <span className="font-semibold text-stone-800">Tags</span>
                {selectedTags.length > 0 && (
                  <span className="ml-2 text-xs bg-orange-100 text-orange-700 px-2 py-0.5 rounded-full">{selectedTags.length} selected</span>
                )}
              </div>
            </div>
            <svg
              className={`w-5 h-5 text-stone-400 transition-transform duration-300 ${tagsOpen ? 'rotate-180' : ''}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          {tagsOpen && (
            <div className="mt-4 flex flex-wrap gap-2">
              {loadingTags ? (
                <p className="text-sm text-stone-500">Loading tags...</p>
              ) : availableTags.length === 0 ? (
                <p className="text-sm text-stone-500">No tags available</p>
              ) : (
                availableTags.map((tag) => (
                  <button
                    key={tag}
                    onClick={() => toggleTag(tag)}
                    className={`px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
                      selectedTags.includes(tag)
                        ? 'bg-orange-500 text-white'
                        : 'bg-stone-100 text-stone-700 hover:bg-orange-100 hover:text-orange-700'
                    }`}
                  >
                    {tag}
                  </button>
                ))
              )}
            </div>
          )}
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
