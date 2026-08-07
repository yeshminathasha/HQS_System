import React, { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

const ToastContext = createContext(null);

let idCounter = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const [confirmState, setConfirmState] = useState(null);
  const timers = useRef({});

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
    if (timers.current[id]) {
      clearTimeout(timers.current[id]);
      delete timers.current[id];
    }
  }, []);

  const push = useCallback((type, message) => {
    const id = ++idCounter;
    setToasts((prev) => [...prev, { id, type, message }]);
    timers.current[id] = setTimeout(() => dismiss(id), 4500);
    return id;
  }, [dismiss]);

  const confirm = useCallback((message) => new Promise((resolve) => {
    setConfirmState({ message, resolve });
  }), []);

  const handleConfirm = useCallback((result) => {
    if (confirmState) {
      confirmState.resolve(result);
      setConfirmState(null);
    }
  }, [confirmState]);

  const api = useMemo(() => ({
    success: (m) => push('success', m),
    error: (m) => push('error', m),
    info: (m) => push('info', m),
    confirm,
  }), [push, confirm]);

  return (
    <ToastContext.Provider value={api}>
      {children}

      <div className="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-80 max-w-[calc(100vw-2rem)]">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            role="status"
            className={`flex items-start gap-3 rounded-lg shadow-lg border px-4 py-3 text-sm bg-white ${
              toast.type === 'success' ? 'border-success-200 text-success-800' :
              toast.type === 'error' ? 'border-danger-200 text-danger-800' :
              'border-primary-200 text-primary-800'
            }`}
          >
            {toast.type === 'success' ? (
              <CheckCircle2 className="h-5 w-5 text-success-500 shrink-0 mt-0.5" />
            ) : toast.type === 'error' ? (
              <AlertCircle className="h-5 w-5 text-danger-500 shrink-0 mt-0.5" />
            ) : (
              <Info className="h-5 w-5 text-primary-500 shrink-0 mt-0.5" />
            )}
            <span className="flex-1">{toast.message}</span>
            <button onClick={() => dismiss(toast.id)} aria-label="Dismiss" className="text-gray-400 hover:text-gray-600">
              <X className="h-4 w-4" />
            </button>
          </div>
        ))}
      </div>

      {confirmState && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[90] p-4" role="dialog" aria-modal="true" aria-label="Confirmation">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-sm p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-2">Please confirm</h3>
            <p className="text-sm text-gray-600 mb-6">{confirmState.message}</p>
            <div className="flex justify-end space-x-3">
              <button onClick={() => handleConfirm(false)} className="btn btn-secondary">Cancel</button>
              <button onClick={() => handleConfirm(true)} className="btn btn-danger">Confirm</button>
            </div>
          </div>
        </div>
      )}
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
