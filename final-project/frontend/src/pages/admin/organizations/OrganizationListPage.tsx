import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { organizationsApi, Organization } from '../../../api/organizations';

export default function OrganizationListPage() {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrganizations();
  }, []);

  const fetchOrganizations = async () => {
    try {
      setLoading(true);
      const data = await organizationsApi.getMyOrganizations();
      setOrganizations(data || []);
    } catch (err: any) {
      setError(err.message || 'Failed to load organizations');
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

  if (error) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-stone-900">My Organizations</h1>
        <button
          onClick={() => navigate('/admin/organizations/create')}
          className="bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700 transition-colors"
        >
          + Create Organization
        </button>
      </div>

      {organizations.length === 0 ? (
        <div className="text-center py-12 bg-stone-50 rounded-lg">
          <p className="text-stone-600 mb-4">You don't lead any organizations yet.</p>
          <button
            onClick={() => navigate('/admin/organizations/create')}
            className="text-orange-600 hover:underline"
          >
            Create your first organization
          </button>
        </div>
      ) : (
        <div className="grid gap-4">
          {organizations.map((org) => (
            <div
              key={org.id}
              onClick={() => navigate(`/admin/organizations/${org.id}`)}
              className="bg-white border border-stone-200 rounded-lg p-5 cursor-pointer hover:shadow-md hover:border-orange-300 transition-all"
            >
              <h2 className="text-xl font-semibold text-stone-900 mb-2">{org.name}</h2>
              <p className="text-stone-600 line-clamp-2">{org.description}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}