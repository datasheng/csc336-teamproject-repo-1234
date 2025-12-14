import { Routes, Route, Link } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { PrivateRoute } from './components/PrivateRoute'
import { Navbar } from './components/Navbar'
import { Events } from './pages/Events'
import { EventDetails } from './pages/EventDetails'
import { Login } from './pages/Login'
import { Signup } from './pages/Signup'
import { ProfilePage } from './pages/ProfilePage'
import { MyTicketsPage } from './pages/MyTicketsPage'

function HomePage() {
  return (
    <div className="min-h-screen bg-stone-50 flex items-center justify-center">
      <div className="text-center px-4">
        <h1 className="text-5xl font-bold mb-4 text-stone-700">Campus Events Platform</h1>
        <p className="text-xl text-stone-600 mb-8">Discover and attend campus events</p>
        <div className="bg-white border border-stone-200 rounded-lg shadow-sm p-6 max-w-md mx-auto mb-8">
          <p className="text-lg text-stone-700">
            Your one-stop destination for discovering, organizing, and attending
            campus events.
          </p>
        </div>

        <div className="flex justify-center gap-4 mb-8">
          <Link
            to="/signup"
            className="bg-orange-600 text-white px-8 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors"
          >
            Sign Up
          </Link>
          <Link
            to="/login"
            className="bg-white text-stone-700 border border-stone-200 px-8 py-3 rounded-lg font-medium hover:bg-stone-50 transition-colors"
          >
            Login
          </Link>
        </div>

        <div className="mt-8 flex justify-center gap-4">
          <div className="bg-orange-100 border border-orange-200 rounded-full px-6 py-2">
            <span className="text-sm text-stone-700">🎓 Universities</span>
          </div>
          <div className="bg-orange-100 border border-orange-200 rounded-full px-6 py-2">
            <span className="text-sm text-stone-700">🎉 Events</span>
          </div>
          <div className="bg-orange-100 border border-orange-200 rounded-full px-6 py-2">
            <span className="text-sm text-stone-700">🎫 Tickets</span>
          </div>
        </div>
      </div>
    </div>
  )
}

function App() {
  return (
    <AuthProvider>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/events" element={<PrivateRoute><Events /></PrivateRoute>} />
        <Route path="/events/:id" element={<PrivateRoute><EventDetails /></PrivateRoute>} />
        <Route path="/profile" element={<PrivateRoute><ProfilePage /></PrivateRoute>} />
        <Route path="/my-tickets" element={<PrivateRoute><MyTicketsPage /></PrivateRoute>} />
      </Routes>
    </AuthProvider>
  )
}

export default App
