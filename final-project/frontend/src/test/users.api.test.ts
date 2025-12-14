import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getCurrentUser, updateCurrentUser } from '../api/users';
import apiClient from '../api/events';

vi.mock('../api/events', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
  },
}));

describe('Users API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getCurrentUser', () => {
    it('should fetch current user successfully', async () => {
      const mockUser = {
        id: 1,
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        campusId: 1,
        campusName: 'Harvard University',
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockUser });

      const result = await getCurrentUser();

      expect(apiClient.get).toHaveBeenCalledWith('/users/me');
      expect(result).toEqual(mockUser);
    });

    it('should handle errors when fetching user', async () => {
      vi.mocked(apiClient.get).mockRejectedValue(new Error('Network error'));

      await expect(getCurrentUser()).rejects.toThrow('Network error');
    });
  });

  describe('updateCurrentUser', () => {
    it('should update user successfully', async () => {
      const updateData = {
        firstName: 'Jane',
        lastName: 'Smith',
        campusId: 2,
      };

      const mockUpdatedUser = {
        id: 1,
        email: 'test@example.com',
        firstName: 'Jane',
        lastName: 'Smith',
        campusId: 2,
        campusName: 'Stanford University',
      };

      vi.mocked(apiClient.put).mockResolvedValue({ data: mockUpdatedUser });

      const result = await updateCurrentUser(updateData);

      expect(apiClient.put).toHaveBeenCalledWith('/users/me', updateData);
      expect(result).toEqual(mockUpdatedUser);
    });

    it('should handle errors when updating user', async () => {
      const updateData = {
        firstName: 'Jane',
        lastName: 'Smith',
        campusId: 2,
      };

      vi.mocked(apiClient.put).mockRejectedValue(new Error('Update failed'));

      await expect(updateCurrentUser(updateData)).rejects.toThrow('Update failed');
    });

    it('should send correct update payload', async () => {
      const updateData = {
        firstName: 'Jane',
        lastName: 'Smith',
        campusId: 2,
      };

      vi.mocked(apiClient.put).mockResolvedValue({ data: {} });

      await updateCurrentUser(updateData);

      expect(apiClient.put).toHaveBeenCalledWith('/users/me', {
        firstName: 'Jane',
        lastName: 'Smith',
        campusId: 2,
      });
    });
  });
});
