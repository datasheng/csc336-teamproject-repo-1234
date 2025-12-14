import { describe, it, expect, vi } from 'vitest';
import { render, fireEvent } from '@testing-library/react';
import { EventFilters } from '../components/EventFilters';

describe('EventFilters', () => {
  it('should render filter inputs', () => {
    const mockOnFilterChange = vi.fn();
    const { getByLabelText } = render(<EventFilters onFilterChange={mockOnFilterChange} />);

    expect(getByLabelText('Campus ID')).toBeInTheDocument();
    expect(getByLabelText('Start Date')).toBeInTheDocument();
    expect(getByLabelText('End Date')).toBeInTheDocument();
  });

  it('should render Apply Filters and Clear buttons', () => {
    const mockOnFilterChange = vi.fn();
    const { getByText } = render(<EventFilters onFilterChange={mockOnFilterChange} />);

    expect(getByText('Apply Filters')).toBeInTheDocument();
    expect(getByText('Clear')).toBeInTheDocument();
  });

  it('should call onFilterChange with empty filters when Clear is clicked', () => {
    const mockOnFilterChange = vi.fn();
    const { getByText } = render(<EventFilters onFilterChange={mockOnFilterChange} />);

    const clearButton = getByText('Clear');
    fireEvent.click(clearButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({});
  });

  it('should call onFilterChange with campusId filter', () => {
    const mockOnFilterChange = vi.fn();
    const { getByLabelText, getByText } = render(<EventFilters onFilterChange={mockOnFilterChange} />);

    const campusInput = getByLabelText('Campus ID');
    fireEvent.change(campusInput, { target: { value: '1' } });

    const applyButton = getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({ campusId: 1 });
  });

  it('should call onFilterChange with date filters', () => {
    const mockOnFilterChange = vi.fn();
    const { getByLabelText, getByText } = render(<EventFilters onFilterChange={mockOnFilterChange} />);

    const startDateInput = getByLabelText('Start Date');
    const endDateInput = getByLabelText('End Date');

    fireEvent.change(startDateInput, { target: { value: '2025-01-01' } });
    fireEvent.change(endDateInput, { target: { value: '2025-12-31' } });

    const applyButton = getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({
      startDate: '2025-01-01',
      endDate: '2025-12-31',
    });
  });

  it('should call onFilterChange with all filters', () => {
    const mockOnFilterChange = vi.fn();
    const { getByLabelText, getByText } = render(<EventFilters onFilterChange={mockOnFilterChange} />);

    const campusInput = getByLabelText('Campus ID');
    const startDateInput = getByLabelText('Start Date');
    const endDateInput = getByLabelText('End Date');

    fireEvent.change(campusInput, { target: { value: '2' } });
    fireEvent.change(startDateInput, { target: { value: '2025-01-01' } });
    fireEvent.change(endDateInput, { target: { value: '2025-12-31' } });

    const applyButton = getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({
      campusId: 2,
      startDate: '2025-01-01',
      endDate: '2025-12-31',
    });
  });

  it('should clear all inputs when Clear is clicked', () => {
    const mockOnFilterChange = vi.fn();
    const { getByLabelText, getByText } = render(<EventFilters onFilterChange={mockOnFilterChange} />);

    const campusInput = getByLabelText('Campus ID') as HTMLInputElement;
    const startDateInput = getByLabelText('Start Date') as HTMLInputElement;
    const endDateInput = getByLabelText('End Date') as HTMLInputElement;

    fireEvent.change(campusInput, { target: { value: '1' } });
    fireEvent.change(startDateInput, { target: { value: '2025-01-01' } });
    fireEvent.change(endDateInput, { target: { value: '2025-12-31' } });

    const clearButton = getByText('Clear');
    fireEvent.click(clearButton);

    expect(campusInput.value).toBe('');
    expect(startDateInput.value).toBe('');
    expect(endDateInput.value).toBe('');
  });
});
