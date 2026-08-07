import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Menu, Search, UserCircle } from 'lucide-react';

export function Topbar({ onMenuClick }) {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');

  const submitSearch = (e) => {
    e.preventDefault();
    const trimmed = query.trim();
    if (trimmed) {
      navigate(`/queue?search=${encodeURIComponent(trimmed)}`);
    }
  };

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 sticky top-0 z-30 lg:ml-64">
      <div className="flex items-center flex-1 gap-4">
        <button
          onClick={onMenuClick}
          className="p-2 rounded-md text-gray-500 hover:bg-gray-100 lg:hidden"
          aria-label="Open menu"
        >
          <Menu className="h-5 w-5" />
        </button>
        <form onSubmit={submitSearch} className="relative flex-1 max-w-96">
          <span className="absolute inset-y-0 left-0 flex items-center pl-3">
            <Search className="h-5 w-5 text-gray-400" />
          </span>
          <input
            type="text"
            className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg leading-5 bg-gray-50 placeholder-gray-500 focus:outline-none focus:bg-white focus:ring-1 focus:ring-primary-500 focus:border-primary-500 sm:text-sm transition-colors"
            placeholder="Search patients, IDs, or doctors..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </form>
      </div>

      <div className="flex items-center space-x-3 border-l border-gray-200 pl-4">
        <div className="flex flex-col text-right">
          <span className="text-sm font-semibold text-gray-900">Reception Desk</span>
          <span className="text-xs text-gray-500">SmartQueue Staff</span>
        </div>
        <UserCircle className="h-10 w-10 text-gray-400" />
      </div>
    </header>
  );
}
