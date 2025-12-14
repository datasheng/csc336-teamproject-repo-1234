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
    <div className="min-h-screen bg-gradient-to-br from-stone-50 via-orange-50/30 to-stone-50">
      <div className="container mx-auto px-4 py-16 max-w-6xl">
        <div className="text-center mb-16 animate-fade-in">
          <div className="inline-block mb-6">
            <span className="bg-gradient-to-r from-orange-600 to-orange-500 text-white px-4 py-2 rounded-full text-sm font-semibold shadow-md">
              Welcome to Campus Events
            </span>
          </div>
          <h1 className="text-6xl md:text-7xl font-bold mb-6 bg-gradient-to-r from-stone-800 to-stone-600 bg-clip-text text-transparent">
            Campus Events Platform
          </h1>
          <p className="text-2xl text-stone-600 mb-8 max-w-2xl mx-auto">
            Discover, organize, and attend amazing campus events
          </p>

          <div className="flex flex-col sm:flex-row justify-center gap-4 mb-12">
            <Link
              to="/signup"
              className="group relative bg-gradient-to-r from-orange-600 to-orange-500 text-white px-10 py-4 rounded-xl font-bold text-lg shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105 overflow-hidden"
            >
              <span className="relative z-10">Get Started Free</span>
              <div className="absolute inset-0 bg-gradient-to-r from-orange-500 to-orange-600 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
            </Link>
            <Link
              to="/login"
              className="group bg-white text-stone-700 border-2 border-stone-200 px-10 py-4 rounded-xl font-bold text-lg hover:border-orange-500 hover:text-orange-600 transition-all duration-300 hover:scale-105 shadow-md hover:shadow-lg"
            >
              Sign In
            </Link>
          </div>
        </div>

        <div className="grid md:grid-cols-3 gap-8 mb-16">
          <div className="group bg-white border border-stone-200 rounded-2xl p-8 shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-2">
            <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl mb-6 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-stone-800 mb-3">Browse Events</h3>
            <p className="text-stone-600 leading-relaxed">
              Explore hundreds of events across multiple campuses. Find workshops, concerts, sports, and more.
            </p>
          </div>

          <div className="group bg-white border border-stone-200 rounded-2xl p-8 shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-2">
            <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl mb-6 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-stone-800 mb-3">Easy Ticketing</h3>
            <p className="text-stone-600 leading-relaxed">
              Purchase tickets instantly with our secure payment system. Get confirmation in seconds.
            </p>
          </div>

          <div className="group bg-white border border-stone-200 rounded-2xl p-8 shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-2">
            <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl mb-6 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-stone-800 mb-3">Community Connect</h3>
            <p className="text-stone-600 leading-relaxed">
              Join student organizations and discover events from clubs you care about.
            </p>
          </div>
        </div>

        <div className="bg-gradient-to-r from-orange-600 to-orange-500 rounded-3xl p-12 shadow-2xl text-center text-white">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Ready to explore campus life?
          </h2>
          <p className="text-xl mb-8 text-orange-100 max-w-2xl mx-auto">
            Join thousands of students discovering and attending events at top universities
          </p>
          <div className="flex flex-wrap justify-center gap-8 mb-8">
            <div className="text-center">
              <div className="text-4xl font-bold mb-1">500+</div>
              <div className="text-orange-100">Active Events</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold mb-1">50+</div>
              <div className="text-orange-100">Universities</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold mb-1">10K+</div>
              <div className="text-orange-100">Students</div>
            </div>
          </div>
          <Link
            to="/signup"
            className="inline-block bg-white text-orange-600 px-8 py-4 rounded-xl font-bold text-lg shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105"
          >
            Create Your Account
          </Link>
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
