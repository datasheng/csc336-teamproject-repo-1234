import apiClient from './events';

export interface ProfitByEvent {
  eventId: number;
  eventDescription: string;
  organizerName: string;
  ticketsSold: number;
  revenue: number;
  profit: number;
}

export interface ProfitByDate {
  date: string;
  ticketsSold: number;
  revenue: number;
  profit: number;
}

export interface ProfitReport {
  totalRevenue: number;
  totalProfit: number;
  totalTicketsSold: number;
  profitByEvent: ProfitByEvent[];
  profitByDate: ProfitByDate[];
  lastUpdated: string;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
}

export const getProfitReport = async (): Promise<ProfitReport> => {
  try {
    const response = await apiClient.get<ProfitReport>('/admin/profit');
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data as ErrorResponse;
    }
    throw new Error('Failed to fetch profit report');
  }
};

