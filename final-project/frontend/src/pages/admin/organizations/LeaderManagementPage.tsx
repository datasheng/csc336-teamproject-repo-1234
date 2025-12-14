import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { organizationsApi, Organization, Leader } from '../../../api/organizations';

export default function LeaderManagementPage() {
  const { id } = useParams<{ id: string }>();
  const [organization, setOrganization] = useState<Organization | null>(null);
  const [leaders, setLeaders] = useState<Leader[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showInviteModal, setShowInviteModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetchData();
    }
  }, [id]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [org, leadersList] = await Promise.all([
        organizationsApi.getOrganization(id!),
        organizationsApi.getLeaders(id!)
      ]);
      setOrganization(org);
      setLeaders(leadersList);
    } catch (err: any) {
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveLeader = async (userId: number, leaderName: string) => {
    if (leaders.length <= 1) {
      setError("Can't remove the last leader");
      return;
    }

    if (!confirm(`Remove ${leaderName} as a leader?`)) {
      return;
    }

    try {
      await organizationsApi.removeLeader(id!, userId);
      setLeaders(leaders.filter(l => l.id !== userId));
    } catch (err: any) {
      setError(err.message || 'Failed to remove leader');
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
    <div className="max-w-4xl mx-auto p-6">
      <button
        onClick={() => navigate(`/admin/organizations/${id}`)}
        className="text-orange-600 hover:underline mb-4 flex items-center gap-1"
      >
        ← Back to Organization
      </button>

      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-stone-900">Manage Leaders</h1>
          <p className="text-stone-600">{organization?.name}</p>
        </div>
        <button
          onClick={() => setShowInviteModal(true)}
          className="bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700 transition-colors"
        >
          + Invite Leader
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
          <button onClick={() => setError(null)} className="ml-2 text-red-900 font-bold">×</button>
        </div>
      )}

      <div className="bg-white border border-stone-200 rounded-lg overflow-hidden">
        <div className="px-6 py-3 bg-stone-50 border-b border-stone-200">
          <h2 className="font-semibold text-stone-700">Current Leaders ({leaders.length})</h2>
        </div>
        
        {leaders.length === 0 ? (
          <div className="p-6 text-center text-stone-600">
            No leaders found
          </div>
        ) : (
          <ul className="divide-y divide-stone-200">
            {leaders.map((leader) => (
              <li key={leader.id} className="px-6 py-4 flex justify-between items-center hover:bg-stone-50">
                <div>
                  <p className="font-medium text-stone-900">
                    {leader.firstName} {leader.lastName}
                  </p>
                  <p className="text-sm text-stone-600">{leader.email}</p>
                </div>
                <button
                  onClick={() => handleRemoveLeader(leader.id, `${leader.firstName} ${leader.lastName}`)}
                  className="text-red-600 hover:text-red-800 text-sm px-3 py-1 rounded hover:bg-red-50 transition-colors"
                  disabled={leaders.length <= 1}
                  title={leaders.length <= 1 ? "Can't remove the last leader" : 'Remove leader'}
                >
                  {leaders.length <= 1 ? '(Only Leader)' : 'Remove'}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Invite Leader Modal */}
      {showInviteModal && (
        <InviteLeaderModal
          orgId={id!}
          onClose={() => setShowInviteModal(false)}
          onSuccess={(newLeader) => {
            setLeaders([...leaders, newLeader]);
            setShowInviteModal(false);
          }}
        />
      )}
    </div>
  );
}

// Invite Leader Modal Component
interface InviteLeaderModalProps {
  orgId: string;
  onClose: () => void;
  onSuccess: (leader: Leader) => void;
}

function InviteLeaderModal({ orgId, onClose, onSuccess }: InviteLeaderModalProps) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email.trim()) {
      setError('Email is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await organizationsApi.addLeader(orgId, email.trim());
      
      // Fetch updated leaders to get the new leader's info
      const leaders = await organizationsApi.getLeaders(orgId);
      const newLeader = leaders.find(l => l.email === email.trim());
      
      if (newLeader) {
        onSuccess(newLeader);
      } else {
        onClose();
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to invite leader');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-stone-900">Invite Leader</h2>
          <button
            onClick={onClose}
            className="text-stone-500 hover:text-stone-700 text-2xl"
          >
            ×
          </button>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="email" className="block text-sm font-medium text-stone-700 mb-1">
              Email Address *
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              placeholder="Enter user's email"
              disabled={loading}
              autoFocus
            />
            <p className="text-xs text-stone-500 mt-1">
              The user must already have an account on the platform.
            </p>
          </div>

          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border border-stone-300 rounded-lg hover:bg-stone-50 transition-colors"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="bg-orange-600 text-white px-4 py-2 rounded-lg hover:bg-orange-700 disabled:bg-orange-400 transition-colors"
            >
              {loading ? 'Inviting...' : 'Invite Leader'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}