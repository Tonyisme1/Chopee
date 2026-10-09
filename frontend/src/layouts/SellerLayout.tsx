import React, { useEffect } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Package,
  ShoppingBag,
  Ticket,
  Star,
  Store,
  ExternalLink,
  LogOut,
  AlertCircle,
} from 'lucide-react';
import { useAuthStore } from '../stores/useAuthStore';

export const SellerLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, isSeller, logout } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login?redirect=/seller');
    }
  }, [isAuthenticated, navigate]);

  if (!isAuthenticated) {
    return null;
  }

  if (!isSeller()) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center">
          <div className="w-16 h-16 bg-orange-100 text-chopee-orange rounded-full flex items-center justify-center mx-auto mb-4">
            <Store className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2">Chưa Đăng Ký Làm Người Bán</h2>
          <p className="text-gray-600 text-sm mb-6">
            Tài khoản của bạn hiện là Người Mua. Hãy đăng ký mở gian hàng để bán nông sản, đồ uống, thiết bị hoặc thời trang trên Chopee!
          </p>
          <div className="flex flex-col gap-3">
            <Link
              to="/register-seller"
              className="w-full py-2.5 px-4 bg-chopee-orange hover:bg-orange-600 text-white font-semibold rounded-xl transition-colors"
            >
              Đăng Ký Mở Gian Hàng Ngay
            </Link>
            <Link
              to="/"
              className="w-full py-2.5 px-4 bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium rounded-xl transition-colors"
            >
              Quay Về Trang Mua Sắm
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const navLinks = [
    { to: '/seller', label: 'Tổng Quan Gian Hàng', icon: LayoutDashboard, exact: true },
    { to: '/seller/products', label: 'Quản Lý Sản Phẩm', icon: Package },
    { to: '/seller/orders', label: 'Quản Lý Đơn Hàng', icon: ShoppingBag },
    { to: '/seller/vouchers', label: 'Mã Giảm Giá Shop', icon: Ticket },
    { to: '/seller/reviews', label: 'Đánh Giá Của Khách', icon: Star },
  ];

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col font-sans">
      {/* Top Navigation */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-40">
        <div className="px-6 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/seller" className="flex items-center gap-2">
              <div className="w-8 h-8 bg-chopee-orange rounded-lg flex items-center justify-center text-white font-bold">
                <Store className="w-5 h-5" />
              </div>
              <span className="font-extrabold text-lg text-gray-900 tracking-tight">Kênh Người Bán</span>
            </Link>
            <span className="text-gray-300">|</span>
            <span className="text-xs bg-orange-50 text-chopee-orange px-2 py-0.5 rounded font-medium border border-orange-200">
              Gian hàng: #{user?.shopId || 'ID'}
            </span>
          </div>

          <div className="flex items-center gap-4 text-sm">
            <Link
              to="/"
              className="flex items-center gap-1.5 text-gray-600 hover:text-chopee-orange text-xs font-medium"
            >
              <ExternalLink className="w-3.5 h-3.5" /> Xem Sàn Chopee
            </Link>
            <span className="text-gray-300">|</span>
            <span className="font-semibold text-gray-800 text-xs">
              {user?.fullName || user?.username}
            </span>
            <button
              onClick={() => {
                logout();
                navigate('/');
              }}
              className="text-red-600 hover:text-red-700 text-xs flex items-center gap-1"
              title="Đăng xuất"
            >
              <LogOut className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Body with Sidebar */}
      <div className="flex-1 flex">
        {/* Sidebar */}
        <aside className="w-64 bg-white border-r border-gray-200 p-4 flex flex-col justify-between">
          <nav className="space-y-1">
            {navLinks.map((item) => {
              const Icon = item.icon;
              const isActive = item.exact
                ? location.pathname === item.to
                : location.pathname.startsWith(item.to);

              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-orange-50 text-chopee-orange font-semibold'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${isActive ? 'text-chopee-orange' : 'text-gray-400'}`} />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>

          <div className="p-3 bg-gray-50 rounded-xl border border-gray-200/80 text-xs text-gray-500">
            <div className="flex items-center gap-1.5 text-gray-700 font-semibold mb-1">
              <AlertCircle className="w-4 h-4 text-chopee-orange" />
              <span>Hỗ trợ Người Bán</span>
            </div>
            <p className="text-[11px] leading-relaxed">
              Mọi đơn hàng từ nhiều shop sẽ được tách độc lập để bạn dễ dàng giao dịch và in vận đơn.
            </p>
          </div>
        </aside>

        {/* Content Outlet */}
        <main className="flex-1 p-6 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
export default SellerLayout;
