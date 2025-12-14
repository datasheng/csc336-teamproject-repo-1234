import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { EventFilters } from './EventFilters';
import * as campusApi from '../api/campuses';

// Mock the campus API
vi.mock('../api/campuses', () => ({
  getAllCampuses: vi.fn(),
}));

describe('EventFilters', () => {
  const mockOnFilterChange = vi.fn();
  const mockCampuses = [
    { id: 1, name: 'Harvard University', address: '123 Test', zipCode: '02138', city: 'Cambridge' },
    { id: 2, name: 'Stanford University', address: '456 Test', zipCode: '94305', city: 'Stanford' },
    { id: 3, name: 'Yale University', address: '789 Test', zipCode: '06510', city: 'New Haven' },
    { id: 4, name: 'UC Berkeley', address: '101 Test', zipCode: '94720', city: 'Berkeley' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(campusApi.getAllCampuses).mockResolvedValue(mockCampuses);
  });

  describe('Campus Dropdown', () => {
    it('renders campus dropdown instead of text input', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(screen.getByRole('combobox', { name: /campus/i })).toBeInTheDocument();
      });

      // Should not have a text input for campus
      expect(screen.queryByPlaceholderText(/enter campus id/i)).not.toBeInTheDocument();
    });

    it('shows loading state while fetching campuses', () => {
      // Delay the promise resolution
      vi.mocked(campusApi.getAllCampuses).mockReturnValue(new Promise(() => {}));

      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      const dropdown = screen.getByRole('combobox', { name: /campus/i });
      expect(dropdown).toBeDisabled();
    });

    it('populates dropdown with campuses after loading', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'Harvard University' })).toBeInTheDocument();
      });

      expect(screen.getByRole('option', { name: 'Stanford University' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Yale University' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'UC Berkeley' })).toBeInTheDocument();
    });

    it('includes "All Campuses" as default option', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'All Campuses' })).toBeInTheDocument();
      });
    });

    it('calls getAllCampuses on mount', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(campusApi.getAllCampuses).toHaveBeenCalledTimes(1);
      });
    });

    it('handles API error gracefully', async () => {
      vi.mocked(campusApi.getAllCampuses).mockRejectedValue(new Error('API Error'));
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalledWith('Error fetching campuses:', expect.any(Error));
      });

      // Dropdown should still be enabled with just "All Campuses" option
      expect(screen.getByRole('combobox', { name: /campus/i })).not.toBeDisabled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('Filter Application', () => {
    it('applies campus filter when campus is selected', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'Harvard University' })).toBeInTheDocument();
      });

      // Select a campus
      fireEvent.change(screen.getByRole('combobox', { name: /campus/i }), { target: { value: '1' } });

      // Click apply
      fireEvent.click(screen.getByRole('button', { name: /apply filters/i }));

      expect(mockOnFilterChange).toHaveBeenCalledWith({ campusId: 1 });
    });

    it('does not include campusId when "All Campuses" is selected', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'All Campuses' })).toBeInTheDocument();
      });

      // Apply with default selection (All Campuses)
      fireEvent.click(screen.getByRole('button', { name: /apply filters/i }));

      expect(mockOnFilterChange).toHaveBeenCalledWith({});
    });

    it('combines campus filter with date filters', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'Stanford University' })).toBeInTheDocument();
      });

      // Select campus
      fireEvent.change(screen.getByRole('combobox', { name: /campus/i }), { target: { value: '2' } });

      // Set dates
      fireEvent.change(screen.getByLabelText(/start date/i), { target: { value: '2024-01-01' } });
      fireEvent.change(screen.getByLabelText(/end date/i), { target: { value: '2024-12-31' } });

      // Apply
      fireEvent.click(screen.getByRole('button', { name: /apply filters/i }));

      expect(mockOnFilterChange).toHaveBeenCalledWith({
        campusId: 2,
        startDate: '2024-01-01',
        endDate: '2024-12-31',
      });
    });
  });

  describe('Filter Clearing', () => {
    it('clears campus selection when clear button is clicked', async () => {
      render(<EventFilters onFilterChange={mockOnFilterChange} />);

      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'UC Berkeley' })).toBeInTheDocument();
      });

      // Select a campus
      const dropdown = screen.getByRole('combobox', { name: /campus/i });
      fireEvent.change(dropdown, { target: { value: '4' } });

      // Verify selection
      expect(dropdown).toHaveValue('4');

      // Clear filters
      fireEvent.click(screen.getByRole('button', { name: /clear filters/i }));

      // Verify cleared
      expect(dropdown).toHaveValue('');
      expect(mockOnFilterChange).toHaveBeenCalledWith({});
    });
  });
});
