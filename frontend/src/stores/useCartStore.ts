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

function parseCartResponse(data: any): { flatItems: CartItem[]; totalItemCount: number; totalAmount: number } {
  const flatItems: CartItem[] = [];

  if (!data) {
    return { flatItems: [], totalItemCount: 0, totalAmount: 0 };
  }

  // 1. If backend returns grouped by shops: List<ShopCartGroupResponse>
  if (data.shops && Array.isArray(data.shops)) {
    data.shops.forEach((shop: any) => {
      if (shop.items && Array.isArray(shop.items)) {
        shop.items.forEach((item: any) => {
          flatItems.push({
            id: item.id,
            productId: item.productId,
            productName: item.productName,
            productSlug: item.productSlug,
            thumbnailUrl: item.thumbnailUrl,
            unit: item.unit || 'sản phẩm',
            stepQuantity: Number(item.stepQuantity) || 1,
            storageType: item.storageType || 'NORMAL',
            sellingPrice: Number(item.unitPrice ?? item.sellingPrice ?? 0),
            quantity: Number(item.quantity) || 1,
            subtotal: Number(item.itemSubtotal ?? item.subtotal ?? ((Number(item.unitPrice) || 0) * (Number(item.quantity) || 1))),
            variantId: item.variantId,
            variantName: item.variantName,
            shopId: shop.shopId ?? item.shopId ?? 0,
            shopName: shop.shopName ?? item.shopName ?? 'Gian hàng',
            shopSlug: shop.shopSlug ?? item.shopSlug,
          });
        });
      }
    });
  } else if (data.items && Array.isArray(data.items)) {
    // 2. Direct items fallback
    data.items.forEach((item: any) => {
      flatItems.push({
        id: item.id,
        productId: item.productId,
        productName: item.productName,
        productSlug: item.productSlug,
        thumbnailUrl: item.thumbnailUrl,
        unit: item.unit || 'sản phẩm',
        stepQuantity: Number(item.stepQuantity) || 1,
        storageType: item.storageType || 'NORMAL',
        sellingPrice: Number(item.unitPrice ?? item.sellingPrice ?? 0),
        quantity: Number(item.quantity) || 1,
        subtotal: Number(item.itemSubtotal ?? item.subtotal ?? ((Number(item.unitPrice) || 0) * (Number(item.quantity) || 1))),
        variantId: item.variantId,
        variantName: item.variantName,
        shopId: item.shopId ?? 0,
        shopName: item.shopName ?? 'Gian hàng',
        shopSlug: item.shopSlug,
      });
    });
  }

  const totalItemCount = typeof data.totalItemCount === 'number' ? data.totalItemCount : flatItems.length;
  const totalAmount = Number(data.grandTotal ?? data.totalAmount ?? flatItems.reduce((sum, item) => sum + item.subtotal, 0));

  return { flatItems, totalItemCount, totalAmount };
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
        const { flatItems, totalItemCount, totalAmount } = parseCartResponse(response.data);
        set({
          items: flatItems,
          totalItemCount,
          totalAmount,
          isLoading: false,
        });
      } else {
        set({ items: [], totalItemCount: 0, totalAmount: 0, isLoading: false });
      }
    } catch (err: any) {
      set({ isLoading: false, error: err.message });
    }
  },

  addToCart: async (productId, quantity, variantId) => {
    set({ isLoading: true, error: null });
    try {
      const response = await cartApi.addToCart({ productId, quantity, variantId });
      if (response.success) {
        await get().fetchCart();
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
      if (response.success) {
        await get().fetchCart();
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
      if (response.success) {
        await get().fetchCart();
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
