function Navbar() {
  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <div className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-md bg-slate-900" />
          <span className="text-lg font-semibold tracking-tight text-slate-900">
            Food Waste Tracker
          </span>
        </div>
        <nav className="flex items-center gap-6 text-sm font-medium text-slate-600">
          <a href="#" className="text-slate-900">Dashboard</a>
          <a href="#" className="hover:text-slate-900">Entries</a>
          <a href="#" className="hover:text-slate-900">Alerts</a>
          <a href="#" className="hover:text-slate-900">Settings</a>
        </nav>
      </div>
    </header>
  )
}

export default Navbar
