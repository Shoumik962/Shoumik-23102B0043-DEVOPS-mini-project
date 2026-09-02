import React, { useState, useEffect, useCallback } from 'react'
import Navbar from './components/Navbar'
import StatCard from './components/StatCard'
import NewEntryModal from './components/NewEntryModal'
import AlertsBanner from './components/AlertsBanner'

const statusStyles = {
  Reviewed: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20 hover:bg-emerald-100',
  Pending: 'bg-amber-50 text-amber-700 ring-amber-600/20 hover:bg-amber-100',
  Flagged: 'bg-rose-50 text-rose-700 ring-rose-600/20 hover:bg-rose-100',
}

function App() {
  const [stats, setStats] = useState({
    totalWasteLogged: '64.8 kg',
    totalWasteKg: 64.8,
    activeEntries: 2,
    reviewedEntries: 4,
    flaggedEntries: 2,
    reductionRate: '14.2%',
    openAlerts: 2,
    categoryBreakdown: {},
    sourceBreakdown: {},
  })
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [backendStatus, setBackendStatus] = useState({ online: false, data: null })
  const [activeTab, setActiveTab] = useState('dashboard')

  // Filters
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('All')
  const [categoryFilter, setCategoryFilter] = useState('All')

  // Modal
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [actionInProgressId, setActionInProgressId] = useState(null)
  const [notification, setNotification] = useState(null)

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type })
    setTimeout(() => {
      setNotification(null)
    }, 4000)
  }

  // Fetch all data
  const fetchData = useCallback(async (isManualRefresh = false) => {
    if (isManualRefresh) setIsRefreshing(true)
    try {
      // 1. Health check
      const healthRes = await fetch('/api/health').catch(() => null)
      if (healthRes && healthRes.ok) {
        const healthData = await healthRes.json()
        setBackendStatus({ online: true, data: healthData })
      } else {
        setBackendStatus({ online: false, data: null })
      }

      // 2. Stats
      const statsRes = await fetch('/api/stats').catch(() => null)
      if (statsRes && statsRes.ok) {
        const statsData = await statsRes.json()
        setStats(statsData)
      }

      // 3. Entries
      const entriesRes = await fetch('/api/entries').catch(() => null)
      if (entriesRes && entriesRes.ok) {
        const entriesData = await entriesRes.json()
        setEntries(entriesData)
      }
    } catch (err) {
      console.error('Error fetching dashboard data:', err)
    } finally {
      setLoading(false)
      if (isManualRefresh) setIsRefreshing(false)
    }
  }, [])

  useEffect(() => {
    fetchData()
    // Poll every 30 seconds
    const interval = setInterval(() => {
      fetchData()
    }, 30000)
    return () => clearInterval(interval)
  }, [fetchData])

  // Handle Status Update
  const handleStatusChange = async (entryId, newStatus) => {
    setActionInProgressId(entryId)
    try {
      const res = await fetch(`/api/entries/${entryId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus }),
      })
      if (res.ok) {
        const updated = await res.json()
        setEntries((prev) => prev.map((e) => (e.id === entryId ? updated : e)))
        showNotification(`Entry ${entryId} status updated to ${newStatus}.`)
        // Refresh stats
        fetch('/api/stats')
          .then((r) => r.json())
          .then((st) => setStats(st))
          .catch(() => {})
      } else {
        showNotification(`Failed to update status for ${entryId}`, 'error')
      }
    } catch {
      showNotification('Network error updating status', 'error')
    } finally {
      setActionInProgressId(null)
    }
  }

  // Handle Delete Entry
  const handleDeleteEntry = async (entryId) => {
    if (!window.confirm(`Are you sure you want to delete entry ${entryId}?`)) return
    setActionInProgressId(entryId)
    try {
      const res = await fetch(`/api/entries/${entryId}`, {
        method: 'DELETE',
      })
      if (res.ok) {
        setEntries((prev) => prev.filter((e) => e.id !== entryId))
        showNotification(`Entry ${entryId} deleted successfully.`)
        // Refresh stats
        fetch('/api/stats')
          .then((r) => r.json())
          .then((st) => setStats(st))
          .catch(() => {})
      } else {
        showNotification(`Failed to delete entry ${entryId}`, 'error')
      }
    } catch {
      showNotification('Network error deleting entry', 'error')
    } finally {
      setActionInProgressId(null)
    }
  }

  // Callback after new entry created
  const handleEntryCreated = (newEntry) => {
    setEntries((prev) => [newEntry, ...prev])
    showNotification(`New entry ${newEntry.id} recorded successfully!`)
    fetchData(true)
  }

  // Filtered Entries List
  const filteredEntries = entries.filter((entry) => {
    const matchesStatus = statusFilter === 'All' || entry.status.toLowerCase() === statusFilter.toLowerCase()
    const matchesCategory = categoryFilter === 'All' || entry.category.toLowerCase() === categoryFilter.toLowerCase()
    const matchesSearch =
      !searchQuery ||
      entry.id?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      entry.source?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      entry.category?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      entry.notes?.toLowerCase().includes(searchQuery.toLowerCase())
    return matchesStatus && matchesCategory && matchesSearch
  })

  // Extract unique categories for filter dropdown
  const uniqueCategories = ['All', ...Array.from(new Set(entries.map((e) => e.category).filter(Boolean)))]

  return (
    <div className="min-h-screen bg-slate-50/70 text-slate-800 antialiased selection:bg-emerald-100 selection:text-emerald-900">
      
      {/* Top Navigation */}
      <Navbar
        backendStatus={backendStatus}
        onRefresh={() => fetchData(true)}
        isRefreshing={isRefreshing}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      {/* Notification Toast */}
      {notification && (
        <div className="fixed bottom-5 right-5 z-50 flex items-center gap-2 rounded-xl bg-slate-900 px-4 py-3 text-sm text-white shadow-xl transition-all animate-in slide-in-from-bottom-5">
          {notification.type === 'success' ? (
            <svg className="h-5 w-5 text-emerald-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          ) : (
            <svg className="h-5 w-5 text-rose-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          )}
          <span>{notification.message}</span>
        </div>
      )}

      {/* Main Content */}
      <main className="mx-auto max-w-7xl px-4 sm:px-6 py-8">
        
        {/* Page Title & Actions */}
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
              Food Waste Tracking Dashboard
            </h1>
            <p className="mt-1 text-sm text-slate-500">
              Live monitoring, automated auditing, and waste reduction telemetry.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsModalOpen(true)}
              className="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-emerald-500 active:bg-emerald-700 transition-all cursor-pointer hover:shadow-md"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              <span>Log Waste Entry</span>
            </button>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8">
          <StatCard
            label="Total Waste Logged"
            value={stats.totalWasteLogged || `${stats.totalWasteKg || 0} kg`}
            hint="Aggregated across all locations"
            color="emerald"
            trend={stats.reductionRate ? `${stats.reductionRate} reduction` : '-12.5%'}
            trendType="positive"
            icon={
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 6l3 18h12l3-18H3zM9 6V4a2 2 0 012-2h2a2 2 0 012 2v2" />
              </svg>
            }
          />
          <StatCard
            label="Active Entries"
            value={String(stats.activeEntries ?? 0)}
            hint="Pending supervisor review"
            color="amber"
            trend="Needs audit"
            trendType="warning"
            icon={
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
          />
          <StatCard
            label="Reviewed & Verified"
            value={String(stats.reviewedEntries ?? (entries.length - (stats.activeEntries || 0)))}
            hint="Audited and cleared"
            color="indigo"
            trend="Compliant"
            trendType="positive"
            icon={
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
          />
          <StatCard
            label="Open Alerts / Incidents"
            value={String(stats.openAlerts ?? 0)}
            hint="Flagged temperature or spoilage"
            color="rose"
            trend={stats.openAlerts > 0 ? 'Requires attention' : 'All clear'}
            trendType={stats.openAlerts > 0 ? 'danger' : 'positive'}
            icon={
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            }
          />
        </div>

        {/* Analytics & DevOps Telemetry Section */}
        <div className="mb-8">
          <AlertsBanner
            stats={stats}
            backendStatus={backendStatus}
            entries={entries}
            onFilterByStatus={(st) => {
              setStatusFilter(st)
              setActiveTab('entries')
            }}
          />
        </div>

        {/* Table / Waste Logs Section */}
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs">
          
          {/* Table Header & Controls */}
          <div className="border-b border-slate-200/80 px-6 py-5">
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div>
                <h2 className="text-base font-bold text-slate-900">Food Waste Logs</h2>
                <p className="text-xs text-slate-500">
                  Showing {filteredEntries.length} of {entries.length} total entries recorded.
                </p>
              </div>

              {/* Filters Bar */}
              <div className="flex flex-wrap items-center gap-3">
                {/* Search */}
                <div className="relative min-w-[200px]">
                  <input
                    type="text"
                    placeholder="Search ID, source, notes..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full rounded-lg border border-slate-300 bg-white pl-9 pr-3 py-1.5 text-xs text-slate-800 placeholder-slate-400 shadow-2xs focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                  />
                  <svg className="absolute left-2.5 top-2 h-4 w-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                </div>

                {/* Status Filter */}
                <div className="flex items-center gap-1">
                  <span className="text-xs font-semibold text-slate-500">Status:</span>
                  <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-800 shadow-2xs focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                  >
                    <option value="All">All Statuses</option>
                    <option value="Reviewed">Reviewed</option>
                    <option value="Pending">Pending</option>
                    <option value="Flagged">Flagged</option>
                  </select>
                </div>

                {/* Category Filter */}
                <div className="flex items-center gap-1">
                  <span className="text-xs font-semibold text-slate-500">Category:</span>
                  <select
                    value={categoryFilter}
                    onChange={(e) => setCategoryFilter(e.target.value)}
                    className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-800 shadow-2xs focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                  >
                    {uniqueCategories.map((c) => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          </div>

          {/* Table Element */}
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
              <thead className="bg-slate-50/80 font-semibold uppercase tracking-wider text-slate-500">
                <tr>
                  <th scope="col" className="px-6 py-3.5">ID</th>
                  <th scope="col" className="px-6 py-3.5">Source</th>
                  <th scope="col" className="px-6 py-3.5">Category</th>
                  <th scope="col" className="px-6 py-3.5">Quantity</th>
                  <th scope="col" className="px-6 py-3.5">Date</th>
                  <th scope="col" className="px-6 py-3.5">Notes</th>
                  <th scope="col" className="px-6 py-3.5">Status</th>
                  <th scope="col" className="px-6 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {loading ? (
                  <tr>
                    <td colSpan="8" className="py-12 text-center text-slate-400">
                      <div className="flex items-center justify-center gap-2">
                        <svg className="h-5 w-5 animate-spin text-emerald-500" viewBox="0 0 24 24" fill="none">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                        </svg>
                        <span>Loading food waste data...</span>
                      </div>
                    </td>
                  </tr>
                ) : filteredEntries.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="py-12 text-center text-slate-400">
                      No entries found matching current filter criteria.
                    </td>
                  </tr>
                ) : (
                  filteredEntries.map((entry) => (
                    <tr key={entry.id} className="hover:bg-slate-50/80 transition-colors">
                      {/* ID */}
                      <td className="whitespace-nowrap px-6 py-4 font-mono font-bold text-slate-900">
                        {entry.id}
                      </td>

                      {/* Source */}
                      <td className="whitespace-nowrap px-6 py-4 font-medium text-slate-800">
                        <div className="flex items-center gap-1.5">
                          <div className="h-2 w-2 rounded-full bg-slate-400" />
                          <span>{entry.source}</span>
                        </div>
                      </td>

                      {/* Category */}
                      <td className="whitespace-nowrap px-6 py-4 text-slate-600">
                        <span className="inline-flex items-center rounded-md bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700">
                          {entry.category}
                        </span>
                      </td>

                      {/* Quantity */}
                      <td className="whitespace-nowrap px-6 py-4 font-semibold text-slate-900">
                        {entry.quantity} {entry.unit || 'kg'}
                      </td>

                      {/* Date */}
                      <td className="whitespace-nowrap px-6 py-4 text-slate-500">
                        {entry.date || '—'}
                      </td>

                      {/* Notes */}
                      <td className="px-6 py-4 text-slate-500 max-w-xs truncate" title={entry.notes}>
                        {entry.notes || <span className="italic text-slate-300">No notes</span>}
                      </td>

                      {/* Status */}
                      <td className="whitespace-nowrap px-6 py-4">
                        <div className="relative inline-block">
                          <select
                            value={entry.status}
                            disabled={actionInProgressId === entry.id}
                            onChange={(e) => handleStatusChange(entry.id, e.target.value)}
                            className={`cursor-pointer appearance-none rounded-full px-3 py-1 pr-6 text-xs font-semibold ring-1 ring-inset transition-colors ${
                              statusStyles[entry.status] || statusStyles.Pending
                            }`}
                          >
                            <option value="Reviewed">Reviewed</option>
                            <option value="Pending">Pending</option>
                            <option value="Flagged">Flagged</option>
                          </select>
                          <svg className="pointer-events-none absolute right-2 top-2 h-3 w-3 opacity-60" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                          </svg>
                        </div>
                      </td>

                      {/* Actions */}
                      <td className="whitespace-nowrap px-6 py-4 text-right">
                        <button
                          onClick={() => handleDeleteEntry(entry.id)}
                          disabled={actionInProgressId === entry.id}
                          title="Delete entry"
                          className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600 transition-colors cursor-pointer disabled:opacity-40"
                        >
                          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

      </main>

      {/* New Entry Modal */}
      <NewEntryModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmitSuccess={handleEntryCreated}
      />

    </div>
  )
}

export default App
