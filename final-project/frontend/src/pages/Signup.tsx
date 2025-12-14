import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { signup as apiSignup } from '../api/auth';
import { getAllCampuses, CampusDTO } from '../api/campuses';
import { useAuth } from '../context/AuthContext';

export const Signup = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [campuses, setCampuses] = useState<CampusDTO[]>([]);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    campusId: '',
  });
  const [loading, setLoading] = useState(false);
  const [loadingCampuses, setLoadingCampuses] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCampuses();
  }, []);

  const fetchCampuses = async () => {
    try {
      setLoadingCampuses(true);
      const data = await getAllCampuses();
      setCampuses(data);
    } catch (err) {
      setError('Failed to load campuses');
    } finally {
      setLoadingCampuses(false);
    }
  };

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.firstName || !formData.lastName || !formData.email || !formData.password || !formData.campusId) {
      setError('Please fill in all fields');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await apiSignup({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
        campusId: parseInt(formData.campusId),
      });
      login(response.token, {
        id: response.userId,
        firstName: response.firstName,
        lastName: response.lastName,
        email: response.email,
        campusId: response.campusId,
      });
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Failed to sign up');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-stone-50 flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full">
        <div className="bg-white border border-stone-200 rounded-lg shadow-sm p-8">
          <h1 className="text-3xl font-bold text-stone-700 mb-2 text-center">Create Account</h1>
          <p className="text-stone-600 text-center mb-8">Join us to discover campus events</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="firstName" className="block text-sm font-medium text-stone-700 mb-1">
                  First Name
                </label>
                <input
                  type="text"
                  id="firstName"
                  value={formData.firstName}
                  onChange={(e) => handleChange('firstName', e.target.value)}
                  className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                  placeholder="John"
                  required
                />
              </div>

              <div>
                <label htmlFor="lastName" className="block text-sm font-medium text-stone-700 mb-1">
                  Last Name
                </label>
                <input
                  type="text"
                  id="lastName"
                  value={formData.lastName}
                  onChange={(e) => handleChange('lastName', e.target.value)}
                  className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                  placeholder="Doe"
                  required
                />
              </div>
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-stone-700 mb-1">
                Email Address
              </label>
              <input
                type="email"
                id="email"
                value={formData.email}
                onChange={(e) => handleChange('email', e.target.value)}
                className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                placeholder="you@example.com"
                required
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-stone-700 mb-1">
                Password
              </label>
              <input
                type="password"
                id="password"
                value={formData.password}
                onChange={(e) => handleChange('password', e.target.value)}
                className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                placeholder="••••••••"
                required
              />
            </div>

            <div>
              <label htmlFor="campusId" className="block text-sm font-medium text-stone-700 mb-1">
                Campus
              </label>
              <select
                id="campusId"
                value={formData.campusId}
                onChange={(e) => handleChange('campusId', e.target.value)}
                className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                required
                disabled={loadingCampuses}
              >
                <option value="">
                  {loadingCampuses ? 'Loading campuses...' : 'Select your campus'}
                </option>
                {campuses.map((campus) => (
                  <option key={campus.id} value={campus.id}>
                    {campus.name} - {campus.city}
                  </option>
                ))}
              </select>
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                <p className="text-red-800 text-sm">{error}</p>
              </div>
            )}

            <button
              type="submit"
              disabled={loading || loadingCampuses}
              className="w-full bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Creating account...' : 'Sign Up'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-stone-600 text-sm">
              Already have an account?{' '}
              <Link to="/login" className="text-orange-600 hover:underline font-medium">
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
