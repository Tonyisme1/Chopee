import { create } from 'zustand';
import { User, AuthResponse } from '../types';
import { authApi } from '../services/api';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (emailOrUsername: string, password: string) => Promise<AuthResponse>;
  register: (data: {
    username: string;
    email: string;
    password: string;
    fullName: string;
    phone?: string;
  }) => Promise<AuthResponse>;
  registerSeller: (data: {
    shopName: string;
    description?: string;
    phone: string;
    address: string;
    shopType: string;
  }) => Promise<AuthResponse>;
  logout: () => void;
  fetchCurrentUser: () => Promise<void>;
  clearError: () => void;
  isSeller: () => boolean;
  isAdmin: () => boolean;
}

const getStoredToken = (): string | null => localStorage.getItem('chopee_token');
const getStoredUser = (): User | null => {
  try {
    const raw = localStorage.getItem('chopee_user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const useAuthStore = create<AuthState>((set, get) => {
  const initialToken = getStoredToken();
  const initialUser = getStoredUser();

  return {
    user: initialUser,
    token: initialToken,
    isAuthenticated: !!initialToken && !!initialUser,
    isLoading: false,
    error: null,

    login: async (emailOrUsername, password) => {
      set({ isLoading: true, error: null });
      try {
        const response = await authApi.login({ emailOrUsername, password });
        if (response.success && response.data) {
          const { token, user } = response.data;
          localStorage.setItem('chopee_token', token);
          localStorage.setItem('chopee_user', JSON.stringify(user));
          set({
            token,
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          return response.data;
        } else {
          throw new Error(response.message || 'Đăng nhập không thành công');
        }
      } catch (err: any) {
        const message = err.message || 'Sai tên đăng nhập hoặc mật khẩu';
        set({ error: message, isLoading: false });
        throw err;
      }
    },

    register: async (data) => {
      set({ isLoading: true, error: null });
      try {
        const response = await authApi.register(data);
        if (response.success && response.data) {
          const { token, user } = response.data;
          localStorage.setItem('chopee_token', token);
          localStorage.setItem('chopee_user', JSON.stringify(user));
          set({
            token,
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          return response.data;
        } else {
          throw new Error(response.message || 'Đăng ký không thành công');
        }
      } catch (err: any) {
        const message = err.message || 'Đăng ký thất bại';
        set({ error: message, isLoading: false });
        throw err;
      }
    },

    registerSeller: async (data) => {
      set({ isLoading: true, error: null });
      try {
        const response = await authApi.registerSeller(data);
        if (response.success && response.data) {
          const { token, user } = response.data;
          localStorage.setItem('chopee_token', token);
          localStorage.setItem('chopee_user', JSON.stringify(user));
          set({
            token,
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          return response.data;
        } else {
          throw new Error(response.message || 'Đăng ký gian hàng không thành công');
        }
      } catch (err: any) {
        const message = err.message || 'Đăng ký gian hàng thất bại';
        set({ error: message, isLoading: false });
        throw err;
      }
    },

    logout: () => {
      localStorage.removeItem('chopee_token');
      localStorage.removeItem('chopee_user');
      set({
        token: null,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
    },

    fetchCurrentUser: async () => {
      const token = get().token;
      if (!token) return;
      try {
        const response = await authApi.getMe();
        if (response.success && response.data) {
          const user = response.data;
          localStorage.setItem('chopee_user', JSON.stringify(user));
          set({ user, isAuthenticated: true });
        }
      } catch {
        // Token expired or invalid
        get().logout();
      }
    },

    clearError: () => set({ error: null }),

    isSeller: () => {
      const user = get().user;
      return user?.role === 'ROLE_SELLER';
    },

    isAdmin: () => {
      const user = get().user;
      return user?.role === 'ROLE_ADMIN';
    },
  };
});

