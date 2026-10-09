// TypeScript Data Types matching Backend Spring Boot DTOs and Entities

export type Role = 'ROLE_BUYER' | 'ROLE_SELLER' | 'ROLE_ADMIN';
export type UserStatus = 'ACTIVE' | 'BLOCKED';
export type ShopType = 'GENERAL' | 'FOOD_FRESH' | 'OFFICIAL_MALL';
export type ShopStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'LOCKED';
export type StorageType = 'NORMAL' | 'FRESH' | 'FROZEN_CHILLED';
export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK';
export type ShippingMethod = 'STANDARD' | 'EXPRESS_FRESH';
export type PaymentMethod = 'COD' | 'VNPAY';
export type PaymentStatus = 'UNPAID' | 'PAID' | 'FAILED' | 'REFUNDED';
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED';
export type CancelledBy = 'BUYER' | 'SELLER' | 'ADMIN' | 'SYSTEM';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: Record<string, string>;
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  avatarUrl?: string;
  role: Role;
  status: UserStatus;
  shopId?: number;
}

export interface UserAddress {
  id: number;
  userId: number;
  receiverName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  detailAddress: string;
  isDefault: boolean;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface Shop {
  id: number;
  name: string;
  slug: string;
  description?: string;
  logoUrl?: string;
  bannerUrl?: string;
  phone: string;
  address: string;
  shopType: ShopType;
  status: ShopStatus;
  rating: number;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  iconUrl?: string;
  displayOrder: number;
  parentId?: number;
  subCategories?: Category[];
}

export interface ProductSummary {
  id: number;
  name: string;
  slug: string;
  thumbnailUrl?: string;
  originalPrice?: number;
  sellingPrice: number;
  discountPercent?: number;
  stockQuantity: number;
  soldQuantity: number;
  unit: string;
  hasVariants?: boolean;
  weightGrams?: number;
  stepQuantity: number;
  minOrderQuantity: number;
  storageType: StorageType;
  origin?: string;
  ratingAvg: number;
  reviewCount: number;
  shopId: number;
  shopName: string;
  shopSlug: string;
  shopAddress?: string;
  categoryId: number;
  categoryName: string;
}

export interface ProductImage {
  id: number;
  imageUrl: string;
  displayOrder: number;
}

export interface ProductVariant {
  id: number;
  variantName: string;
  price: number;
  stockQuantity: number;
}

export interface ProductDetail extends ProductSummary {
  shelfLife?: string;
  attributes?: string; // JSON String chứa Dynamic Specs
  shop: Shop;
  images: ProductImage[];
  variants: ProductVariant[];
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productSlug: string;
  thumbnailUrl?: string;
  unit: string;
  stepQuantity: number;
  storageType: StorageType;
  sellingPrice: number;
  quantity: number;
  subtotal: number;
  variantId?: number;
  variantName?: string;
  shopId: number;
  shopName: string;
  shopSlug?: string;
}

export interface CartResponse {
  items: CartItem[];
  totalItemCount: number;
  totalAmount: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  thumbnailUrl?: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  variantName?: string;
}

export interface Order {
  id: number;
  orderCode: string;
  groupOrderCode: string;
  shopId: number;
  shopName: string;
  totalAmount: number;
  shippingFee: number;
  discountAmount: number;
  shopDiscountAmount?: number;
  platformDiscountAmount?: number;
  finalAmount: number;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  shippingMethod: ShippingMethod;
  shippingName: string;
  shippingPhone: string;
  shippingAddress: string;
  note?: string;
  cancelledBy?: CancelledBy;
  cancellationReason?: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface OrderStatusHistory {
  id: number;
  orderId: number;
  previousStatus?: OrderStatus;
  newStatus: OrderStatus;
  changedBy?: string;
  note?: string;
  createdAt: string;
}

export interface Review {
  id: number;
  productId: number;
  productName?: string;
  variantId?: number;
  variantName?: string;
  userId: number;
  userFullName?: string;
  userAvatarUrl?: string;
  orderItemId?: number;
  rating: number;
  comment?: string;
  imagesJson?: string;
  shopReply?: string;
  createdAt: string;
}

export interface SubOrderPreview {
  shopId: number;
  shopName: string;
  items: CartItem[];
  shopSubtotal: number;
  shippingFee: number;
  shopDiscount: number;
  shopTotal: number;
}

export interface CheckoutPreviewResponse {
  groupSubtotal: number;
  totalShippingFee: number;
  totalDiscount: number;
  finalTotalAmount: number;
  subOrders: SubOrderPreview[];
}

export interface AIChatResponse {
  reply: string;
  intent: string;
  recommendedProducts: ProductSummary[];
  suggestedQuestions: string[];
}

export type DiscountType = 'PERCENT' | 'FIXED_AMOUNT';

export interface Voucher {
  id: number;
  code: string;
  name?: string;
  shopId?: number;
  shopName?: string;
  discountType: DiscountType;
  discountValue: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  usageLimit?: number;
  usedCount?: number;
  startDate?: string;
  endDate?: string;
  isValid?: boolean;
}

export interface ValidateVoucherResponse {
  code: string;
  isValid: boolean;
  message?: string;
  discountAmount: number;
  finalAmount: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
