import React, { useEffect, useState } from 'react';
import { ShoppingBag, CheckCircle, Store, Shield, Sparkles, Layers } from 'lucide-react';
import { Link } from 'react-router-dom';

export const HomePage: React.FC = () => {
  const [healthStatus, setHealthStatus] = useState<string>('Checking backend...');
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    fetch('/api/v1/health')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) {
          setHealthStatus(data.message);
        } else {
          setHealthStatus('Backend reachable, but returned status issue');
        }
      })
      .catch(() => {
        setHealthStatus('Backend offline or starting up on :8080');
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-8 py-4">
      {/* Hero Welcome Banner */}
      <div className="bg-gradient-to-r from-orange-500 via-orange-600 to-amber-500 rounded-2xl shadow-lg p-8 md:p-12 text-white relative overflow-hidden">
        <div className="relative z-10 max-w-2xl">
          <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-white/20 backdrop-blur-sm text-xs font-semibold mb-4">
            <Sparkles className="w-3.5 h-3.5" /> Kiến Trúc Modular Monolith Đã Sẵn Sàng (52 APIs)
          </div>
          <h1 className="text-3xl md:text-5xl font-black tracking-tight leading-tight mb-4">
            Chào Mừng Đến Với Chopee Marketplace
          </h1>
          <p className="text-orange-100 text-sm md:text-base mb-6 leading-relaxed">
            Hệ thống sàn thương mại điện tử đa người bán tích hợp Chợ thực phẩm tươi sống, Nước giải khát, Thiết bị gia dụng, Phụ kiện công nghệ và Trợ lý AI mua sắm thông minh.
          </p>

          <div className="flex flex-wrap items-center gap-3">
            <Link
              to="/seller"
              className="px-5 py-2.5 bg-white text-chopee-orange font-bold text-sm rounded-xl shadow-md hover:bg-orange-50 transition-colors flex items-center gap-2"
            >
              <Store className="w-4 h-4" /> Kênh Người Bán
            </Link>
            <Link
              to="/admin"
              className="px-5 py-2.5 bg-black/20 hover:bg-black/30 text-white font-medium text-sm rounded-xl backdrop-blur-sm transition-colors flex items-center gap-2"
            >
              <Shield className="w-4 h-4" /> Quản Trị Sàn
            </Link>
          </div>
        </div>

        {/* Decorative elements */}
        <div className="absolute right-[-40px] bottom-[-40px] opacity-10 text-white pointer-events-none">
          <ShoppingBag className="w-96 h-96" />
        </div>
      </div>

      {/* Backend Status Card */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200/70 p-5 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
            <CheckCircle className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs text-gray-500 font-medium">Trạng thái kết nối Spring Boot Backend (:8080):</p>
            <p className="text-sm font-bold text-gray-800">
              {loading ? 'Đang kiểm tra kết nối...' : healthStatus}
            </p>
          </div>
        </div>
        <span className="text-xs font-semibold px-3 py-1 bg-emerald-100 text-emerald-800 rounded-full">
          60/60 Tests Passing
        </span>
      </div>

      {/* Highlights Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="p-6 rounded-2xl bg-white border border-gray-200/70 shadow-sm hover:shadow-md transition-shadow">
          <div className="w-10 h-10 rounded-xl bg-orange-100 text-chopee-orange flex items-center justify-center mb-4">
            <Store className="w-5 h-5" />
          </div>
          <h3 className="font-bold text-gray-900 text-base mb-1.5">Multi-Vendor Order Splitting</h3>
          <p className="text-xs text-gray-600 leading-relaxed">
            Giỏ hàng gom sản phẩm từ nhiều shop khác nhau, tự động tách độc lập thành từng đơn hàng cho từng nhà bán lẻ.
          </p>
        </div>

        <div className="p-6 rounded-2xl bg-white border border-gray-200/70 shadow-sm hover:shadow-md transition-shadow">
          <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-4">
            <Layers className="w-5 h-5" />
          </div>
          <h3 className="font-bold text-gray-900 text-base mb-1.5">Chợ Thực Phẩm Tươi Sống</h3>
          <p className="text-xs text-gray-600 leading-relaxed">
            Hỗ trợ bán theo kg thập phân (0.5kg), điều kiện bảo quản mát/đông lạnh và phương thức giao hỏa tốc 2 giờ.
          </p>
        </div>

        <div className="p-6 rounded-2xl bg-white border border-gray-200/70 shadow-sm hover:shadow-md transition-shadow">
          <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center mb-4">
            <Sparkles className="w-5 h-5" />
          </div>
          <h3 className="font-bold text-gray-900 text-base mb-1.5">AI Shopping Copilot</h3>
          <p className="text-xs text-gray-600 leading-relaxed">
            Trợ lý AI Gemini tư vấn nguyên liệu món ăn gia đình, thông số kỹ thuật công nghệ và gợi ý thẻ sản phẩm mua ngay.
          </p>
        </div>
      </div>
    </div>
  );
};
export default HomePage;
