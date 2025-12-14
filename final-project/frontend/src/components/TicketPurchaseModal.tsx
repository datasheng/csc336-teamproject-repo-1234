import { useState } from 'react';
import { CostDTO } from '../api/events';
import { purchaseTicket, TicketConfirmationDTO, ErrorResponse } from '../api/tickets';

interface TicketPurchaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  eventId: number;
  eventDescription: string;
  costs: CostDTO[];
  availableCapacity: number;
}

type Step = 'select' | 'payment' | 'confirmation' | 'error';

interface PaymentFormData {
  cardNumber: string;
  cardName: string;
  expiryDate: string;
  cvv: string;
}

export const TicketPurchaseModal = ({
  isOpen,
  onClose,
  eventId,
  eventDescription,
  costs,
  availableCapacity,
}: TicketPurchaseModalProps) => {
  const [step, setStep] = useState<Step>('select');
  const [selectedType, setSelectedType] = useState<string>('');
  const [selectedCost, setSelectedCost] = useState<number>(0);
  const [paymentData, setPaymentData] = useState<PaymentFormData>({
    cardNumber: '',
    cardName: '',
    expiryDate: '',
    cvv: '',
  });
  const [loading, setLoading] = useState(false);
  const [confirmation, setConfirmation] = useState<TicketConfirmationDTO | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleClose = () => {
    setStep('select');
    setSelectedType('');
    setSelectedCost(0);
    setPaymentData({
      cardNumber: '',
      cardName: '',
      expiryDate: '',
      cvv: '',
    });
    setConfirmation(null);
    setError(null);
    onClose();
  };

  const handleTicketSelect = (type: string, cost: number) => {
    setSelectedType(type);
    setSelectedCost(cost);
    setStep('payment');
  };

  const handlePaymentChange = (field: keyof PaymentFormData, value: string) => {
    setPaymentData(prev => ({ ...prev, [field]: value }));
  };

  const formatCardNumber = (value: string) => {
    const cleaned = value.replace(/\D/g, '');
    const limited = cleaned.slice(0, 16);
    return limited.replace(/(\d{4})(?=\d)/g, '$1 ');
  };

  const formatExpiryDate = (value: string) => {
    const cleaned = value.replace(/\D/g, '');
    const limited = cleaned.slice(0, 4);
    if (limited.length >= 2) {
      return limited.slice(0, 2) + '/' + limited.slice(2);
    }
    return limited;
  };

  const validatePaymentForm = (): boolean => {
    const cardNumberDigits = paymentData.cardNumber.replace(/\s/g, '');
    if (cardNumberDigits.length !== 16) return false;
    if (!paymentData.cardName.trim()) return false;
    if (paymentData.expiryDate.length !== 5) return false;
    if (paymentData.cvv.length !== 3) return false;
    return true;
  };

  const handlePurchase = async () => {
    if (!validatePaymentForm()) {
      setError('Please fill in all payment fields correctly');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const result = await purchaseTicket({
        eventId,
        type: selectedType,
      });
      setConfirmation(result);
      setStep('confirmation');
    } catch (err) {
      const errorResponse = err as ErrorResponse;
      setError(errorResponse.message || 'Failed to purchase ticket');
      setStep('error');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {step === 'select' && (
          <div className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-stone-700">Select Ticket Type</h2>
              <button
                onClick={handleClose}
                className="text-stone-400 hover:text-stone-600 text-2xl font-bold"
              >
                ×
              </button>
            </div>

            <div className="mb-6">
              <p className="text-stone-600 mb-2">{eventDescription}</p>
              <p className="text-sm text-stone-500">
                {availableCapacity} {availableCapacity === 1 ? 'ticket' : 'tickets'} remaining
              </p>
            </div>

            {availableCapacity === 0 ? (
              <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
                <p className="text-red-800 font-semibold text-lg">Event Sold Out</p>
                <p className="text-red-600 text-sm mt-2">All tickets have been sold for this event</p>
              </div>
            ) : costs.length === 0 ? (
              <div className="space-y-3">
                <div
                  onClick={() => handleTicketSelect('General', 0)}
                  className="border-2 border-stone-200 rounded-lg p-6 cursor-pointer hover:border-orange-600 transition-colors"
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <div className="font-semibold text-stone-700 text-lg">General Admission</div>
                      <div className="text-sm text-stone-500">Free event</div>
                    </div>
                    <div className="text-2xl font-bold text-green-600">Free</div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="space-y-3">
                {costs.map((cost) => (
                  <div
                    key={cost.type}
                    onClick={() => handleTicketSelect(cost.type, cost.cost)}
                    className="border-2 border-stone-200 rounded-lg p-6 cursor-pointer hover:border-orange-600 transition-colors"
                  >
                    <div className="flex justify-between items-center">
                      <div>
                        <div className="font-semibold text-stone-700 text-lg capitalize">{cost.type}</div>
                        <div className="text-sm text-stone-500">Per ticket</div>
                      </div>
                      <div className="text-2xl font-bold text-orange-600">
                        {cost.cost === 0 ? 'Free' : `$${cost.cost.toFixed(2)}`}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {step === 'payment' && (
          <div className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-stone-700">Payment Information</h2>
              <button
                onClick={handleClose}
                className="text-stone-400 hover:text-stone-600 text-2xl font-bold"
              >
                ×
              </button>
            </div>

            <div className="bg-stone-50 border border-stone-200 rounded-lg p-4 mb-6">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm text-stone-500">Ticket Type</p>
                  <p className="font-semibold text-stone-700 capitalize">{selectedType}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-stone-500">Total</p>
                  <p className="font-bold text-orange-600 text-xl">
                    {selectedCost === 0 ? 'Free' : `$${selectedCost.toFixed(2)}`}
                  </p>
                </div>
              </div>
            </div>

            <div className="space-y-4 mb-6">
              <div>
                <label htmlFor="cardName" className="block text-sm font-medium text-stone-700 mb-1">
                  Cardholder Name
                </label>
                <input
                  type="text"
                  id="cardName"
                  value={paymentData.cardName}
                  onChange={(e) => handlePaymentChange('cardName', e.target.value)}
                  className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                  placeholder="John Doe"
                />
              </div>

              <div>
                <label htmlFor="cardNumber" className="block text-sm font-medium text-stone-700 mb-1">
                  Card Number
                </label>
                <input
                  type="text"
                  id="cardNumber"
                  value={paymentData.cardNumber}
                  onChange={(e) => handlePaymentChange('cardNumber', formatCardNumber(e.target.value))}
                  className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                  placeholder="1234 5678 9012 3456"
                  maxLength={19}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="expiryDate" className="block text-sm font-medium text-stone-700 mb-1">
                    Expiry Date
                  </label>
                  <input
                    type="text"
                    id="expiryDate"
                    value={paymentData.expiryDate}
                    onChange={(e) => handlePaymentChange('expiryDate', formatExpiryDate(e.target.value))}
                    className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                    placeholder="MM/YY"
                    maxLength={5}
                  />
                </div>

                <div>
                  <label htmlFor="cvv" className="block text-sm font-medium text-stone-700 mb-1">
                    CVV
                  </label>
                  <input
                    type="text"
                    id="cvv"
                    value={paymentData.cvv}
                    onChange={(e) => handlePaymentChange('cvv', e.target.value.replace(/\D/g, '').slice(0, 3))}
                    className="w-full px-4 py-2 border border-stone-200 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600"
                    placeholder="123"
                    maxLength={3}
                  />
                </div>
              </div>
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
                <p className="text-red-800 text-sm">{error}</p>
              </div>
            )}

            <div className="flex gap-3">
              <button
                onClick={() => setStep('select')}
                className="flex-1 bg-white text-stone-700 border border-stone-200 px-6 py-3 rounded-lg font-medium hover:bg-stone-50 transition-colors"
              >
                Back
              </button>
              <button
                onClick={handlePurchase}
                disabled={loading || !validatePaymentForm()}
                className="flex-1 bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Processing...' : `Purchase${selectedCost > 0 ? ` - $${selectedCost.toFixed(2)}` : ''}`}
              </button>
            </div>
          </div>
        )}

        {step === 'confirmation' && confirmation && (
          <div className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-green-600">Purchase Successful!</h2>
              <button
                onClick={handleClose}
                className="text-stone-400 hover:text-stone-600 text-2xl font-bold"
              >
                ×
              </button>
            </div>

            <div className="bg-green-50 border border-green-200 rounded-lg p-6 mb-6">
              <div className="text-center mb-4">
                <p className="text-green-800 font-semibold text-lg">{confirmation.message}</p>
              </div>

              <div className="space-y-3 border-t border-green-200 pt-4">
                <div className="flex justify-between">
                  <span className="text-stone-600">Event:</span>
                  <span className="font-semibold text-stone-700">{confirmation.eventDescription}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-stone-600">Ticket Type:</span>
                  <span className="font-semibold text-stone-700 capitalize">{confirmation.type}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-stone-600">Amount Paid:</span>
                  <span className="font-semibold text-orange-600">
                    {confirmation.cost === 0 ? 'Free' : `$${confirmation.cost.toFixed(2)}`}
                  </span>
                </div>
              </div>
            </div>

            <button
              onClick={handleClose}
              className="w-full bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors"
            >
              Done
            </button>
          </div>
        )}

        {step === 'error' && (
          <div className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-red-600">Purchase Failed</h2>
              <button
                onClick={handleClose}
                className="text-stone-400 hover:text-stone-600 text-2xl font-bold"
              >
                ×
              </button>
            </div>

            <div className="bg-red-50 border border-red-200 rounded-lg p-6 mb-6">
              <div className="text-center mb-4">
                <p className="text-red-800 font-semibold text-lg">Unable to Complete Purchase</p>
                <p className="text-red-600 text-sm mt-2">{error}</p>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleClose}
                className="flex-1 bg-white text-stone-700 border border-stone-200 px-6 py-3 rounded-lg font-medium hover:bg-stone-50 transition-colors"
              >
                Close
              </button>
              <button
                onClick={() => setStep('select')}
                className="flex-1 bg-orange-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-orange-700 transition-colors"
              >
                Try Again
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
