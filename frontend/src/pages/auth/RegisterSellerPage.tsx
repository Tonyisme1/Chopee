import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Store, Phone, MapPin, FileText, AlertCircle, ArrowRight, Layers } from 'lucide-react';
import { useAuthStore } from '../../stores/useAuthStore';

export const RegisterSellerPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, registerSeller, isLoading, error, clearError } = useAuthStore();

  const [shopName, setShopName] = useState('');
  const [description, setDescription] = useState('');
  const [phone, setPhone] = useState(user?.phone || '');
  const [address, setAddress] = useState('');
  const [shopType, setShopType] = useState('FOOD_FRESH');
  const [formError, setFormError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    clearError();

    if (!shopName.trim() || !phone.trim() || !address.trim()) {
      setFormError('Vui lòng điền đầy đủ tên gian hàng, số điện thoại và địa chỉ kho');
      return;
    }

    try {
      await registerSeller({
        shopName: shopName.trim(),
        description: description.trim() || undefined,
        phone: phone.trim(),
        address: address.trim(),
        shopType,
      });
      navigate('/seller');
    } catch (err: any) {
      setFormError(err.message || 'Đăng ký gian hàng không thành công');
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-orange-50/40 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-xl border border-gray-100 p-8 text-center">
          <div className="w-16 h-16 bg-orange-100 text-chopee-orange rounded-full flex items-center justify-center mx-auto mb-4">
            <Store className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2">Đăng Nhập Trước Khi Mở Shop</h2>
          <p className="text-gray-600 text-sm mb-6">
            Bạn cần đăng nhập tài khoản Chopee trước khi đăng ký trở thành Người Bán.
          </p>
          <div className="flex flex-col gap-3">
            <Link
              to="/login?redirect=/register-seller"
              className="py-2.5 px-4 bg-chopee-orange hover:bg-orange-600 text-white font-semibold rounded-xl transition-colors"
            >
              Đăng Nhập Ngay
            </Link>
            <Link
              to="/register"
              className="py-2.5 px-4 bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium rounded-xl transition-colors"
            >
              Tạo Tài Khoản Mới
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-orange-50/40 flex flex-col justify-between">
      {/* Header */}
      <header className="bg-white border-b border-gray-100 py-4 px-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-10 h-10 bg-chopee-orange rounded-xl flex items-center justify-center text-white shadow-sm font-bold text-xl">
              <Store className="w-6 h-6" />
            </div>
            <div>
              <span className="text-2xl font-black text-gray-900 tracking-tight">Chopee</span>
              <span className="text-xs text-chopee-orange font-semibold ml-2">Đăng Ký Kênh Người Bán</span>
            </div>
          </Link>
          <Link to="/" className="text-xs text-gray-600 hover:text-chopee-orange font-medium">
            Quay lại mua sắm
          </Link>
        </div>
      </header>

      {/* Main Register Form */}
      <main className="flex-1 flex items-center justify-center p-4 py-10">
        <div className="max-w-xl w-full bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
          <h2 className="text-2xl font-extrabold text-gray-900 mb-1">Mở Gian Hàng Kinh Doanh</h2>
          <p className="text-gray-500 text-xs mb-6">
            Bắt đầu bán nông sản thực phẩm tươi sống, đồ uống hoặc thiết bị gia dụng tới hàng triệu khách hàng Chopee.
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
                Tên Gian Hàng / Cửa Hàng <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={shopName}
                  onChange={(e) => setShopName(e.target.value)}
                  placeholder="Ví dụ: Nông Sản Sạch Đà Lạt Farm"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <Store className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Mô tả giới thiệu cửa hàng
              </label>
              <div className="relative">
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Chuyên cung cấp rau củ chuẩn VietGAP, tươi ngon mỗi ngày..."
                  rows={3}
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                />
                <FileText className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Số điện thoại liên hệ <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="0912345678"
                    className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                    required
                  />
                  <Phone className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Loại hình gian hàng <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <select
                    value={shopType}
                    onChange={(e) => setShopType(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                    <option value="FOOD_FRESH">Chợ Thực Phẩm Tươi Sống (Rau, Củ, Thịt, Cá)</option>
                    <option value="GENERAL">Cửa Hàng Tổng Hợp (Gia dụng, Đồ uống, Đồ khô)</option>
                    <option value="OFFICIAL_MALL">Gian Hàng Chính Hãng (Chopee Mall)</option>
                  </select>
                  <Layers className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">
                Địa chỉ lấy hàng / Kho xuất hàng <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="Ví dụ: 123 Phường Cam Ly, TP. Đà Lạt, Lâm Đồng"
                  className="w-full pl-10 pr-4 py-2 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <MapPin className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-4 py-3 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-sm rounded-xl shadow-md shadow-orange-500/20 transition-all flex items-center justify-center gap-2 disabled:opacity-60"
            >
              {isLoading ? 'Đang kích hoạt gian hàng...' : 'Hoàn Tất Mở Gian Hàng'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>
        </div>
      </main>

      {/* Footer */}
      <footer className="py-4 text-center text-xs text-gray-400 border-t border-gray-200 bg-white">
        © 2026 Chopee Marketplace. Bằng cách đăng ký, bạn cam kết tuân thủ chính sách người bán và tiêu chuẩn an toàn thực phẩm.
      </footer>
    </div>
  );
};
export default RegisterSellerPage;
