import apiClient from './events';

export interface Organization {
  id: number;
  name: string;
  description: string;
}

export interface Leader {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export const organizationsApi = {
  // Get all organizations the current user leads
  getMyOrganizations: async (): Promise<Organization[]> => {
    const response = await apiClient.get<Organization[]>('/users/me/organizations');
    return response.data;
  },

  // Get a single organization by ID
  getOrganization: async (id: string | number): Promise<Organization> => {
    const response = await apiClient.get<Organization>(`/organizations/${id}`);
    return response.data;
  },

  // Create a new organization
  createOrganization: async (data: { name: string; description: string }): Promise<Organization> => {
    const response = await apiClient.post<Organization>('/organizations', data);
    return response.data;
  },

  // Update an organization
  updateOrganization: async (id: string | number, data: { name: string; description: string }): Promise<Organization> => {
    const response = await apiClient.put<Organization>(`/organizations/${id}`, data);
    return response.data;
  },

  // Get leaders of an organization
  getLeaders: async (orgId: string | number): Promise<Leader[]> => {
    const response = await apiClient.get<Leader[]>(`/organizations/${orgId}/leaders`);
    return response.data;
  },

  // Add a leader to an organization
  addLeader: async (orgId: string | number, email: string): Promise<void> => {
    await apiClient.post(`/organizations/${orgId}/leaders`, { email });
  },

  // Remove a leader from an organization
  removeLeader: async (orgId: string | number, userId: number): Promise<void> => {
    await apiClient.delete(`/organizations/${orgId}/leaders/${userId}`);
  },
};