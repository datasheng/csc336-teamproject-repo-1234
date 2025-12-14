import { describe, it, expect, vi } from 'vitest';
import { render, fireEvent } from '@testing-library/react';
import { Pagination } from '../components/Pagination';

describe('Pagination', () => {
  it('should not render when totalPages is 1', () => {
    const mockOnPageChange = vi.fn();
    const { container } = render(
      <Pagination currentPage={1} totalPages={1} onPageChange={mockOnPageChange} />
    );
    expect(container.firstChild).toBeNull();
  });

  it('should render Previous and Next buttons', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={1} totalPages={5} onPageChange={mockOnPageChange} />
    );
    expect(getByText('Previous')).toBeInTheDocument();
    expect(getByText('Next')).toBeInTheDocument();
  });

  it('should disable Previous button on first page', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={1} totalPages={5} onPageChange={mockOnPageChange} />
    );
    const prevButton = getByText('Previous') as HTMLButtonElement;
    expect(prevButton.disabled).toBe(true);
  });

  it('should disable Next button on last page', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={5} totalPages={5} onPageChange={mockOnPageChange} />
    );
    const nextButton = getByText('Next') as HTMLButtonElement;
    expect(nextButton.disabled).toBe(true);
  });

  it('should call onPageChange with previous page number', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={3} totalPages={5} onPageChange={mockOnPageChange} />
    );
    const prevButton = getByText('Previous');
    fireEvent.click(prevButton);
    expect(mockOnPageChange).toHaveBeenCalledWith(2);
  });

  it('should call onPageChange with next page number', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={3} totalPages={5} onPageChange={mockOnPageChange} />
    );
    const nextButton = getByText('Next');
    fireEvent.click(nextButton);
    expect(mockOnPageChange).toHaveBeenCalledWith(4);
  });

  it('should call onPageChange when clicking page number', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={1} totalPages={5} onPageChange={mockOnPageChange} />
    );
    const page3Button = getByText('3');
    fireEvent.click(page3Button);
    expect(mockOnPageChange).toHaveBeenCalledWith(3);
  });

  it('should render all page numbers when totalPages <= 5', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={1} totalPages={4} onPageChange={mockOnPageChange} />
    );
    expect(getByText('1')).toBeInTheDocument();
    expect(getByText('2')).toBeInTheDocument();
    expect(getByText('3')).toBeInTheDocument();
    expect(getByText('4')).toBeInTheDocument();
  });

  it('should render ellipsis when totalPages > 5', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={1} totalPages={10} onPageChange={mockOnPageChange} />
    );
    expect(getByText('...')).toBeInTheDocument();
  });

  it('should highlight current page', () => {
    const mockOnPageChange = vi.fn();
    const { getByText } = render(
      <Pagination currentPage={3} totalPages={5} onPageChange={mockOnPageChange} />
    );
    const currentPageButton = getByText('3');
    expect(currentPageButton.className).toContain('bg-blue-600');
  });
});
