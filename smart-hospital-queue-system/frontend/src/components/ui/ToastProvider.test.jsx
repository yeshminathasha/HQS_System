import { act, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ToastProvider, useToast } from './ToastProvider';

function Probe() {
  const toast = useToast();
  return (
    <div>
      <button onClick={() => toast.success('Saved successfully')}>Success</button>
      <button onClick={() => toast.error('Something failed')}>Error</button>
      <button
        onClick={() => {
          toast.confirm('Delete this patient?').then((result) => {
            document.body.dataset.confirmResult = String(result);
          });
        }}
      >
        Ask
      </button>
    </div>
  );
}

describe('ToastProvider', () => {
  it('shows a success toast with the message', async () => {
    const user = userEvent.setup();
    render(
      <ToastProvider>
        <Probe />
      </ToastProvider>
    );

    await user.click(screen.getByRole('button', { name: 'Success' }));
    expect(screen.getByRole('status')).toHaveTextContent('Saved successfully');
  });

  it('auto-dismisses the toast after the timeout', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    try {
      const user = userEvent.setup();
      render(
        <ToastProvider>
          <Probe />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: 'Error' }));
      expect(screen.getByRole('status')).toHaveTextContent('Something failed');

      act(() => {
        vi.advanceTimersByTime(5000);
      });

      expect(screen.queryByRole('status')).not.toBeInTheDocument();
    } finally {
      vi.useRealTimers();
    }
  });

  it('resolves the confirm promise when confirmed', async () => {
    const user = userEvent.setup();
    render(
      <ToastProvider>
        <Probe />
      </ToastProvider>
    );

    await user.click(screen.getByRole('button', { name: 'Ask' }));
    const dialog = screen.getByRole('dialog', { name: 'Confirmation' });
    expect(dialog).toHaveTextContent('Delete this patient?');

    await user.click(screen.getByRole('button', { name: 'Confirm' }));
    await waitFor(() => expect(document.body.dataset.confirmResult).toBe('true'));
  });

  it('resolves the confirm promise as false when cancelled', async () => {
    const user = userEvent.setup();
    render(
      <ToastProvider>
        <Probe />
      </ToastProvider>
    );

    await user.click(screen.getByRole('button', { name: 'Ask' }));
    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    await waitFor(() => expect(document.body.dataset.confirmResult).toBe('false'));
  });
});
