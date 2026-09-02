import React from 'react'

function AlertsBanner({ stats, backendStatus, entries, onFilterByStatus }) {
  const flaggedEntries = entries.filter((e) => e.status === 'Flagged')

  const categoryEntries = stats?.categoryBreakdown ? Object.entries(stats.categoryBreakdown) : []
  const maxCategoryVal = Math.max(...categoryEntries.map(([, val]) => val), 1)

  return (
    <div className="space-y-6">
      
      {/* Flagged Attention Alert */}
      {flaggedEntries.length > 0 && (
        <div className="rounded-xl border border-rose-200 bg-gradient-to-r from-rose-50 to-orange-50 p-4 shadow-xs">
          <div className="flex items-start gap-3">
            <div className="rounded-lg bg-rose-100 p-2 text-rose-600">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-bold text-rose-900">
                  {flaggedEntries.length} Waste Incident{flaggedEntries.length > 1 ? 's' : ''} Flagged For Action
                </h4>
                <button
                  onClick={() => onFilterByStatus('Flagged')}
                  className="text-xs font-semibold text-rose-700 hover:underline cursor-pointer"
                >
                  View Flagged &rarr;
                </button>
              </div>
              <p className="mt-1 text-xs text-rose-700">
                Incidents caused by refrigeration sensor glitches, humidity spikes, or handling issues require supervisor inspection.
              </p>
              <div className="mt-2 flex flex-wrap gap-2">
                {flaggedEntries.map((entry) => (
                  <span
                    key={entry.id}
                    className="inline-flex items-center gap-1 rounded-md bg-white/90 px-2 py-1 text-xs font-medium text-rose-800 border border-rose-200"
                  >
                    <span className="font-semibold">{entry.id}</span>: {entry.source} ({entry.quantity} {entry.unit}) — {entry.notes || entry.category}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Analytics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        
        {/* Category Breakdown */}
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <h3 className="text-sm font-bold tracking-tight text-slate-900 flex items-center justify-between">
            <span>Waste Distribution by Category</span>
            <span className="text-xs font-normal text-slate-500">Total: {stats?.totalWasteKg || 0} kg</span>
          </h3>
          <div className="mt-4 space-y-3">
            {categoryEntries.length === 0 ? (
              <p className="text-xs text-slate-400 py-4 text-center">No category data logged yet.</p>
            ) : (
              categoryEntries.map(([cat, kg]) => {
                const pct = Math.round((kg / (stats?.totalWasteKg || 1)) * 100)
                return (
                  <div key={cat}>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="font-medium text-slate-700">{cat}</span>
                      <span className="text-slate-500">{kg.toFixed(1)} kg ({pct}%)</span>
                    </div>
                    <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                      <div
                        className="h-full rounded-full bg-emerald-500 transition-all duration-500"
                        style={{ width: `${Math.min(100, Math.max(6, (kg / maxCategoryVal) * 100))}%` }}
                      />
                    </div>
                  </div>
                )
              })
            )}
          </div>
        </div>

        {/* DevOps System & API Monitoring Card */}
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold tracking-tight text-slate-900">
                DevOps System & API Endpoints
              </h3>
              <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold ${
                backendStatus.online ? 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-600/20' : 'bg-rose-50 text-rose-700 ring-1 ring-rose-600/20'
              }`}>
                {backendStatus.online ? 'Healthy' : 'Disconnected'}
              </span>
            </div>
            <p className="mt-1 text-xs text-slate-500">
              Live telemetry and automated healthcheck endpoints for CI/CD pipelines & Docker orchestration.
            </p>

            <div className="mt-4 space-y-2 text-xs">
              <div className="flex items-center justify-between rounded-lg bg-slate-50 p-2.5 border border-slate-100 font-mono">
                <span className="text-emerald-700 font-semibold">GET /api/health</span>
                <a
                  href="/api/health"
                  target="_blank"
                  rel="noreferrer"
                  className="text-indigo-600 hover:text-indigo-800 font-sans font-medium"
                >
                  Inspect &rarr;
                </a>
              </div>
              <div className="flex items-center justify-between rounded-lg bg-slate-50 p-2.5 border border-slate-100 font-mono">
                <span className="text-emerald-700 font-semibold">GET /api/stats</span>
                <a
                  href="/api/stats"
                  target="_blank"
                  rel="noreferrer"
                  className="text-indigo-600 hover:text-indigo-800 font-sans font-medium"
                >
                  Inspect &rarr;
                </a>
              </div>
              <div className="flex items-center justify-between rounded-lg bg-slate-50 p-2.5 border border-slate-100 font-mono">
                <span className="text-emerald-700 font-semibold">GET /api/entries</span>
                <a
                  href="/api/entries"
                  target="_blank"
                  rel="noreferrer"
                  className="text-indigo-600 hover:text-indigo-800 font-sans font-medium"
                >
                  Inspect &rarr;
                </a>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
            <span>Uptime: {backendStatus.data?.uptimeSeconds ? `${backendStatus.data.uptimeSeconds}s` : 'Active'}</span>
            <span>Java: {backendStatus.data?.javaVersion || 'JDK 17/21'}</span>
            <span>Architecture: Microservices + Vite SPA</span>
          </div>
        </div>

      </div>

    </div>
  )
}

export default AlertsBanner
