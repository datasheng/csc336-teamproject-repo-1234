import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { EventCard } from '../components/EventCard';
import { EventDTO } from '../api/events';

describe('EventCard', () => {
  const mockEvent: EventDTO = {
    id: 1,
    organizerId: 1,
    organizerName: 'Computer Science Club',
    campusId: 1,
    campusName: 'Harvard University',
    capacity: 100,
    description: 'Introduction to Machine Learning Workshop',
    startTime: '2025-12-20T14:00:00',
    endTime: '2025-12-20T17:00:00',
    costs: [
      { type: 'student', cost: 0 },
      { type: 'general', cost: 10 },
    ],
    ticketsSold: 25,
    availableCapacity: 75,
  };

  it('should render event description', () => {
    const { getByText } = render(<EventCard event={mockEvent} />);
    expect(getByText('Introduction to Machine Learning Workshop')).toBeInTheDocument();
  });

  it('should render organizer name', () => {
    const { getByText } = render(<EventCard event={mockEvent} />);
    expect(getByText('Computer Science Club')).toBeInTheDocument();
  });

  it('should render campus name', () => {
    const { getByText } = render(<EventCard event={mockEvent} />);
    expect(getByText('Harvard University')).toBeInTheDocument();
  });

  it('should display Free when lowest price is 0', () => {
    const { getByText } = render(<EventCard event={mockEvent} />);
    expect(getByText('Free')).toBeInTheDocument();
  });

  it('should display price when not free', () => {
    const paidEvent = {
      ...mockEvent,
      costs: [{ type: 'student', cost: 15 }],
    };
    const { getByText } = render(<EventCard event={paidEvent} />);
    expect(getByText('$15.00')).toBeInTheDocument();
  });

  it('should display lowest price when multiple costs', () => {
    const multiCostEvent = {
      ...mockEvent,
      costs: [
        { type: 'student', cost: 10 },
        { type: 'general', cost: 25 },
      ],
    };
    const { getByText } = render(<EventCard event={multiCostEvent} />);
    expect(getByText('$10.00')).toBeInTheDocument();
  });

  it('should display tickets sold and capacity', () => {
    const { getByText } = render(<EventCard event={mockEvent} />);
    expect(getByText(/25 \/ 100 sold/)).toBeInTheDocument();
  });

  it('should display available capacity', () => {
    const { getByText } = render(<EventCard event={mockEvent} />);
    expect(getByText('75 spots remaining')).toBeInTheDocument();
  });

  it('should render with no costs as Free', () => {
    const freeEvent = {
      ...mockEvent,
      costs: [],
    };
    const { getByText } = render(<EventCard event={freeEvent} />);
    expect(getByText('Free')).toBeInTheDocument();
  });
});
