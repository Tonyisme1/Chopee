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
          <Route
            index
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Tổng Quan Gian Hàng</h2>
                <p className="text-gray-500 text-sm">Chào mừng đến với Kênh Người Bán Chopee.</p>
              </div>
            }
          />
          <Route
            path="products"
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Quản Lý Sản Phẩm</h2>
              </div>
            }
          />
          <Route
            path="orders"
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Quản Lý Đơn Hàng</h2>
              </div>
            }
          />
          <Route
            path="vouchers"
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Mã Giảm Giá Shop</h2>
              </div>
            }
          />
          <Route
            path="reviews"
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Đánh Giá Của Khách Hàng</h2>
              </div>
            }
          />
        </Route>

        {/* Admin Portal */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route
            index
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Tổng Quan Sàn Chopee</h2>
                <p className="text-gray-500 text-sm">Cổng quản trị dành cho Quản Trị Viên sàn.</p>
              </div>
            }
          />
          <Route
            path="shops"
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Duyệt & Quản Lý Gian Hàng</h2>
              </div>
            }
          />
          <Route
            path="vouchers"
            element={
              <div className="p-6 bg-white rounded-xl shadow-sm border border-gray-100">
                <h2 className="text-xl font-bold mb-2">Mã Khuyến Mãi Toàn Sàn</h2>
              </div>
            }
          />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
