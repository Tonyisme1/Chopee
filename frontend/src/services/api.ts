import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import {
  ApiResponse,
  AuthResponse,
  Category,
  ProductSummary,
  ProductDetail,
  CartResponse,
  CheckoutPreviewResponse,
  Order,
  UserAddress,
  Voucher,
  ValidateVoucherResponse,
  Review,
  PageResponse,
  AIChatResponse,
  User
} from '../types';

// Axios Instance configured for Vite Proxy -> Spring Boot Backend
const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request Interceptor: Attach JWT Token from localStorage
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('chopee_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response Interceptor: Handle Global 401 & Extract Data
api.interceptors.response.use(
  (response) => response.data,
  (error: AxiosError<{ message?: string; errors?: Record<string, string> }>) => {
    if (error.response?.status === 401) {
      // Clear token if expired or unauthorized
      localStorage.removeItem('chopee_token');
      localStorage.removeItem('chopee_user');
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }
    const errorMessage =
      error.response?.data?.message || error.message || 'Có lỗi xảy ra, vui lòng thử lại sau';
    return Promise.reject(new Error(errorMessage));
  }
);

export default api;

// ==========================================
// API CLIENT MODULES
// ==========================================

export const authApi = {
  login: (data: { emailOrUsername: string; password: string }): Promise<ApiResponse<AuthResponse>> =>
    api.post('/auth/login', data),

  register: (data: {
    username: string;
    email: string;
    password: string;
    fullName: string;
    phone?: string;
  }): Promise<ApiResponse<AuthResponse>> => api.post('/auth/register', data),

  registerSeller: (data: {
    shopName: string;
    description?: string;
    phone: string;
    address: string;
    shopType: string;
  }): Promise<ApiResponse<AuthResponse>> => api.post('/auth/register-seller', data),

  getMe: (): Promise<ApiResponse<User>> => api.get('/auth/me'),
};

export const catalogApi = {
  getCategories: (): Promise<ApiResponse<Category[]>> =>
    api.get('/public/categories'),

  getProducts: (params?: {
    keyword?: string;
    categoryId?: number;
    minPrice?: number;
    maxPrice?: number;
    storageType?: string;
    rating?: number;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<ApiResponse<PageResponse<ProductSummary>>> =>
    api.get('/public/products', { params }),

  getProductById: (id: number): Promise<ApiResponse<ProductDetail>> =>
    api.get(`/public/products/${id}`),

  getShopById: (id: number): Promise<ApiResponse<any>> =>
    api.get(`/public/shops/${id}`),
};

export const cartApi = {
  getCart: (): Promise<ApiResponse<CartResponse>> =>
    api.get('/buyer/cart'),

  addToCart: (data: {
    productId: number;
    quantity: number;
    variantId?: number;
  }): Promise<ApiResponse<CartResponse>> =>
    api.post('/buyer/cart/items', data),

  updateCartItem: (
    itemId: number,
    data: { quantity: number }
  ): Promise<ApiResponse<CartResponse>> =>
    api.put(`/buyer/cart/items/${itemId}`, data),

  removeCartItem: (itemId: number): Promise<ApiResponse<CartResponse>> =>
    api.delete(`/buyer/cart/items/${itemId}`),

  clearCart: (): Promise<ApiResponse<void>> =>
    api.delete('/buyer/cart/clear'),
};

export const orderApi = {
  checkoutPreview: (data: {
    items: { productId: number; quantity: number; variantId?: number }[];
    shippingMethod?: string;
    shopVoucherCodes?: Record<number, string>;
    platformVoucherCode?: string;
  }): Promise<ApiResponse<CheckoutPreviewResponse>> =>
    api.post('/buyer/orders/checkout-preview', data),

  createOrder: (data: {
    shippingName: string;
    shippingPhone: string;
    shippingAddress: string;
    paymentMethod: string;
    shippingMethod: string;
    note?: string;
    shopVoucherCodes?: Record<number, string>;
    platformVoucherCode?: string;
  }): Promise<ApiResponse<{ groupOrderCode: string; orders: Order[] }>> =>
    api.post('/buyer/orders', data),

  getOrders: (params?: { status?: string; page?: number; size?: number }): Promise<ApiResponse<PageResponse<Order>>> =>
    api.get('/buyer/orders', { params }),

  getOrderByCode: (orderCode: string): Promise<ApiResponse<Order>> =>
    api.get(`/buyer/orders/${orderCode}`),

  cancelOrder: (orderCode: string, reason?: string): Promise<ApiResponse<Order>> =>
    api.put(`/buyer/orders/${orderCode}/cancel`, { reason }),
};

export const addressApi = {
  getAddresses: (): Promise<ApiResponse<UserAddress[]>> =>
    api.get('/buyer/addresses'),

  createAddress: (data: {
    receiverName: string;
    phone: string;
    province: string;
    district: string;
    ward: string;
    detailAddress: string;
    isDefault?: boolean;
  }): Promise<ApiResponse<UserAddress>> =>
    api.post('/buyer/addresses', data),

  setDefaultAddress: (id: number): Promise<ApiResponse<UserAddress>> =>
    api.put(`/buyer/addresses/${id}/default`),

  deleteAddress: (id: number): Promise<ApiResponse<void>> =>
    api.delete(`/buyer/addresses/${id}`),
};

export const voucherApi = {
  getPlatformVouchers: (): Promise<ApiResponse<Voucher[]>> =>
    api.get('/public/vouchers'),

  getShopVouchers: (shopId: number): Promise<ApiResponse<Voucher[]>> =>
    api.get(`/public/shops/${shopId}/vouchers`),

  validateVoucher: (params: {
    code: string;
    shopId?: number;
    orderAmount: number;
  }): Promise<ApiResponse<ValidateVoucherResponse>> =>
    api.get('/public/vouchers/validate', { params }),

  getSellerVouchers: (): Promise<ApiResponse<Voucher[]>> =>
    api.get('/seller/vouchers'),

  createSellerVoucher: (data: any): Promise<ApiResponse<Voucher>> =>
    api.post('/seller/vouchers', data),

  createAdminVoucher: (data: any): Promise<ApiResponse<Voucher>> =>
    api.post('/admin/vouchers', data),
};

export const reviewApi = {
  getProductReviews: (
    productId: number,
    params?: { page?: number; size?: number }
  ): Promise<ApiResponse<PageResponse<Review>>> =>
    api.get(`/public/products/${productId}/reviews`, { params }),

  createReview: (data: {
    productId: number;
    orderItemId: number;
    rating: number;
    comment?: string;
    imagesJson?: string;
  }): Promise<ApiResponse<Review>> =>
    api.post('/buyer/reviews', data),

  getSellerReviews: (params?: { page?: number; size?: number }): Promise<ApiResponse<PageResponse<Review>>> =>
    api.get('/seller/reviews', { params }),

  replyReview: (
    reviewId: number,
    data: { reply: string }
  ): Promise<ApiResponse<Review>> =>
    api.put(`/seller/reviews/${reviewId}/reply`, data),
};

export const paymentApi = {
  createVNPayPayment: (data: {
    groupOrderCode: string;
    amount: number;
    orderInfo?: string;
  }): Promise<ApiResponse<{ paymentUrl: string }>> =>
    api.post('/payment/vnpay/create-payment', data),
};

export const sellerApi = {
  getDashboard: (): Promise<ApiResponse<any>> =>
    api.get('/seller/dashboard'),

  getProducts: (params?: { page?: number; size?: number }): Promise<ApiResponse<PageResponse<ProductSummary>>> =>
    api.get('/seller/products', { params }),

  createProduct: (data: any): Promise<ApiResponse<ProductDetail>> =>
    api.post('/seller/products', data),

  updateProduct: (id: number, data: any): Promise<ApiResponse<ProductDetail>> =>
    api.put(`/seller/products/${id}`, data),

  deleteProduct: (id: number): Promise<ApiResponse<void>> =>
    api.delete(`/seller/products/${id}`),

  getOrders: (params?: { status?: string; page?: number; size?: number }): Promise<ApiResponse<PageResponse<Order>>> =>
    api.get('/seller/orders', { params }),

  updateOrderStatus: (
    orderId: number,
    data: { status: string; note?: string }
  ): Promise<ApiResponse<Order>> =>
    api.put(`/seller/orders/${orderId}/status`, data),
};

export const adminApi = {
  getDashboard: (): Promise<ApiResponse<any>> =>
    api.get('/admin/dashboard'),

  getShops: (params?: { status?: string; page?: number; size?: number }): Promise<ApiResponse<PageResponse<any>>> =>
    api.get('/admin/shops', { params }),

  updateShopStatus: (
    shopId: number,
    data: { status: string; reason?: string }
  ): Promise<ApiResponse<any>> =>
    api.put(`/admin/shops/${shopId}/status`, data),
};

export const aiApi = {
  chat: (data: { message: string; history?: any[] }): Promise<ApiResponse<AIChatResponse>> =>
    api.post('/ai/chat', data),
};
