import axios from 'axios';
import apiClient from './events';

export interface PurchaseTicketRequest {
  eventId: number;
  type: string;
}

export interface TicketConfirmationDTO {
  eventId: number;
  userId: number;
  type: string;
  cost: number;
  eventDescription: string;
  message: string;
}

export interface UserTicketDTO {
  eventId: number;
  type: string;
  eventDescription: string;
  organizerName: string;
  cost: number;
  startTime: string;
  endTime: string;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
}

export const purchaseTicket = async (request: PurchaseTicketRequest): Promise<TicketConfirmationDTO> => {
  try {
    const response = await apiClient.post<TicketConfirmationDTO>('/tickets', request);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ErrorResponse;
    }
    throw new Error('Failed to purchase ticket');
  }
};

export const getUserTickets = async (): Promise<UserTicketDTO[]> => {
  const response = await apiClient.get<UserTicketDTO[]>('/users/me/tickets');
  return response.data;
};
