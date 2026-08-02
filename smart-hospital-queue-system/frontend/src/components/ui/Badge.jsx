import React from 'react';

export function Badge({ children, variant = 'neutral', className = '' }) {
  const variants = {
    neutral: 'bg-gray-100 text-gray-800',
    success: 'bg-success-500/10 text-success-500', // Green tint
    warning: 'bg-warning-500/10 text-warning-500', // Amber tint
    danger: 'bg-danger-500/10 text-danger-500', // Red tint
    primary: 'bg-primary-100 text-primary-700', // Blue tint
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${variants[variant]} ${className}`}>
      {children}
    </span>
  );
}
