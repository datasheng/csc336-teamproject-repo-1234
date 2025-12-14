import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <nav className="bg-white border-b border-stone-200 shadow-sm">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center gap-8">
            <Link to="/" className="text-xl font-bold text-orange-600 hover:text-orange-700 transition-colors">
              Campus Events
            </Link>
            <div className="flex gap-6">
              <Link
                to="/events"
                className="text-stone-600 hover:text-orange-600 font-medium transition-colors"
              >
                Browse Events
              </Link>
              <Link
                to="/my-tickets"
                className="text-stone-600 hover:text-orange-600 font-medium transition-colors"
              >
                My Tickets
              </Link>
            </div>
          </div>

          <div className="flex items-center gap-6">
            {user && (
              <span className="text-sm text-stone-600">
                {user.firstName} {user.lastName}
              </span>
            )}
            <Link
              to="/profile"
              className="text-stone-600 hover:text-orange-600 font-medium transition-colors"
            >
              Profile
            </Link>
            <button
              onClick={handleLogout}
              className="bg-orange-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-orange-700 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};
