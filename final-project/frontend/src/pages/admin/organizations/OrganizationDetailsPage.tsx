import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { organizationsApi, Organization } from '../../../api/organizations';

export default function OrganizationDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const [organization, setOrganization] = useState<Organization | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetchOrganization();
    }
  }, [id]);

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
          <div className="text-2xl mb-2">✏️</div>
          <h3 className="font-semibold text-stone-900">Edit Organization</h3>
          <p className="text-sm text-stone-600">Update name and description</p>
        </button>

        <button
          onClick={() => navigate(`/admin/organizations/${id}/leaders`)}
          className="bg-white border border-stone-200 rounded-lg p-4 hover:shadow-md hover:border-orange-300 transition-all text-left"
        >
          <div className="text-2xl mb-2">👥</div>
          <h3 className="font-semibold text-stone-900">Manage Leaders</h3>
          <p className="text-sm text-stone-600">Invite or remove leaders</p>
        </button>

        <button
          onClick={() => navigate(`/admin/organizations/${id}/create-event`)}
          className="bg-orange-600 text-white rounded-lg p-4 hover:bg-orange-700 transition-all text-left"
        >
          <div className="text-2xl mb-2">📅</div>
          <h3 className="font-semibold">Create Event</h3>
          <p className="text-sm opacity-90">Host a new event</p>
        </button>
      </div>

      <div className="mt-6">
        <button
          onClick={() => navigate(`/admin/organizations/${id}/events`)}
          className="text-orange-600 hover:underline"
        >
          View all events by this organization →
        </button>
      </div>
    </div>
  );
}