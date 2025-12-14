import { useState, useEffect } from 'react';
import { getCurrentUser, updateCurrentUser, UpdateUserRequest } from '../api/users';
import { getAllCampuses, CampusDTO } from '../api/campuses';
import { useAuth } from '../context/AuthContext';

export const ProfilePage = () => {
  const { user: authUser, login } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    campusId: 0,
    email: '',
    campusName: '',
  });

  const [campuses, setCampuses] = useState<CampusDTO[]>([]);

  useEffect(() => {
    fetchUserAndCampuses();
  }, []);

  const fetchUserAndCampuses = async () => {
    try {
      setLoading(true);
      setError(null);

      const [userData, campusData] = await Promise.all([
        getCurrentUser(),
        getAllCampuses(),
      ]);

      setFormData({
        firstName: userData.firstName,
        lastName: userData.lastName,
        campusId: userData.campusId,
        email: userData.email,
        campusName: userData.campusName || '',
      });
      setCampuses(campusData);
    } catch (err) {
      setError('Failed to load profile. Please try again later.');
      console.error('Error fetching profile:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: string, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError(null);
      setSuccess(false);

      const updateData: UpdateUserRequest = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        campusId: formData.campusId,
      };

      const updatedUser = await updateCurrentUser(updateData);

      const selectedCampus = campuses.find(c => c.id === updatedUser.campusId);
      setFormData({
        ...formData,
        firstName: updatedUser.firstName,
        lastName: updatedUser.lastName,
        campusId: updatedUser.campusId,
        campusName: selectedCampus?.name || '',
      });

      if (authUser) {
        login(localStorage.getItem('token') || '', {
          ...authUser,
          firstName: updatedUser.firstName,
          lastName: updatedUser.lastName,
          campusId: updatedUser.campusId,
        });
      }

      setSuccess(true);
      setIsEditing(false);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError('Failed to update profile. Please try again.');
      console.error('Error updating profile:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    if (authUser) {
      setFormData({
        firstName: authUser.firstName,
        lastName: authUser.lastName,
        campusId: authUser.campusId,
        email: authUser.email,
        campusName: formData.campusName,
      });
    }
    setIsEditing(false);
    setError(null);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="text-xl text-stone-600">Loading profile...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-stone-50 py-8">
      <div className="container mx-auto px-4 max-w-2xl">
        <h1 className="text-3xl font-bold text-stone-700 mb-8">My Profile</h1>

        {success && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
            <p className="text-green-800">Profile updated successfully!</p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        <div className="bg-white border border-stone-200 rounded-lg shadow-sm p-8">
          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-stone-700 mb-2">
                Email
              </label>
              <input
                type="email"
                value={formData.email}
                disabled
                className="w-full px-4 py-2 border border-stone-200 rounded-md bg-stone-50 text-stone-500 cursor-not-allowed"
              />
              <p className="text-xs text-stone-500 mt-1">Email cannot be changed</p>
            </div>

            <div>
              <label htmlFor="firstName" className="block text-sm font-medium text-stone-700 mb-2">
                First Name
              </label>
              <input
                type="text"
                id="firstName"
                value={formData.firstName}
                onChange={(e) => handleInputChange('firstName', e.target.value)}
                disabled={!isEditing}
                className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600 disabled:bg-stone-50 disabled:text-stone-500"
              />
            </div>

            <div>
              <label htmlFor="lastName" className="block text-sm font-medium text-stone-700 mb-2">
                Last Name
              </label>
              <input
                type="text"
                id="lastName"
                value={formData.lastName}
                onChange={(e) => handleInputChange('lastName', e.target.value)}
                disabled={!isEditing}
                className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600 disabled:bg-stone-50 disabled:text-stone-500"
              />
            </div>

            <div>
              <label htmlFor="campus" className="block text-sm font-medium text-stone-700 mb-2">
                Campus
              </label>
              {isEditing ? (
                <select
                  id="campus"
                  value={formData.campusId}
                  onChange={(e) => handleInputChange('campusId', parseInt(e.target.value))}
                  className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                >
                  <option value="">Select a campus</option>
                  {campuses.map((campus) => (
                    <option key={campus.id} value={campus.id}>
                      {campus.name}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  type="text"
                  value={formData.campusName}
                  disabled
                  className="w-full px-4 py-2 border border-stone-200 rounded-md bg-stone-50 text-stone-500"
                />
              )}
            </div>
          </div>

          <div className="mt-8 flex gap-4">
            {!isEditing ? (
              <button
                onClick={() => setIsEditing(true)}
                className="flex-1 bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors"
              >
                Edit Profile
              </button>
            ) : (
              <>
                <button
                  onClick={handleCancel}
                  disabled={saving}
                  className="flex-1 bg-white text-stone-700 border border-stone-200 px-6 py-3 rounded-lg font-medium hover:bg-stone-50 transition-colors disabled:opacity-50"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSave}
                  disabled={saving || !formData.firstName || !formData.lastName || !formData.campusId}
                  className="flex-1 bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
