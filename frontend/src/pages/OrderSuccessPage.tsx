import React from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle2, PackageCheck, ShoppingBag, Store } from 'lucide-react';

export const OrderSuccessPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const groupOrderCode = searchParams.get('groupOrderCode') || 'ORD-CHOPEE';

  return (
    <div className="min-h-[500px] flex items-center justify-center py-10 px-4">
      <div className="max-w-lg w-full bg-white rounded-3xl border border-gray-100 shadow-xl p-8 md:p-10 text-center space-y-6">
        {/* Animated Success Icon */}
        <div className="w-20 h-20 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto shadow-inner animate-in zoom-in">
          <CheckCircle2 className="w-12 h-12" />
        </div>

        {/* Title & Congratulations */}
        <div>
          <h1 className="text-2xl md:text-3xl font-black text-gray-900 tracking-tight mb-2">
            Đặt Hàng Thành Công!
          </h1>
          <p className="text-xs text-gray-500 leading-relaxed max-w-sm mx-auto">
            Cảm ơn bạn đã mua sắm tại Chopee. Đơn hàng của bạn đã được gửi đến các đối tác gian hàng để chuẩn bị và đóng gói.
          </p>
        </div>

        {/* Group Order Code Highlight Card */}
        <div className="p-4 bg-orange-50/70 border border-orange-200/80 rounded-2xl text-left space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="text-gray-500 font-medium">Mã nhóm đơn chung:</span>
            <span className="font-mono font-black text-sm text-chopee-orange bg-white px-2.5 py-1 rounded-lg border border-orange-200">
              {groupOrderCode}
            </span>
          </div>

          <div className="flex items-start gap-2 pt-2 border-t border-orange-200/50 text-[11px] text-gray-600">
            <Store className="w-4 h-4 text-chopee-orange flex-shrink-0 mt-0.5" />
            <span>
              <strong>Kiến trúc Multi-Vendor:</strong> Giỏ hàng đã được phân tách độc lập thành các đơn hàng riêng biệt cho từng gian hàng để tối ưu vận chuyển.
            </span>
          </div>
        </div>

        {/* Navigation CTAs */}
        <div className="flex flex-col sm:flex-row gap-3 pt-2">
          <Link
            to="/orders/my"
            className="flex-1 py-3 px-5 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-xs rounded-xl shadow-md shadow-orange-500/20 transition-all flex items-center justify-center gap-2"
          >
            <PackageCheck className="w-4 h-4" />
            <span>Quản Lý Đơn Mua</span>
          </Link>

          <Link
            to="/"
            className="flex-1 py-3 px-5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-xs rounded-xl transition-all flex items-center justify-center gap-2"
          >
            <ShoppingBag className="w-4 h-4" />
            <span>Tiếp Tục Mua Sắm</span>
          </Link>
        </div>
      </div>
    </div>
  );
};
export default OrderSuccessPage;
