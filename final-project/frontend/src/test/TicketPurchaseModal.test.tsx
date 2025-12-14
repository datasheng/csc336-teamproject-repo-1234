import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, fireEvent, waitFor } from '@testing-library/react';
import { TicketPurchaseModal } from '../components/TicketPurchaseModal';
import * as ticketsApi from '../api/tickets';

vi.mock('../api/tickets');

describe('TicketPurchaseModal', () => {
  const mockOnClose = vi.fn();
  const mockCosts = [
    { type: 'student', cost: 0 },
    { type: 'general', cost: 10 },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should not render when isOpen is false', () => {
    const { container } = render(
      <TicketPurchaseModal
        isOpen={false}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );
    expect(container.firstChild).toBeNull();
  });

  it('should render ticket selection when isOpen is true', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );
    expect(getByText('Select Ticket Type')).toBeInTheDocument();
    expect(getByText('Test Event')).toBeInTheDocument();
  });

  it('should display available capacity', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );
    expect(getByText('50 tickets remaining')).toBeInTheDocument();
  });

  it('should display sold out message when capacity is 0', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={0}
      />
    );
    expect(getByText('Event Sold Out')).toBeInTheDocument();
  });

  it('should display all ticket types', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );
    expect(getByText('student')).toBeInTheDocument();
    expect(getByText('general')).toBeInTheDocument();
    expect(getByText('Free')).toBeInTheDocument();
    expect(getByText('$10.00')).toBeInTheDocument();
  });

  it('should show general admission for free events', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={[]}
        availableCapacity={50}
      />
    );
    expect(getByText('General Admission')).toBeInTheDocument();
    expect(getByText('Free event')).toBeInTheDocument();
  });

  it('should navigate to payment step when ticket is selected', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    expect(getByText('Payment Information')).toBeInTheDocument();
    expect(getByText('Cardholder Name')).toBeInTheDocument();
    expect(getByText('Card Number')).toBeInTheDocument();
  });

  it('should validate payment form fields', () => {
    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const purchaseButton = getByText(/Purchase/);
    expect(purchaseButton).toBeDisabled();

    const cardNameInput = getByPlaceholderText('John Doe');
    const cardNumberInput = getByPlaceholderText('1234 5678 9012 3456');
    const expiryInput = getByPlaceholderText('MM/YY');
    const cvvInput = getByPlaceholderText('123');

    fireEvent.change(cardNameInput, { target: { value: 'John Doe' } });
    fireEvent.change(cardNumberInput, { target: { value: '1234567890123456' } });
    fireEvent.change(expiryInput, { target: { value: '12/25' } });
    fireEvent.change(cvvInput, { target: { value: '123' } });

    expect(purchaseButton).not.toBeDisabled();
  });

  it('should format card number with spaces', () => {
    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const cardNumberInput = getByPlaceholderText('1234 5678 9012 3456') as HTMLInputElement;
    fireEvent.change(cardNumberInput, { target: { value: '1234567890123456' } });

    expect(cardNumberInput.value).toBe('1234 5678 9012 3456');
  });

  it('should format expiry date with slash', () => {
    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const expiryInput = getByPlaceholderText('MM/YY') as HTMLInputElement;
    fireEvent.change(expiryInput, { target: { value: '1225' } });

    expect(expiryInput.value).toBe('12/25');
  });

  it('should successfully purchase ticket', async () => {
    const mockConfirmation = {
      eventId: 1,
      userId: 1,
      type: 'student',
      cost: 0,
      eventDescription: 'Test Event',
      message: 'Ticket purchased successfully',
    };
    vi.spyOn(ticketsApi, 'purchaseTicket').mockResolvedValue(mockConfirmation);

    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const cardNameInput = getByPlaceholderText('John Doe');
    const cardNumberInput = getByPlaceholderText('1234 5678 9012 3456');
    const expiryInput = getByPlaceholderText('MM/YY');
    const cvvInput = getByPlaceholderText('123');

    fireEvent.change(cardNameInput, { target: { value: 'John Doe' } });
    fireEvent.change(cardNumberInput, { target: { value: '1234567890123456' } });
    fireEvent.change(expiryInput, { target: { value: '1225' } });
    fireEvent.change(cvvInput, { target: { value: '123' } });

    const purchaseButton = getByText(/Purchase/);
    fireEvent.click(purchaseButton);

    await waitFor(() => {
      expect(getByText('Purchase Successful!')).toBeInTheDocument();
      expect(getByText('Ticket purchased successfully')).toBeInTheDocument();
    });
  });

  it('should display error on purchase failure', async () => {
    const mockError = {
      message: 'Event is sold out',
    };
    vi.spyOn(ticketsApi, 'purchaseTicket').mockRejectedValue(mockError);

    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const cardNameInput = getByPlaceholderText('John Doe');
    const cardNumberInput = getByPlaceholderText('1234 5678 9012 3456');
    const expiryInput = getByPlaceholderText('MM/YY');
    const cvvInput = getByPlaceholderText('123');

    fireEvent.change(cardNameInput, { target: { value: 'John Doe' } });
    fireEvent.change(cardNumberInput, { target: { value: '1234567890123456' } });
    fireEvent.change(expiryInput, { target: { value: '1225' } });
    fireEvent.change(cvvInput, { target: { value: '123' } });

    const purchaseButton = getByText(/Purchase/);
    fireEvent.click(purchaseButton);

    await waitFor(() => {
      expect(getByText('Purchase Failed')).toBeInTheDocument();
      expect(getByText('Event is sold out')).toBeInTheDocument();
    });
  });

  it('should allow going back from payment to selection', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    expect(getByText('Payment Information')).toBeInTheDocument();

    const backButton = getByText('Back');
    fireEvent.click(backButton);

    expect(getByText('Select Ticket Type')).toBeInTheDocument();
  });

  it('should call onClose when clicking close button', () => {
    const { getByText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const closeButton = getByText('×');
    fireEvent.click(closeButton);

    expect(mockOnClose).toHaveBeenCalled();
  });

  it('should call onClose when clicking Done after successful purchase', async () => {
    const mockConfirmation = {
      eventId: 1,
      userId: 1,
      type: 'student',
      cost: 0,
      eventDescription: 'Test Event',
      message: 'Ticket purchased successfully',
    };
    vi.spyOn(ticketsApi, 'purchaseTicket').mockResolvedValue(mockConfirmation);

    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const cardNameInput = getByPlaceholderText('John Doe');
    const cardNumberInput = getByPlaceholderText('1234 5678 9012 3456');
    const expiryInput = getByPlaceholderText('MM/YY');
    const cvvInput = getByPlaceholderText('123');

    fireEvent.change(cardNameInput, { target: { value: 'John Doe' } });
    fireEvent.change(cardNumberInput, { target: { value: '1234567890123456' } });
    fireEvent.change(expiryInput, { target: { value: '1225' } });
    fireEvent.change(cvvInput, { target: { value: '123' } });

    const purchaseButton = getByText(/Purchase/);
    fireEvent.click(purchaseButton);

    await waitFor(() => {
      expect(getByText('Done')).toBeInTheDocument();
    });

    const doneButton = getByText('Done');
    fireEvent.click(doneButton);

    expect(mockOnClose).toHaveBeenCalled();
  });

  it('should limit card number to 16 digits', () => {
    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const cardNumberInput = getByPlaceholderText('1234 5678 9012 3456') as HTMLInputElement;
    fireEvent.change(cardNumberInput, { target: { value: '12345678901234567890' } });

    expect(cardNumberInput.value.replace(/\s/g, '').length).toBe(16);
  });

  it('should limit CVV to 3 digits', () => {
    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const studentTicket = getByText('student').closest('div[class*="cursor-pointer"]');
    fireEvent.click(studentTicket!);

    const cvvInput = getByPlaceholderText('123') as HTMLInputElement;
    fireEvent.change(cvvInput, { target: { value: '12345' } });

    expect(cvvInput.value.length).toBe(3);
  });

  it('should display confirmation details after successful purchase', async () => {
    const mockConfirmation = {
      eventId: 1,
      userId: 1,
      type: 'general',
      cost: 10,
      eventDescription: 'Test Event',
      message: 'Ticket purchased successfully',
    };
    vi.spyOn(ticketsApi, 'purchaseTicket').mockResolvedValue(mockConfirmation);

    const { getByText, getByPlaceholderText } = render(
      <TicketPurchaseModal
        isOpen={true}
        onClose={mockOnClose}
        eventId={1}
        eventDescription="Test Event"
        costs={mockCosts}
        availableCapacity={50}
      />
    );

    const generalTicket = getByText('general').closest('div[class*="cursor-pointer"]');
    fireEvent.click(generalTicket!);

    const cardNameInput = getByPlaceholderText('John Doe');
    const cardNumberInput = getByPlaceholderText('1234 5678 9012 3456');
    const expiryInput = getByPlaceholderText('MM/YY');
    const cvvInput = getByPlaceholderText('123');

    fireEvent.change(cardNameInput, { target: { value: 'John Doe' } });
    fireEvent.change(cardNumberInput, { target: { value: '1234567890123456' } });
    fireEvent.change(expiryInput, { target: { value: '1225' } });
    fireEvent.change(cvvInput, { target: { value: '123' } });

    const purchaseButton = getByText(/Purchase/);
    fireEvent.click(purchaseButton);

    await waitFor(() => {
      expect(getByText('Test Event')).toBeInTheDocument();
      expect(getByText('$10.00')).toBeInTheDocument();
    });
  });
});
