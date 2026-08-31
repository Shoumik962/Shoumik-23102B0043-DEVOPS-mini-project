import StatCard from './components/StatCard'
import Navbar from './components/Navbar'

const stats = [
  { label: 'Total Waste Logged', value: '1,284 kg', hint: 'Last 30 days' },
  { label: 'Active Entries', value: '42', hint: 'Awaiting review' },
  { label: 'Reduction Rate', value: '12.5%', hint: 'Compared to prior month' },
  { label: 'Open Alerts', value: '3', hint: 'Requires attention' },
]

const recentEntries = [
  { id: 'FW-1042', source: 'Kitchen A', category: 'Produce', quantity: '8.2 kg', status: 'Reviewed' },
  { id: 'FW-1041', source: 'Cafeteria', category: 'Prepared Food', quantity: '15.6 kg', status: 'Pending' },
  { id: 'FW-1040', source: 'Kitchen B', category: 'Dairy', quantity: '3.4 kg', status: 'Reviewed' },
  { id: 'FW-1039', source: 'Storage', category: 'Bakery', quantity: '6.1 kg', status: 'Flagged' },
]

const statusStyles = {
  Reviewed: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  Pending: 'bg-amber-50 text-amber-700 ring-amber-600/20',
  Flagged: 'bg-rose-50 text-rose-700 ring-rose-600/20',
}

function App() {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <div className="mb-8">
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900">
            Overview
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Summary of food waste activity across all tracked locations.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {stats.map((stat) => (
            <StatCard key={stat.label} {...stat} />
          ))}
        </div>

        <div className="mt-10 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
            <h2 className="text-base font-semibold text-slate-900">Recent Entries</h2>
            <button className="rounded-md bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-700 transition-colors">
              New Entry
            </button>
          </div>

          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Source</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Category</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Quantity</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {recentEntries.map((entry) => (
                <tr key={entry.id} className="hover:bg-slate-50">
                  <td className="px-6 py-3 text-sm font-medium text-slate-900">{entry.id}</td>
                  <td className="px-6 py-3 text-sm text-slate-600">{entry.source}</td>
                  <td className="px-6 py-3 text-sm text-slate-600">{entry.category}</td>
                  <td className="px-6 py-3 text-sm text-slate-600">{entry.quantity}</td>
                  <td className="px-6 py-3 text-sm">
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${statusStyles[entry.status]}`}>
                      {entry.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  )
}

export default App
