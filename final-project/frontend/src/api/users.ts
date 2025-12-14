import apiClient from './events';

export interface UserDTO {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  campusId: number;
  campusName?: string;
}

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  campusId: number;
}

export const getCurrentUser = async (): Promise<UserDTO> => {
  const response = await apiClient.get<UserDTO>('/users/me');
  return response.data;
};

export const updateCurrentUser = async (data: UpdateUserRequest): Promise<UserDTO> => {
  const response = await apiClient.put<UserDTO>('/users/me', data);
  return response.data;
};
