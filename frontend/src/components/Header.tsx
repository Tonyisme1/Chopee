import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  ShoppingBag,
  ShoppingCart,
  Search,
  Store,
  Shield,
  LogOut,
  Package,
  ChevronDown,
} from 'lucide-react';
import { useAuthStore } from '../stores/useAuthStore';
import { useCartStore } from '../stores/useCartStore';

export const Header: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout, isSeller, isAdmin } = useAuthStore();
  const { totalItemCount, fetchCart } = useCartStore();
  const [keyword, setKeyword] = useState('');
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
    }
  }, [isAuthenticated, fetchCart]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      navigate(`/?keyword=${encodeURIComponent(keyword.trim())}`);
    } else {
      navigate('/');
    }
  };

  const handleLogout = () => {
    logout();
    setIsUserMenuOpen(false);
    navigate('/');
  };

  return (
    <header className="bg-chopee-orange text-white shadow-md sticky top-0 z-50">
      {/* Top Utility Bar */}
      <div className="border-b border-orange-400/40 text-xs py-1.5 px-4">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center space-x-4">
            <Link
              to="/seller"
              className="hover:text-orange-200 transition-colors flex items-center gap-1 font-medium"
            >
              <Store className="w-3.5 h-3.5" /> Kênh Người Bán
            </Link>
            <span className="text-orange-300">|</span>
            <Link
              to="/admin"
              className="hover:text-orange-200 transition-colors flex items-center gap-1 font-medium"
            >
              <Shield className="w-3.5 h-3.5" /> Quản Trị Sàn
            </Link>
          </div>

          <div className="flex items-center space-x-4">
            {isAuthenticated && user ? (
              <div className="relative">
                <button
                  onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                  className="flex items-center gap-1.5 hover:text-orange-200 focus:outline-none font-medium"
                >
                  <div className="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center text-white text-[10px] font-bold">
                    {user.fullName ? user.fullName[0].toUpperCase() : 'U'}
                  </div>
                  <span>{user.fullName || user.username}</span>
                  <ChevronDown className="w-3.5 h-3.5" />
                </button>

                {isUserMenuOpen && (
                  <div
                    className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-xl py-1 text-gray-800 text-sm border border-gray-100 z-50 animate-in fade-in"
                    onMouseLeave={() => setIsUserMenuOpen(false)}
                  >
                    <div className="px-4 py-2 border-b border-gray-100 text-xs text-gray-500">
                      Tài khoản: <strong className="text-gray-800 font-semibold">{user.username}</strong>
                    </div>

                    <Link
                      to="/orders/my"
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 hover:bg-orange-50 hover:text-chopee-orange"
                    >
                      <Package className="w-4 h-4" /> Đơn Mua Của Tôi
                    </Link>

                    {isSeller() && (
                      <Link
                        to="/seller"
                        onClick={() => setIsUserMenuOpen(false)}
                        className="flex items-center gap-2 px-4 py-2 hover:bg-orange-50 hover:text-chopee-orange"
                      >
                        <Store className="w-4 h-4" /> Quản Lý Gian Hàng
                      </Link>
                    )}

                    {isAdmin() && (
                      <Link
                        to="/admin"
                        onClick={() => setIsUserMenuOpen(false)}
                        className="flex items-center gap-2 px-4 py-2 hover:bg-orange-50 hover:text-chopee-orange"
                      >
                        <Shield className="w-4 h-4" /> Cổng Quản Trị Sàn
                      </Link>
                    )}

                    <button
                      onClick={handleLogout}
                      className="w-full text-left flex items-center gap-2 px-4 py-2 hover:bg-red-50 text-red-600 border-t border-gray-100 mt-1"
                    >
                      <LogOut className="w-4 h-4" /> Đăng Xuất
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center space-x-3 font-medium">
                <Link to="/register" className="hover:text-orange-200">
                  Đăng Ký
                </Link>
                <span className="text-orange-300">|</span>
                <Link to="/login" className="hover:text-orange-200">
                  Đăng Nhập
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Main Search and Logo Bar */}
      <div className="max-w-7xl mx-auto px-4 py-3.5 flex items-center justify-between gap-6">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 flex-shrink-0 group">
          <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-chopee-orange shadow-sm font-black text-xl group-hover:scale-105 transition-transform">
            <ShoppingBag className="w-6 h-6" />
          </div>
          <div>
            <span className="text-2xl font-black tracking-tight block leading-none">Chopee</span>
            <span className="text-[10px] text-orange-100 font-normal tracking-wide block mt-0.5">
              Chợ Đa Ngành & Thực Phẩm Tươi
            </span>
          </div>
        </Link>

        {/* Search Bar */}
        <form onSubmit={handleSearch} className="flex-1 max-w-2xl relative">
          <div className="flex items-center bg-white rounded-lg shadow-inner overflow-hidden p-1">
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Tìm kiếm rau củ tươi, hải sản, đồ uống, thiết bị công nghệ..."
              className="w-full px-3 py-1.5 text-sm text-gray-800 placeholder-gray-400 focus:outline-none"
            />
            <button
              type="submit"
              className="bg-chopee-orange hover:bg-orange-600 text-white px-5 py-2 rounded-md flex items-center justify-center transition-colors"
            >
              <Search className="w-4 h-4" />
            </button>
          </div>
        </form>

        {/* Cart Icon */}
        <Link
          to="/cart"
          className="relative p-2.5 text-white hover:text-orange-200 transition-colors flex-shrink-0"
          title="Giỏ hàng Chopee"
        >
          <ShoppingCart className="w-7 h-7" />
          {totalItemCount > 0 && (
            <span className="absolute top-0.5 right-0.5 bg-white text-chopee-orange font-bold text-xs rounded-full min-w-[20px] h-5 px-1 flex items-center justify-center shadow-md border-2 border-chopee-orange animate-bounce">
              {totalItemCount > 99 ? '99+' : totalItemCount}
            </span>
          )}
        </Link>
      </div>
    </header>
  );
};
