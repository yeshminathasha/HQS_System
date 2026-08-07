import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import Pagination from './Pagination';

describe('Pagination', () => {
  it('renders nothing when there is only one page', () => {
    const { container } = render(
      <Pagination page={0} totalPages={1} onPageChange={vi.fn()} totalElements={10} />
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders page info and record count', () => {
    render(
      <Pagination page={2} totalPages={5} onPageChange={vi.fn()} totalElements={42} />
    );
    expect(screen.getByText('Page 3 of 5')).toBeInTheDocument();
    expect(screen.getByText('42 record(s)')).toBeInTheDocument();
  });

  it('disables Prev on the first page and Next on the last page', () => {
    const { rerender } = render(
      <Pagination page={0} totalPages={3} onPageChange={vi.fn()} totalElements={30} />
    );
    expect(screen.getByRole('button', { name: 'Prev' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Next' })).toBeEnabled();

    rerender(
      <Pagination page={2} totalPages={3} onPageChange={vi.fn()} totalElements={30} />
    );
    expect(screen.getByRole('button', { name: 'Prev' })).toBeEnabled();
    expect(screen.getByRole('button', { name: 'Next' })).toBeDisabled();
  });

  it('calls onPageChange with the next/previous page', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<Pagination page={1} totalPages={3} onPageChange={onPageChange} totalElements={30} />);

    await user.click(screen.getByRole('button', { name: 'Prev' }));
    expect(onPageChange).toHaveBeenCalledWith(0);

    await user.click(screen.getByRole('button', { name: 'Next' }));
    expect(onPageChange).toHaveBeenCalledWith(2);
  });
});
