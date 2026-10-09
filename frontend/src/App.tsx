import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { MarketLayout } from './layouts/MarketLayout';
import { SellerLayout } from './layouts/SellerLayout';
import { AdminLayout } from './layouts/AdminLayout';
import { HomePage } from './pages/HomePage';
import { ProductDetailPage } from './pages/ProductDetailPage';
import { CategoryPage } from './pages/CategoryPage';
import { CartPage } from './pages/CartPage';
import { CheckoutPage } from './pages/CheckoutPage';
import { OrderSuccessPage } from './pages/OrderSuccessPage';
import { MyOrdersPage } from './pages/MyOrdersPage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { RegisterSellerPage } from './pages/auth/RegisterSellerPage';
import { SellerDashboardPage } from './pages/seller/SellerDashboardPage';
import { SellerProductsPage } from './pages/seller/SellerProductsPage';
import { SellerOrdersPage } from './pages/seller/SellerOrdersPage';
import { SellerVouchersPage } from './pages/seller/SellerVouchersPage';
import { SellerReviewsPage } from './pages/seller/SellerReviewsPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminShopsPage } from './pages/admin/AdminShopsPage';
import { AdminVouchersPage } from './pages/admin/AdminVouchersPage';
import { useAuthStore } from './stores/useAuthStore';

export default function App() {
  const { fetchCurrentUser } = useAuthStore();

  useEffect(() => {
    fetchCurrentUser();
  }, [fetchCurrentUser]);

  return (
    <BrowserRouter>
      <Routes>
        {/* Auth Routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/register-seller" element={<RegisterSellerPage />} />

        {/* Public Marketplace Portal */}
        <Route path="/" element={<MarketLayout />}>
          <Route index element={<HomePage />} />
          <Route path="products/:id" element={<ProductDetailPage />} />
          <Route path="categories/:id" element={<CategoryPage />} />
          <Route path="cart" element={<CartPage />} />
          <Route path="checkout" element={<CheckoutPage />} />
          <Route path="orders/success" element={<OrderSuccessPage />} />
          <Route path="orders/my" element={<MyOrdersPage />} />
        </Route>

        {/* Seller Portal */}
        <Route path="/seller" element={<SellerLayout />}>
          <Route index element={<SellerDashboardPage />} />
          <Route path="products" element={<SellerProductsPage />} />
          <Route path="orders" element={<SellerOrdersPage />} />
          <Route path="vouchers" element={<SellerVouchersPage />} />
          <Route path="reviews" element={<SellerReviewsPage />} />
        </Route>

        {/* Admin Portal */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="shops" element={<AdminShopsPage />} />
          <Route path="vouchers" element={<AdminVouchersPage />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
