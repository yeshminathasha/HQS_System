import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, Calendar, Clock, Activity, UserPlus, FileText, X } from 'lucide-react';

export function Sidebar({ open = false, onClose }) {
  const menuItems = [
    { name: 'Dashboard', icon: LayoutDashboard, path: '/' },
    { name: 'Patient Queue', icon: Users, path: '/queue' },
    { name: 'Emergency', icon: Activity, path: '/emergency' },
    { name: 'Appointments', icon: Calendar, path: '/appointments' },
    { name: 'History', icon: Clock, path: '/history' },
    { name: 'Register', icon: UserPlus, path: '/register' },
    { name: 'Reports', icon: FileText, path: '/reports' },
  ];

  return (
    <>
      {open && (
        <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={onClose} aria-hidden="true" />
      )}
      <aside
        className={`w-64 bg-white border-r border-gray-200 flex flex-col h-screen fixed left-0 top-0 z-50 transition-transform duration-200 ${
          open ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0`}
      >
        <div className="h-16 flex items-center justify-between px-6 border-b border-gray-200">
          <div className="flex items-center">
            <Activity className="h-8 w-8 text-primary-600 mr-3" />
            <span className="text-xl font-bold text-gray-900 tracking-tight">SmartQueue</span>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 lg:hidden" aria-label="Close menu">
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="flex-1 py-6 px-4 space-y-2 overflow-y-auto">
          <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4 px-2">Menu</div>
          {menuItems.map((item) => (
            <NavLink
              key={item.name}
              to={item.path}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-primary-50 text-primary-700'
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                }`
              }
            >
              <item.icon className="h-5 w-5 mr-3" />
              {item.name}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-gray-200">
          <div className="bg-primary-50 rounded-lg p-4">
            <p className="text-sm font-medium text-primary-900">Smart Hospital Queue System</p>
            <p className="text-xs text-primary-700 mt-1">Spring Boot + React</p>
          </div>
        </div>
      </aside>
    </>
  );
}
