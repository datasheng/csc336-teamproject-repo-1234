import axios from 'axios';
import apiClient from './events';

export interface SignupRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  campusId: number;
  createOrganization?: boolean;
  organizationName?: string;
  organizationDescription?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  campusId: number;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
}

export const signup = async (request: SignupRequest): Promise<AuthResponse> => {
  try {
    const response = await apiClient.post<AuthResponse>('/auth/signup', request);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ErrorResponse;
    }
    throw new Error('Failed to sign up');
  }
};

export const login = async (request: LoginRequest): Promise<AuthResponse> => {
  try {
    const response = await apiClient.post<AuthResponse>('/auth/login', request);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ErrorResponse;
    }
    throw new Error('Failed to login');
  }
};
