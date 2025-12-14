import axios from 'axios';
import apiClient from './events';

export interface CampusDTO {
  id: number;
  name: string;
  address: string;
  zipCode: string;
  city: string;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
}

export const getCampusesByCity = async (city: string): Promise<CampusDTO[]> => {
  try {
    const response = await apiClient.get<CampusDTO[]>('/campuses', {
      params: { city },
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ErrorResponse;
    }
    throw new Error('Failed to fetch campuses');
  }
};

export const getAllCampuses = async (): Promise<CampusDTO[]> => {
  try {
    const cities = ['Boston', 'Cambridge', 'New York', 'Los Angeles', 'Chicago'];
    const campusPromises = cities.map(city => getCampusesByCity(city).catch(() => []));
    const campusArrays = await Promise.all(campusPromises);
    return campusArrays.flat();
  } catch (error) {
    throw new Error('Failed to fetch campuses');
  }
};
