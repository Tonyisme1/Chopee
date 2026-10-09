import React, { useEffect } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import {
  ShieldAlert,
  BarChart3,
  Store,
  Ticket,
  ExternalLink,
  LogOut,
  ShieldCheck,
} from 'lucide-react';
import { useAuthStore } from '../stores/useAuthStore';

export const AdminLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, isAdmin, logout } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login?redirect=/admin');
    }
  }, [isAuthenticated, navigate]);

  if (!isAuthenticated) {
    return null;
  }

  if (!isAdmin()) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center">
          <div className="w-16 h-16 bg-red-100 text-red-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2">Truy Cập Bị Giới Hạn</h2>
          <p className="text-gray-600 text-sm mb-6">
            Khu vực này chỉ dành cho Quản Trị Viên sàn Chopee (ROLE_ADMIN). Bạn không có quyền truy cập.
          </p>
          <Link
            to="/"
            className="inline-block py-2.5 px-6 bg-chopee-orange hover:bg-orange-600 text-white font-semibold rounded-xl transition-colors"
          >
            Quay Về Trang Mua Sắm
          </Link>
        </div>
      </div>
    );
  }

  const navLinks = [
    { to: '/admin', label: 'Tổng Quan Toàn Sàn', icon: BarChart3, exact: true },
    { to: '/admin/shops', label: 'Duyệt & Quản Lý Shop', icon: Store },
    { to: '/admin/vouchers', label: 'Mã Khuyến Mãi Toàn Sàn', icon: Ticket },
  ];

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col font-sans">
      {/* Top Navigation */}
      <header className="bg-slate-900 text-white border-b border-slate-800 sticky top-0 z-40">
        <div className="px-6 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/admin" className="flex items-center gap-2">
              <div className="w-8 h-8 bg-chopee-orange rounded-lg flex items-center justify-center text-white font-bold">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <span className="font-extrabold text-lg text-white tracking-tight">Chopee Admin Console</span>
            </Link>
            <span className="text-slate-600">|</span>
            <span className="text-xs bg-slate-800 text-slate-300 px-2 py-0.5 rounded font-medium border border-slate-700">
              Quản Trị Hệ Thống
            </span>
          </div>

          <div className="flex items-center gap-4 text-sm">
            <Link
              to="/"
              className="flex items-center gap-1.5 text-slate-300 hover:text-white text-xs font-medium"
            >
              <ExternalLink className="w-3.5 h-3.5" /> Xem Sàn Chopee
            </Link>
            <span className="text-slate-600">|</span>
            <span className="font-semibold text-white text-xs">
              {user?.fullName || user?.username} (Admin)
            </span>
            <button
              onClick={() => {
                logout();
                navigate('/');
              }}
              className="text-red-400 hover:text-red-300 text-xs flex items-center gap-1"
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
                      ? 'bg-slate-100 text-slate-900 font-bold border-l-4 border-chopee-orange pl-2.5'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${isActive ? 'text-chopee-orange' : 'text-gray-400'}`} />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>

          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs text-slate-500">
            <p className="font-bold text-slate-700 mb-0.5">Bảo Mật Sàn Chopee</p>
            <p className="text-[11px] leading-relaxed">
              Mọi hành động phê duyệt gian hàng hoặc khóa shop đều được ghi nhận vào nhật ký hệ thống.
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
export default AdminLayout;
