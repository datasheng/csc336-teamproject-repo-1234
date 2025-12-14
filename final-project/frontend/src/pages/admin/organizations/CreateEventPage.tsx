import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { organizationsApi, Organization } from '../../../api/organizations';
import { getAllCampuses, CampusDTO } from '../../../api/campuses';
import apiClient from '../../../api/events';

interface CostEntry {
  type: string;
  cost: number;
}

interface CreateEventRequest {
  organizerId: number;
  campusId: number;
  capacity: number;
  description: string;
  startTime: string;
  endTime: string;
  costs: CostEntry[];
}

export default function CreateEventPage() {
  const { id: organizationId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [organization, setOrganization] = useState<Organization | null>(null);
  const [campuses, setCampuses] = useState<CampusDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Form state
  const [description, setDescription] = useState('');
  const [campusId, setCampusId] = useState('');
  const [capacity, setCapacity] = useState('100');
  const [startDate, setStartDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endDate, setEndDate] = useState('');
  const [endTime, setEndTime] = useState('');
  const [costs, setCosts] = useState<CostEntry[]>([{ type: 'General', cost: 0 }]);

  useEffect(() => {
    fetchData();
  }, [organizationId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [org, campusData] = await Promise.all([
        organizationsApi.getOrganization(organizationId!),
        getAllCampuses()
      ]);
      setOrganization(org);
      setCampuses(campusData);
    } catch (err: any) {
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const addCostEntry = () => {
    setCosts([...costs, { type: '', cost: 0 }]);
  };

  const removeCostEntry = (index: number) => {
    if (costs.length > 1) {
      setCosts(costs.filter((_, i) => i !== index));
    }
  };

  const updateCostEntry = (index: number, field: 'type' | 'cost', value: string | number) => {
    const updated = [...costs];
    if (field === 'cost') {
      updated[index].cost = Number(value);
    } else {
      updated[index].type = value as string;
    }
    setCosts(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!description.trim()) {
      setError('Please enter an event description');
      return;
    }
    if (!campusId) {
      setError('Please select a campus');
      return;
    }
    if (!startDate || !startTime || !endDate || !endTime) {
      setError('Please enter start and end date/time');
      return;
    }

    // Validate costs
    for (const cost of costs) {
      if (!cost.type.trim()) {
        setError('Please enter a ticket type for all pricing entries');
        return;
      }
    }

    try {
      setSubmitting(true);
      setError(null);

      const request: CreateEventRequest = {
        organizerId: parseInt(organizationId!),
        campusId: parseInt(campusId),
        capacity: parseInt(capacity),
        description: description.trim(),
        startTime: `${startDate}T${startTime}:00`,
        endTime: `${endDate}T${endTime}:00`,
        costs: costs.filter(c => c.type.trim())
      };

      await apiClient.post('/events', request);
      navigate(`/admin/organizations/${organizationId}`);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to create event');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600"></div>
      </div>
    );
  }

  if (!organization) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          Organization not found
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-6">
      <button
        onClick={() => navigate(`/admin/organizations/${organizationId}`)}
        className="text-orange-600 hover:underline mb-4 flex items-center gap-1"
      >
        ← Back to {organization.name}
      </button>

      <div className="bg-white border border-stone-200 rounded-lg p-6">
        <h1 className="text-2xl font-bold text-stone-900 mb-2">Create New Event</h1>
        <p className="text-stone-600 mb-6">Hosting as <strong>{organization.name}</strong></p>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Event Description */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-stone-700 mb-1">
              Event Title/Description <span className="text-red-500">*</span>
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              placeholder="e.g., Spring Career Fair 2026 - Connect with top employers"
              required
            />
          </div>

          {/* Campus Selection */}
          <div>
            <label htmlFor="campusId" className="block text-sm font-medium text-stone-700 mb-1">
              Campus <span className="text-red-500">*</span>
            </label>
            <select
              id="campusId"
              value={campusId}
              onChange={(e) => setCampusId(e.target.value)}
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              required
            >
              <option value="">Select a campus</option>
              {campuses.map((campus) => (
                <option key={campus.id} value={campus.id}>
                  {campus.name}
                </option>
              ))}
            </select>
          </div>

          {/* Capacity */}
          <div>
            <label htmlFor="capacity" className="block text-sm font-medium text-stone-700 mb-1">
              Capacity <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              id="capacity"
              value={capacity}
              onChange={(e) => setCapacity(e.target.value)}
              min="1"
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              required
            />
          </div>

          {/* Date and Time */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="startDate" className="block text-sm font-medium text-stone-700 mb-1">
                Start Date <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                id="startDate"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                required
              />
            </div>
            <div>
              <label htmlFor="startTime" className="block text-sm font-medium text-stone-700 mb-1">
                Start Time <span className="text-red-500">*</span>
              </label>
              <input
                type="time"
                id="startTime"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="endDate" className="block text-sm font-medium text-stone-700 mb-1">
                End Date <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                id="endDate"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                required
              />
            </div>
            <div>
              <label htmlFor="endTime" className="block text-sm font-medium text-stone-700 mb-1">
                End Time <span className="text-red-500">*</span>
              </label>
              <input
                type="time"
                id="endTime"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                required
              />
            </div>
          </div>

          {/* Ticket Pricing */}
          <div>
            <label className="block text-sm font-medium text-stone-700 mb-2">
              Ticket Pricing
            </label>
            <div className="space-y-3">
              {costs.map((cost, index) => (
                <div key={index} className="flex gap-3 items-center">
                  <input
                    type="text"
                    value={cost.type}
                    onChange={(e) => updateCostEntry(index, 'type', e.target.value)}
                    placeholder="Ticket type (e.g., General, Student, VIP)"
                    className="flex-1 px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                  />
                  <div className="relative w-32">
                    <span className="absolute left-3 top-2 text-stone-500">$</span>
                    <input
                      type="number"
                      value={cost.cost}
                      onChange={(e) => updateCostEntry(index, 'cost', e.target.value)}
                      min="0"
                      step="0.01"
                      placeholder="0.00"
                      className="w-full pl-7 pr-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                  {costs.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeCostEntry(index)}
                      className="text-red-500 hover:text-red-700 p-2"
                    >
                      ✕
                    </button>
                  )}
                </div>
              ))}
            </div>
            <button
              type="button"
              onClick={addCostEntry}
              className="mt-3 text-orange-600 hover:text-orange-700 text-sm font-medium"
            >
              + Add another ticket type
            </button>
            <p className="text-xs text-stone-500 mt-1">
              Set price to $0 for free tickets
            </p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-3">
              <p className="text-red-800 text-sm">{error}</p>
            </div>
          )}

          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              disabled={submitting}
              className="flex-1 bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {submitting ? 'Creating Event...' : 'Create Event'}
            </button>
            <button
              type="button"
              onClick={() => navigate(`/admin/organizations/${organizationId}`)}
              className="px-6 py-3 border border-stone-300 rounded-lg font-medium text-stone-700 hover:bg-stone-50 transition-colors"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
