import axios from 'axios';

export interface CostDTO {
  type: string;
  cost: number;
}

export interface EventDTO {
  id: number;
  organizerId: number;
  organizerName: string;
  campusId: number;
  campusName: string;
  capacity: number;
  description: string;
  startTime: string;
  endTime: string;
  costs: CostDTO[];
  ticketsSold: number;
  availableCapacity: number;
}

export interface EventFilters {
  campusId?: number;
  organizerId?: number;
  startDate?: string;
  endDate?: string;
  freeOnly?: boolean;
  minPrice?: number;
  maxPrice?: number;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
}

const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getEvents = async (filters?: EventFilters): Promise<EventDTO[]> => {
  try {
    const params = new URLSearchParams();

    if (filters?.campusId) {
      params.append('campusId', filters.campusId.toString());
    }
    if (filters?.organizerId) {
      params.append('organizerId', filters.organizerId.toString());
    }
    if (filters?.startDate) {
      params.append('startDate', filters.startDate);
    }
    if (filters?.endDate) {
      params.append('endDate', filters.endDate);
    }
    if (filters?.freeOnly) {
      params.append('freeOnly', 'true');
    }
    if (filters?.minPrice !== undefined) {
      params.append('minPrice', filters.minPrice.toString());
    }
    if (filters?.maxPrice !== undefined) {
      params.append('maxPrice', filters.maxPrice.toString());
    }

    const response = await apiClient.get<EventDTO[]>('/events', {
      params: params.toString() ? Object.fromEntries(params) : undefined,
    });

    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ErrorResponse;
    }
    throw new Error('Failed to fetch events');
  }
};

export const getEventById = async (id: number): Promise<EventDTO> => {
  try {
    const response = await apiClient.get<EventDTO>(`/events/${id}`);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ErrorResponse;
    }
    throw new Error('Failed to fetch event');
  }
};

export const setAuthToken = (token: string | null) => {
  if (token) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

export default apiClient;
