import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingBag, Lock, User, Mail, Phone, AlertCircle, ArrowRight } from 'lucide-react';
import { useAuthStore } from '../../stores/useAuthStore';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register, isLoading, error, clearError } = useAuthStore();

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    clearError();

    if (!username.trim() || !email.trim() || !fullName.trim() || !password) {
      setFormError('Vui lòng điền đầy đủ các thông tin bắt buộc');
      return;
    }

    if (password !== confirmPassword) {
      setFormError('Mật khẩu xác nhận không khớp');
      return;
    }

    if (password.length < 6) {
      setFormError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }

    try {
      await register({
        username: username.trim(),
        email: email.trim(),
        fullName: fullName.trim(),
        phone: phone.trim() || undefined,
        password,
      });
      navigate('/');
    } catch (err: any) {
      setFormError(err.message || 'Đăng ký tài khoản không thành công');
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
              <span className="text-xs text-chopee-orange font-semibold ml-2">Đăng Ký Tài Khoản</span>
            </div>
          </Link>
          <Link to="/login" className="text-xs text-chopee-orange hover:underline font-medium">
            Đã có tài khoản? Đăng nhập
          </Link>
        </div>
      </header>

      {/* Main Register Form */}
      <main className="flex-1 flex items-center justify-center p-4 py-10">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
          <h2 className="text-2xl font-extrabold text-gray-900 mb-1">Tạo Tài Khoản Chopee</h2>
          <p className="text-gray-500 text-xs mb-6">
            Khám phá hàng ngàn sản phẩm tươi ngon và tiện ích cho gia đình bạn.
          </p>

          {(formError || error) && (
            <div className="mb-5 p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{formError || error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3.5">
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Tên đăng nhập <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Ví dụ: nguyenvanan"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <User className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Họ và tên <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="Ví dụ: Nguyễn Văn An"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <User className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Địa chỉ Email <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <Mail className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Số điện thoại</label>
              <div className="relative">
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="0912345678"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                />
                <Phone className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Mật khẩu <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Ít nhất 6 ký tự"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Xác nhận mật khẩu <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Nhập lại mật khẩu"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-3 py-3 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-sm rounded-xl shadow-md shadow-orange-500/20 transition-all flex items-center justify-center gap-2 disabled:opacity-60"
            >
              {isLoading ? 'Đang tạo tài khoản...' : 'Đăng Ký Ngay'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="mt-6 text-center text-xs text-gray-500">
            Đã có tài khoản Chopee?{' '}
            <Link to="/login" className="text-chopee-orange font-bold hover:underline">
              Đăng nhập ngay
            </Link>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="py-4 text-center text-xs text-gray-400 border-t border-gray-200 bg-white">
        © 2026 Chopee Marketplace. Bằng cách đăng ký, bạn đồng ý với Điều khoản dịch vụ & Chính sách bảo mật.
      </footer>
    </div>
  );
};
export default RegisterPage;

