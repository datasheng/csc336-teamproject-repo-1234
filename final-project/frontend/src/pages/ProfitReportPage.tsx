import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getProfitReport, ProfitReport, ErrorResponse } from '../api/profit';

export const ProfitReportPage = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [report, setReport] = useState<ProfitReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastRefresh, setLastRefresh] = useState<Date>(new Date());

  useEffect(() => {
    // Check authentication and admin status
    if (!isAuthenticated || !user?.isAdmin) {
      navigate('/');
      return;
    }

    fetchReport();
    
    // Auto-refresh every 30 seconds
    const interval = setInterval(() => {
      fetchReport();
    }, 30000);

    return () => clearInterval(interval);
  }, [isAuthenticated, user, navigate]);

  const fetchReport = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getProfitReport();
      setReport(data);
      setLastRefresh(new Date());
    } catch (err: any) {
      const errorMsg = err.message || (err as ErrorResponse)?.message || 'Failed to load profit report';
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated || !user?.isAdmin) {
    return null;
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="min-h-screen bg-stone-50 py-8 px-4">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-stone-700 mb-2">Profit Report</h1>
          <p className="text-stone-600">Real-time revenue and profit analytics</p>
          <div className="mt-4 flex items-center gap-4">
            <button
              onClick={fetchReport}
              disabled={loading}
              className="px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors disabled:opacity-50"
            >
              {loading ? 'Refreshing...' : 'Refresh'}
            </button>
            <span className="text-sm text-stone-500">
              Last updated: {lastRefresh.toLocaleTimeString()}
            </span>
          </div>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        {loading && !report ? (
          <div className="text-center py-12">
            <p className="text-stone-600">Loading profit report...</p>
          </div>
        ) : report ? (
          <div className="space-y-6">
            {/* Summary Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white border border-stone-200 rounded-lg p-6 shadow-sm">
                <h3 className="text-sm font-medium text-stone-600 mb-2">Total Revenue</h3>
                <p className="text-3xl font-bold text-stone-700">
                  {formatCurrency(report.totalRevenue)}
                </p>
              </div>
              <div className="bg-white border border-stone-200 rounded-lg p-6 shadow-sm">
                <h3 className="text-sm font-medium text-stone-600 mb-2">Total Profit (10%)</h3>
                <p className="text-3xl font-bold text-green-600">
                  {formatCurrency(report.totalProfit)}
                </p>
              </div>
              <div className="bg-white border border-stone-200 rounded-lg p-6 shadow-sm">
                <h3 className="text-sm font-medium text-stone-600 mb-2">Total Tickets Sold</h3>
                <p className="text-3xl font-bold text-stone-700">
                  {report.totalTicketsSold.toLocaleString()}
                </p>
              </div>
            </div>

            {/* Profit by Event */}
            <div className="bg-white border border-stone-200 rounded-lg shadow-sm overflow-hidden">
              <div className="px-6 py-4 border-b border-stone-200">
                <h2 className="text-xl font-semibold text-stone-700">Profit by Event</h2>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-stone-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Event
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Organizer
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Tickets Sold
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Revenue
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Profit (10%)
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-stone-200">
                    {report.profitByEvent.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-6 py-8 text-center text-stone-500">
                          No ticket sales yet
                        </td>
                      </tr>
                    ) : (
                      report.profitByEvent.map((event) => (
                        <tr key={event.eventId} className="hover:bg-stone-50">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm font-medium text-stone-900">
                              {event.eventDescription}
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm text-stone-600">{event.organizerName}</div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-stone-600">
                            {event.ticketsSold}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium text-stone-700">
                            {formatCurrency(event.revenue)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-bold text-green-600">
                            {formatCurrency(event.profit)}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Profit by Date */}
            <div className="bg-white border border-stone-200 rounded-lg shadow-sm overflow-hidden">
              <div className="px-6 py-4 border-b border-stone-200">
                <h2 className="text-xl font-semibold text-stone-700">Profit by Date (Last 30 Days)</h2>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-stone-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Date
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Tickets Sold
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Revenue
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-stone-500 uppercase tracking-wider">
                        Profit (10%)
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-stone-200">
                    {report.profitByDate.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="px-6 py-8 text-center text-stone-500">
                          No ticket sales in the last 30 days
                        </td>
                      </tr>
                    ) : (
                      report.profitByDate.map((dateData, index) => (
                        <tr key={index} className="hover:bg-stone-50">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm font-medium text-stone-900">
                              {new Date(dateData.date).toLocaleDateString()}
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-stone-600">
                            {dateData.ticketsSold}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium text-stone-700">
                            {formatCurrency(dateData.revenue)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-bold text-green-600">
                            {formatCurrency(dateData.profit)}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};

