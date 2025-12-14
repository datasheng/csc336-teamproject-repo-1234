import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ProfilePage } from '../pages/ProfilePage';
import { AuthProvider } from '../context/AuthContext';
import * as usersApi from '../api/users';
import * as campusesApi from '../api/campuses';

vi.mock('../api/users');
vi.mock('../api/campuses');

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{component}</AuthProvider>
    </BrowserRouter>
  );
};

describe('ProfilePage', () => {
  const mockUser = {
    id: 1,
    email: 'test@example.com',
    firstName: 'John',
    lastName: 'Doe',
    campusId: 1,
    campusName: 'Harvard University',
  };

  const mockCampuses = [
    { id: 1, name: 'Harvard University', city: 'Cambridge' },
    { id: 2, name: 'Stanford University', city: 'Stanford' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('user', JSON.stringify({
      id: 1,
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      campusId: 1,
    }));
  });

  it('should display loading state initially', () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockImplementation(() => new Promise(() => {}));
    vi.spyOn(campusesApi, 'getAllCampuses').mockImplementation(() => new Promise(() => {}));

    const { getByText } = renderWithProviders(<ProfilePage />);
    expect(getByText('Loading profile...')).toBeInTheDocument();
  });

  it('should display user profile after loading', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByDisplayValue, getByText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('My Profile')).toBeInTheDocument();
      expect(getByDisplayValue('test@example.com')).toBeInTheDocument();
      expect(getByDisplayValue('John')).toBeInTheDocument();
      expect(getByDisplayValue('Doe')).toBeInTheDocument();
    });
  });

  it('should display error message on fetch failure', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockRejectedValue(new Error('Network Error'));
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Failed to load profile. Please try again later.')).toBeInTheDocument();
    });
  });

  it('should show edit form when clicking Edit Profile', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });

    const editButton = getByText('Edit Profile');
    fireEvent.click(editButton);

    expect(getByText('Save Changes')).toBeInTheDocument();
    expect(getByText('Cancel')).toBeInTheDocument();
  });

  it('should allow editing first name', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByText, getByLabelText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });

    fireEvent.click(getByText('Edit Profile'));

    const firstNameInput = getByLabelText('First Name') as HTMLInputElement;
    fireEvent.change(firstNameInput, { target: { value: 'Jane' } });

    expect(firstNameInput.value).toBe('Jane');
  });

  it('should successfully save profile changes', async () => {
    const updatedUser = { ...mockUser, firstName: 'Jane' };
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);
    vi.spyOn(usersApi, 'updateCurrentUser').mockResolvedValue(updatedUser);

    const { getByText, getByLabelText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });

    fireEvent.click(getByText('Edit Profile'));

    const firstNameInput = getByLabelText('First Name');
    fireEvent.change(firstNameInput, { target: { value: 'Jane' } });

    const saveButton = getByText('Save Changes');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(getByText('Profile updated successfully!')).toBeInTheDocument();
    });
  });

  it('should cancel editing and restore original values', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByText, getByLabelText, getByDisplayValue } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });

    fireEvent.click(getByText('Edit Profile'));

    const firstNameInput = getByLabelText('First Name');
    fireEvent.change(firstNameInput, { target: { value: 'Jane' } });

    const cancelButton = getByText('Cancel');
    fireEvent.click(cancelButton);

    await waitFor(() => {
      expect(getByDisplayValue('John')).toBeInTheDocument();
    });
  });

  it('should disable email field', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByDisplayValue } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      const emailInput = getByDisplayValue('test@example.com') as HTMLInputElement;
      expect(emailInput.disabled).toBe(true);
    });
  });

  it('should display campus dropdown when editing', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByText, getByLabelText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });

    fireEvent.click(getByText('Edit Profile'));

    const campusSelect = getByLabelText('Campus') as HTMLSelectElement;
    expect(campusSelect.tagName).toBe('SELECT');
  });

  it('should disable save button when fields are invalid', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByText, getByLabelText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });

    fireEvent.click(getByText('Edit Profile'));

    const firstNameInput = getByLabelText('First Name');
    fireEvent.change(firstNameInput, { target: { value: '' } });

    const saveButton = getByText('Save Changes') as HTMLButtonElement;
    expect(saveButton.disabled).toBe(true);
  });

  it('should display error on save failure', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);
    vi.spyOn(usersApi, 'updateCurrentUser').mockRejectedValue(new Error('Update failed'));

    const { getByText, getByLabelText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });

    fireEvent.click(getByText('Edit Profile'));

    const firstNameInput = getByLabelText('First Name');
    fireEvent.change(firstNameInput, { target: { value: 'Jane' } });

    const saveButton = getByText('Save Changes');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(getByText('Failed to update profile. Please try again.')).toBeInTheDocument();
    });
  });

  it('should show Edit Profile button when not editing', async () => {
    vi.spyOn(usersApi, 'getCurrentUser').mockResolvedValue(mockUser);
    vi.spyOn(campusesApi, 'getAllCampuses').mockResolvedValue(mockCampuses);

    const { getByText } = renderWithProviders(<ProfilePage />);

    await waitFor(() => {
      expect(getByText('Edit Profile')).toBeInTheDocument();
    });
  });
});
