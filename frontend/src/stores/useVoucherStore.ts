import { create } from 'zustand';
import { Voucher, ValidateVoucherResponse } from '../types';
import { voucherApi } from '../services/api';

interface VoucherState {
  platformVouchers: Voucher[];
  shopVouchers: Record<number, Voucher[]>;
  appliedPlatformVoucher: Voucher | null;
  appliedShopVouchers: Record<number, Voucher>;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchPlatformVouchers: () => Promise<void>;
  fetchShopVouchers: (shopId: number) => Promise<void>;
  validateVoucher: (
    code: string,
    orderAmount: number,
    shopId?: number
  ) => Promise<ValidateVoucherResponse>;
  applyPlatformVoucher: (voucher: Voucher | null) => void;
  applyShopVoucher: (shopId: number, voucher: Voucher | null) => void;
  clearAppliedVouchers: () => void;
}

export const useVoucherStore = create<VoucherState>((set) => ({
  platformVouchers: [],
  shopVouchers: {},
  appliedPlatformVoucher: null,
  appliedShopVouchers: {},
  isLoading: false,
  error: null,

  fetchPlatformVouchers: async () => {
    set({ isLoading: true, error: null });
    try {
      const response = await voucherApi.getPlatformVouchers();
      if (response.success && response.data) {
        set({ platformVouchers: response.data, isLoading: false });
      }
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
    }
  },

  fetchShopVouchers: async (shopId: number) => {
    try {
      const response = await voucherApi.getShopVouchers(shopId);
      if (response.success && response.data) {
        set((state) => ({
          shopVouchers: {
            ...state.shopVouchers,
            [shopId]: response.data,
          },
        }));
      }
    } catch (err: any) {
      console.error(`Failed to fetch vouchers for shop ${shopId}:`, err);
    }
  },

  validateVoucher: async (code, orderAmount, shopId) => {
    try {
      const response = await voucherApi.validateVoucher({ code, orderAmount, shopId });
      if (response.success && response.data) {
        return response.data;
      }
      throw new Error(response.message || 'Mã giảm giá không hợp lệ');
    } catch (err: any) {
      throw err;
    }
  },

  applyPlatformVoucher: (voucher) => set({ appliedPlatformVoucher: voucher }),

  applyShopVoucher: (shopId, voucher) =>
    set((state) => {
      const newMap = { ...state.appliedShopVouchers };
      if (voucher) {
        newMap[shopId] = voucher;
      } else {
        delete newMap[shopId];
      }
      return { appliedShopVouchers: newMap };
    }),

  clearAppliedVouchers: () =>
    set({
      appliedPlatformVoucher: null,
      appliedShopVouchers: {},
    }),
}));
