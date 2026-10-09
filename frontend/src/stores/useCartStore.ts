import { create } from 'zustand';
import { CartItem } from '../types';
import { cartApi } from '../services/api';

export interface ShopCartGroup {
  shopId: number;
  shopName: string;
  items: CartItem[];
  subtotal: number;
}

interface CartState {
  items: CartItem[];
  totalItemCount: number;
  totalAmount: number;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchCart: () => Promise<void>;
  addToCart: (productId: number, quantity: number, variantId?: number) => Promise<void>;
  updateQuantity: (itemId: number, quantity: number) => Promise<void>;
  removeItem: (itemId: number) => Promise<void>;
  clearCart: () => Promise<void>;
  getItemsByShop: () => ShopCartGroup[];
}

export const useCartStore = create<CartState>((set, get) => ({
  items: [],
  totalItemCount: 0,
  totalAmount: 0,
  isLoading: false,
  error: null,

  fetchCart: async () => {
    // Only fetch if token is present
    const token = localStorage.getItem('chopee_token');
    if (!token) {
      set({ items: [], totalItemCount: 0, totalAmount: 0 });
      return;
    }

    set({ isLoading: true, error: null });
    try {
      const response = await cartApi.getCart();
      if (response.success && response.data) {
        const { items, totalItemCount, totalAmount } = response.data;
        set({
          items: items || [],
          totalItemCount: totalItemCount || 0,
          totalAmount: totalAmount || 0,
          isLoading: false,
        });
      }
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
    }
  },

  addToCart: async (productId, quantity, variantId) => {
    set({ isLoading: true, error: null });
    try {
      const response = await cartApi.addToCart({ productId, quantity, variantId });
      if (response.success && response.data) {
        const { items, totalItemCount, totalAmount } = response.data;
        set({
          items: items || [],
          totalItemCount: totalItemCount || 0,
          totalAmount: totalAmount || 0,
          isLoading: false,
        });
      }
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
      throw err;
    }
  },

  updateQuantity: async (itemId, quantity) => {
    if (quantity <= 0) {
      await get().removeItem(itemId);
      return;
    }
    set({ isLoading: true, error: null });
    try {
      const response = await cartApi.updateCartItem(itemId, { quantity });
      if (response.success && response.data) {
        const { items, totalItemCount, totalAmount } = response.data;
        set({
          items: items || [],
          totalItemCount: totalItemCount || 0,
          totalAmount: totalAmount || 0,
          isLoading: false,
        });
      }
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
      throw err;
    }
  },

  removeItem: async (itemId) => {
    set({ isLoading: true, error: null });
    try {
      const response = await cartApi.removeCartItem(itemId);
      if (response.success && response.data) {
        const { items, totalItemCount, totalAmount } = response.data;
        set({
          items: items || [],
          totalItemCount: totalItemCount || 0,
          totalAmount: totalAmount || 0,
          isLoading: false,
        });
      }
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
      throw err;
    }
  },

  clearCart: async () => {
    set({ isLoading: true, error: null });
    try {
      await cartApi.clearCart();
      set({ items: [], totalItemCount: 0, totalAmount: 0, isLoading: false });
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
    }
  },

  getItemsByShop: () => {
    const { items } = get();
    const groupMap = new Map<number, ShopCartGroup>();

    items.forEach((item) => {
      if (!groupMap.has(item.shopId)) {
        groupMap.set(item.shopId, {
          shopId: item.shopId,
          shopName: item.shopName,
          items: [],
          subtotal: 0,
        });
      }
      const group = groupMap.get(item.shopId)!;
      group.items.push(item);
      group.subtotal += item.subtotal;
    });

    return Array.from(groupMap.values());
  },
}));
