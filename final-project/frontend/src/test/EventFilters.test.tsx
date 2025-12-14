import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, fireEvent, waitFor, screen } from '@testing-library/react';
import { EventFilters } from '../components/EventFilters';
import * as campusApi from '../api/campuses';

// Mock the campus API
vi.mock('../api/campuses', () => ({
  getAllCampuses: vi.fn(),
}));

describe('EventFilters', () => {
  const mockCampuses = [
    { id: 1, name: 'Harvard University', address: '123 Test', zipCode: '02138', city: 'Cambridge' },
    { id: 2, name: 'Stanford University', address: '456 Test', zipCode: '94305', city: 'Stanford' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(campusApi.getAllCampuses).mockResolvedValue(mockCampuses);
  });

  it('should render filter inputs', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByRole('combobox', { name: /campus/i })).toBeInTheDocument();
    });
    expect(screen.getByLabelText('Start Date')).toBeInTheDocument();
    expect(screen.getByLabelText('End Date')).toBeInTheDocument();
  });

  it('should render Apply Filters and Clear Filters buttons', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByText('Apply Filters')).toBeInTheDocument();
    });
    expect(screen.getByText('Clear Filters')).toBeInTheDocument();
  });

  it('should call onFilterChange with empty filters when Clear is clicked', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByText('Clear Filters')).toBeInTheDocument();
    });

    const clearButton = screen.getByText('Clear Filters');
    fireEvent.click(clearButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({});
  });

  it('should call onFilterChange with campusId filter', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByRole('option', { name: 'Harvard University' })).toBeInTheDocument();
    });

    const campusDropdown = screen.getByRole('combobox', { name: /campus/i });
    fireEvent.change(campusDropdown, { target: { value: '1' } });

    const applyButton = screen.getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({ campusId: 1 });
  });

  it('should call onFilterChange with date filters', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByLabelText('Start Date')).toBeInTheDocument();
    });

    const startDateInput = screen.getByLabelText('Start Date');
    const endDateInput = screen.getByLabelText('End Date');

    fireEvent.change(startDateInput, { target: { value: '2025-01-01' } });
    fireEvent.change(endDateInput, { target: { value: '2025-12-31' } });

    const applyButton = screen.getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({
      startDate: '2025-01-01',
      endDate: '2025-12-31',
    });
  });

  it('should call onFilterChange with all filters', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByRole('option', { name: 'Stanford University' })).toBeInTheDocument();
    });

    const campusDropdown = screen.getByRole('combobox', { name: /campus/i });
    const startDateInput = screen.getByLabelText('Start Date');
    const endDateInput = screen.getByLabelText('End Date');

    fireEvent.change(campusDropdown, { target: { value: '2' } });
    fireEvent.change(startDateInput, { target: { value: '2025-01-01' } });
    fireEvent.change(endDateInput, { target: { value: '2025-12-31' } });

    const applyButton = screen.getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith({
      campusId: 2,
      startDate: '2025-01-01',
      endDate: '2025-12-31',
    });
  });

  it('should clear all inputs when Clear Filters is clicked', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByRole('option', { name: 'Harvard University' })).toBeInTheDocument();
    });

    const campusDropdown = screen.getByRole('combobox', { name: /campus/i }) as HTMLSelectElement;
    const startDateInput = screen.getByLabelText('Start Date') as HTMLInputElement;
    const endDateInput = screen.getByLabelText('End Date') as HTMLInputElement;

    fireEvent.change(campusDropdown, { target: { value: '1' } });
    fireEvent.change(startDateInput, { target: { value: '2025-01-01' } });
    fireEvent.change(endDateInput, { target: { value: '2025-12-31' } });

    const clearButton = screen.getByText('Clear Filters');
    fireEvent.click(clearButton);

    expect(campusDropdown.value).toBe('');
    expect(startDateInput.value).toBe('');
    expect(endDateInput.value).toBe('');
  });

  it('should show All Campuses option in dropdown', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(screen.getByRole('option', { name: 'All Campuses' })).toBeInTheDocument();
    });
  });

  it('should fetch campuses on mount', async () => {
    const mockOnFilterChange = vi.fn();
    render(<EventFilters onFilterChange={mockOnFilterChange} />);

    await waitFor(() => {
      expect(campusApi.getAllCampuses).toHaveBeenCalledTimes(1);
    });
  });
});
