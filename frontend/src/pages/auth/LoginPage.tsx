import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingBag, Lock, User, AlertCircle, ArrowRight } from 'lucide-react';
import { useAuthStore } from '../../stores/useAuthStore';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isLoading, error, clearError } = useAuthStore();

  const [emailOrUsername, setEmailOrUsername] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  // Get return redirect url
  const searchParams = new URLSearchParams(location.search);
  const redirectUrl = searchParams.get('redirect') || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    clearError();

    if (!emailOrUsername.trim() || !password) {
      setFormError('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu');
      return;
    }

    try {
      const auth = await login(emailOrUsername.trim(), password);
      if (auth.user.role === 'ROLE_SELLER' && redirectUrl === '/') {
        navigate('/seller');
      } else if (auth.user.role === 'ROLE_ADMIN' && redirectUrl === '/') {
        navigate('/admin');
      } else {
        navigate(redirectUrl);
      }
    } catch (err: any) {
      setFormError(err.message || 'Đăng nhập không thành công');
    }
  };

  const handleQuickLogin = async (userAcc: string, pass: string) => {
    setEmailOrUsername(userAcc);
    setPassword(pass);
    setFormError(null);
    clearError();

    try {
      const auth = await login(userAcc, pass);
      if (auth.user.role === 'ROLE_SELLER') {
        navigate('/seller');
      } else if (auth.user.role === 'ROLE_ADMIN') {
        navigate('/admin');
      } else {
        navigate(redirectUrl);
      }
    } catch (err: any) {
      setFormError(err.message);
    }
  };

  return (
    <div className="min-h-screen bg-orange-50/40 flex flex-col justify-between">
      {/* Header */}
      <header className="bg-white border-b border-gray-100 py-4 px-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-10 h-10 bg-chopee-orange rounded-xl flex items-center justify-center text-white shadow-sm font-bold text-xl">
              <ShoppingBag className="w-6 h-6" />
            </div>
            <div>
              <span className="text-2xl font-black text-gray-900 tracking-tight">Chopee</span>
              <span className="text-xs text-chopee-orange font-semibold ml-2">Đăng Nhập</span>
            </div>
          </Link>
          <a href="#" className="text-xs text-chopee-orange hover:underline font-medium">
            Bạn cần trợ giúp?
          </a>
        </div>
      </header>

      {/* Main Login Form */}
      <main className="flex-1 flex items-center justify-center p-4 py-12">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
          <h2 className="text-2xl font-extrabold text-gray-900 mb-1">Đăng Nhập Chopee</h2>
          <p className="text-gray-500 text-xs mb-6">
            Mua sắm nông sản sạch, đồ uống và thiết bị công nghệ với giá tốt nhất.
          </p>

          {(formError || error) && (
            <div className="mb-5 p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{formError || error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Email hoặc Tên đăng nhập
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={emailOrUsername}
                  onChange={(e) => setEmailOrUsername(e.target.value)}
                  placeholder="Ví dụ: buyer1, seller_food hoặc admin"
                  className="w-full pl-10 pr-4 py-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
                  required
                />
                <User className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Mật khẩu</label>
              <div className="relative">
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Nhập mật khẩu của bạn"
                  className="w-full pl-10 pr-4 py-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
                  required
                />
                <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-2 py-3 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-sm rounded-xl shadow-md shadow-orange-500/20 transition-all flex items-center justify-center gap-2 disabled:opacity-60"
            >
              {isLoading ? 'Đang xác thực...' : 'Đăng Nhập'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Quick Demo Accounts Helper */}
          <div className="mt-6 pt-5 border-t border-gray-100">
            <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider mb-2">
              Đăng nhập thử nghiệm nhanh:
            </p>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleQuickLogin('buyer1', '123456')}
                className="px-2 py-1.5 bg-gray-50 hover:bg-orange-50 hover:text-chopee-orange hover:border-orange-200 border border-gray-200 rounded-lg text-xs font-medium text-gray-700 transition-colors"
              >
                🛒 Buyer
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('seller_food', '123456')}
                className="px-2 py-1.5 bg-gray-50 hover:bg-orange-50 hover:text-chopee-orange hover:border-orange-200 border border-gray-200 rounded-lg text-xs font-medium text-gray-700 transition-colors"
              >
                🏪 Seller
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('admin', '123456')}
                className="px-2 py-1.5 bg-gray-50 hover:bg-orange-50 hover:text-chopee-orange hover:border-orange-200 border border-gray-200 rounded-lg text-xs font-medium text-gray-700 transition-colors"
              >
                🛡️ Admin
              </button>
            </div>
          </div>

          {/* Register links */}
          <div className="mt-6 text-center text-xs text-gray-500 space-y-2">
            <p>
              Bạn mới biết đến Chopee?{' '}
              <Link to="/register" className="text-chopee-orange font-bold hover:underline">
                Đăng ký ngay
              </Link>
            </p>
            <p>
              Muốn mở gian hàng?{' '}
              <Link to="/register-seller" className="text-blue-600 font-semibold hover:underline">
                Đăng ký Kênh Người Bán
              </Link>
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="py-4 text-center text-xs text-gray-400 border-t border-gray-200 bg-white">
        © 2026 Chopee Marketplace. Bảo mật phiên đăng nhập qua JWT Stateless.
      </footer>
    </div>
  );
};
export default LoginPage;

