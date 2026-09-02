import React from 'react'

function StatCard({ label, value, hint, icon, trend, trendType = 'positive', color = 'emerald' }) {
  const colorMap = {
    emerald: 'text-emerald-600 bg-emerald-50 border-emerald-100',
    amber: 'text-amber-600 bg-amber-50 border-amber-100',
    rose: 'text-rose-600 bg-rose-50 border-rose-100',
    indigo: 'text-indigo-600 bg-indigo-50 border-indigo-100',
    blue: 'text-blue-600 bg-blue-50 border-blue-100',
  }

  const activeColor = colorMap[color] || colorMap.emerald

  return (
    <div className="relative overflow-hidden rounded-xl border border-slate-200/80 bg-white p-5 shadow-sm transition-all duration-200 hover:shadow-md hover:border-slate-300 group">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{label}</p>
          <p className="mt-2 text-3xl font-bold tracking-tight text-slate-900">{value}</p>
        </div>
        {icon && (
          <div className={`flex h-11 w-11 items-center justify-center rounded-lg border ${activeColor} transition-transform group-hover:scale-105`}>
            {icon}
          </div>
        )}
      </div>

      <div className="mt-3 flex items-center justify-between border-t border-slate-100 pt-3 text-xs">
        <span className="text-slate-500">{hint}</span>
        {trend && (
          <span className={`inline-flex items-center font-medium ${
            trendType === 'positive' ? 'text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full' : 
            trendType === 'warning' ? 'text-amber-700 bg-amber-50 px-2 py-0.5 rounded-full' : 
            'text-rose-700 bg-rose-50 px-2 py-0.5 rounded-full'
          }`}>
            {trend}
          </span>
        )}
      </div>
    </div>
  )
}

export default StatCard
