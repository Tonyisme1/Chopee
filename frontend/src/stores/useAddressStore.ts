import { create } from 'zustand';
import { UserAddress } from '../types';
import { addressApi } from '../services/api';

interface AddressState {
  addresses: UserAddress[];
  defaultAddress: UserAddress | null;
  selectedAddress: UserAddress | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchAddresses: () => Promise<void>;
  addAddress: (data: {
    receiverName: string;
    phone: string;
    province: string;
    district: string;
    ward: string;
    detailAddress: string;
    isDefault?: boolean;
  }) => Promise<void>;
  setDefault: (id: number) => Promise<void>;
  deleteAddress: (id: number) => Promise<void>;
  selectAddress: (address: UserAddress) => void;
}

export const useAddressStore = create<AddressState>((set, get) => ({
  addresses: [],
  defaultAddress: null,
  selectedAddress: null,
  isLoading: false,
  error: null,

  fetchAddresses: async () => {
    const token = localStorage.getItem('chopee_token');
    if (!token) {
      set({ addresses: [], defaultAddress: null, selectedAddress: null });
      return;
    }

    set({ isLoading: true, error: null });
    try {
      const response = await addressApi.getAddresses();
      if (response.success && response.data) {
        const list = response.data;
        const defaultAddr = list.find((a) => a.isDefault) || (list.length > 0 ? list[0] : null);
        set({
          addresses: list,
          defaultAddress: defaultAddr,
          selectedAddress: get().selectedAddress || defaultAddr,
          isLoading: false,
        });
      }
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
    }
  },

  addAddress: async (data) => {
    set({ isLoading: true, error: null });
    try {
      await addressApi.createAddress(data);
      await get().fetchAddresses();
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
      throw err;
    }
  },

  setDefault: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await addressApi.setDefaultAddress(id);
      await get().fetchAddresses();
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
      throw err;
    }
  },

  deleteAddress: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await addressApi.deleteAddress(id);
      await get().fetchAddresses();
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
      throw err;
    }
  },

  selectAddress: (address: UserAddress) => set({ selectedAddress: address }),
}));

