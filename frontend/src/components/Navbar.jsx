import React from 'react'

function Navbar({ backendStatus, onRefresh, isRefreshing, activeTab, setActiveTab }) {
  return (
    <header className="sticky top-0 z-30 border-b border-slate-200/80 bg-white/95 backdrop-blur-md transition-all shadow-xs">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 sm:px-6 py-3.5">
        
        {/* Brand / Logo */}
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white shadow-sm shadow-emerald-500/20">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
            </svg>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-lg font-bold tracking-tight text-slate-900">
                EcoTrack Food Waste
              </span>
              <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider text-slate-600 border border-slate-200">
                DevOps v0.1.0
              </span>
            </div>
            <p className="text-xs text-slate-500 hidden sm:block">Campus & Facility Waste Reduction System</p>
          </div>
        </div>

        {/* Center Tabs */}
        <nav className="hidden md:flex items-center gap-1 rounded-lg bg-slate-100/80 p-1 border border-slate-200/60 text-xs font-semibold">
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`rounded-md px-3.5 py-1.5 transition-all ${
              activeTab === 'dashboard'
                ? 'bg-white text-slate-900 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Dashboard
          </button>
          <button
            onClick={() => setActiveTab('entries')}
            className={`rounded-md px-3.5 py-1.5 transition-all ${
              activeTab === 'entries'
                ? 'bg-white text-slate-900 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Waste Logs
          </button>
          <button
            onClick={() => setActiveTab('analytics')}
            className={`rounded-md px-3.5 py-1.5 transition-all ${
              activeTab === 'analytics'
                ? 'bg-white text-slate-900 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Analytics & Alerts
          </button>
        </nav>

        {/* Right Status & Controls */}
        <div className="flex items-center gap-3">
          {/* Backend Status Indicator */}
          <div className="flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50/80 px-3 py-1 text-xs font-medium">
            <span className="relative flex h-2 w-2">
              {backendStatus.online ? (
                <>
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75"></span>
                  <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500"></span>
                </>
              ) : (
                <>
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-rose-400 opacity-75"></span>
                  <span className="relative inline-flex h-2 w-2 rounded-full bg-rose-500"></span>
                </>
              )}
            </span>
            <span className={backendStatus.online ? 'text-slate-700' : 'text-rose-700 font-semibold'}>
              {backendStatus.online ? 'Backend: 8081 UP' : 'Backend: Disconnected'}
            </span>
          </div>

          {/* Refresh Button */}
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            title="Refresh data from backend"
            className="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 shadow-xs hover:bg-slate-50 hover:text-slate-900 transition-colors disabled:opacity-50"
          >
            <svg
              className={`h-3.5 w-3.5 text-slate-500 ${isRefreshing ? 'animate-spin text-emerald-600' : ''}`}
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
            <span className="hidden sm:inline">Refresh</span>
          </button>
        </div>

      </div>
    </header>
  )
}

export default Navbar
