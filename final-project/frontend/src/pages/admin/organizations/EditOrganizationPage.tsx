import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { organizationsApi, Organization } from '../../../api/organizations';

export default function EditOrganizationPage() {
  const { id } = useParams<{ id: string }>();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
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
      const org = await organizationsApi.getOrganization(id!);
      setName(org.name);
      setDescription(org.description || '');
    } catch (err: any) {
      setError(err.message || 'Failed to load organization');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!name.trim()) {
      setError('Organization name is required');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      await organizationsApi.updateOrganization(id!, {
        name: name.trim(),
        description: description.trim()
      });
      navigate(`/admin/organizations/${id}`);
    } catch (err: any) {
      setError(err.message || 'Failed to update organization');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-6">
      <button
        onClick={() => navigate(`/admin/organizations/${id}`)}
        className="text-orange-600 hover:underline mb-4 flex items-center gap-1"
      >
        ← Back to Organization
      </button>

      <h1 className="text-2xl font-bold text-stone-900 mb-6">Edit Organization</h1>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-stone-700 mb-1">
            Organization Name *
          </label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
            placeholder="Enter organization name"
            disabled={saving}
          />
        </div>

        <div>
          <label htmlFor="description" className="block text-sm font-medium text-stone-700 mb-1">
            Description
          </label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
            placeholder="Describe your organization"
            disabled={saving}
          />
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={saving}
            className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 disabled:bg-orange-400 transition-colors"
          >
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
          <button
            type="button"
            onClick={() => navigate(`/admin/organizations/${id}`)}
            className="px-6 py-2 border border-stone-300 rounded-lg hover:bg-stone-50 transition-colors"
            disabled={saving}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}