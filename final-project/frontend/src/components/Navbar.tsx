import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Navbar = () => {
  const { user, logout, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Don't show navbar while loading auth state
  if (isLoading) {
    return null;
  }

  // Show minimal navbar with login/signup for unauthenticated users
  if (!isAuthenticated) {
    return (
      <nav className="bg-white border-b-2 border-stone-100 shadow-md sticky top-0 z-50 backdrop-blur-sm bg-white/95">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-center h-20">
            <Link to="/" className="flex items-center gap-3 group">
              <div className="w-10 h-10 bg-gradient-to-br from-orange-600 to-orange-500 rounded-xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <span className="text-2xl font-bold bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
                CampusTix
              </span>
            </Link>

            <div className="flex items-center gap-4">
              <Link
                to="/login"
                className="px-5 py-2.5 text-stone-700 hover:text-orange-600 font-semibold transition-all duration-300 rounded-xl hover:bg-orange-50"
              >
                Sign In
              </Link>
              <Link
                to="/signup"
                className="bg-gradient-to-r from-orange-600 to-orange-500 text-white px-6 py-2.5 rounded-xl font-bold hover:shadow-lg transition-all duration-300 hover:scale-105"
              >
                Get Started
              </Link>
            </div>
          </div>
        </div>
      </nav>
    );
  }

  return (
    <nav className="bg-white border-b-2 border-stone-100 shadow-md sticky top-0 z-50 backdrop-blur-sm bg-white/95">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-20">
          <div className="flex items-center gap-10">
            <Link to="/" className="flex items-center gap-3 group">
              <div className="w-10 h-10 bg-gradient-to-br from-orange-600 to-orange-500 rounded-xl flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <span className="text-2xl font-bold bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
                CampusTix
              </span>
            </Link>
            <div className="flex gap-2">
              <Link
                to="/events"
                className="px-5 py-2.5 text-stone-700 hover:text-orange-600 font-semibold transition-all duration-300 rounded-xl hover:bg-orange-50"
              >
                Browse Events
              </Link>
              <Link
                to="/my-tickets"
                className="px-5 py-2.5 text-stone-700 hover:text-orange-600 font-semibold transition-all duration-300 rounded-xl hover:bg-orange-50"
              >
                My Tickets
              </Link>
              <Link
                to="/admin/organizations"
                className="px-5 py-2.5 text-stone-700 hover:text-orange-600 font-semibold transition-all duration-300 rounded-xl hover:bg-orange-50"
              >
                My Organizations
              </Link>
              {user?.isAdmin && (
                <Link
                  to="/admin/profit"
                  className="px-5 py-2.5 text-stone-700 hover:text-orange-600 font-semibold transition-all duration-300 rounded-xl hover:bg-orange-50"
                >
                  Profit Report
                </Link>
              )}
            </div>
          </div>

          <div className="flex items-center gap-4">
            {user && (
              <div className="flex items-center gap-3 px-4 py-2 bg-stone-50 rounded-xl border border-stone-200">
                <div className="w-8 h-8 bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg flex items-center justify-center shadow">
                  <span className="text-white font-bold text-sm">
                    {user.firstName[0]}{user.lastName[0]}
                  </span>
                </div>
                <span className="text-sm font-semibold text-stone-700">
                  {user.firstName} {user.lastName}
                </span>
              </div>
            )}
            <Link
              to="/profile"
              className="px-5 py-2.5 text-stone-700 hover:text-orange-600 font-semibold transition-all duration-300 rounded-xl hover:bg-orange-50"
            >
              Profile
            </Link>
            <button
              onClick={handleLogout}
              className="bg-gradient-to-r from-orange-600 to-orange-500 text-white px-6 py-2.5 rounded-xl font-bold hover:shadow-lg transition-all duration-300 hover:scale-105"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};
