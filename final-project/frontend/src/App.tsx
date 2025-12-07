import { Routes, Route } from 'react-router-dom'

function HomePage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 to-purple-700 flex items-center justify-center">
      <div className="text-center text-white px-4">
        <h1 className="text-5xl font-bold mb-4">Campus Events Platform</h1>
        <p className="text-xl opacity-90 mb-8">Coming Soon</p>
        <div className="bg-white/10 backdrop-blur-sm rounded-lg p-6 max-w-md mx-auto">
          <p className="text-lg">
            Your one-stop destination for discovering, organizing, and attending
            campus events.
          </p>
        </div>
        <div className="mt-8 flex justify-center gap-4">
          <div className="bg-white/20 rounded-full px-6 py-2">
            <span className="text-sm">🎓 Universities</span>
          </div>
          <div className="bg-white/20 rounded-full px-6 py-2">
            <span className="text-sm">🎉 Events</span>
          </div>
          <div className="bg-white/20 rounded-full px-6 py-2">
            <span className="text-sm">🎫 Tickets</span>
          </div>
        </div>
      </div>
    </div>
  )
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
    </Routes>
  )
}

export default App
