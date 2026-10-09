import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  DollarSign,
  Package,
  ShoppingBag,
  Star,
  Clock,
  ArrowRight,
  RefreshCw,
} from 'lucide-react';
import { sellerApi } from '../../services/api';

export const SellerDashboardPage: React.FC = () => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    sellerApi
      .getDashboard()
      .then((res) => {
        if (res.success && res.data) {
          setData(res.data);
        }
      })
      .catch((err) => console.error('Lỗi tải dashboard seller:', err))
      .finally(() => setLoading(false));
  }, []);

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

  if (loading) {
    return (
      <div className="min-h-[400px] flex items-center justify-center text-gray-400">
        <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
      </div>
    );
  }

  const stats = [
    {
      title: 'Doanh Thu Gian Hàng',
      value: formatCurrency(data?.totalRevenue || 0),
      icon: DollarSign,
      color: 'bg-emerald-50 text-emerald-600 border-emerald-200',
      desc: 'Tổng số tiền từ các đơn đã giao thành công',
    },
    {
      title: 'Đơn Chờ Xác Nhận',
      value: data?.pendingOrdersCount || 0,
      icon: Clock,
      color: 'bg-amber-50 text-amber-600 border-amber-200',
      desc: 'Cần xác nhận và đóng gói cho shipper',
    },
    {
      title: 'Sản Phẩm Đang Bán',
      value: data?.activeProductsCount || 0,
      icon: Package,
      color: 'bg-blue-50 text-blue-600 border-blue-200',
      desc: 'Tồn kho khả dụng mở bán trên Chopee',
    },
    {
      title: 'Đánh Giá Cửa Hàng',
      value: `${data?.shopRating ? data.shopRating.toFixed(1) : '5.0'} / 5.0`,
      icon: Star,
      color: 'bg-orange-50 text-chopee-orange border-orange-200',
      desc: 'Điểm uy tín đánh giá từ người mua',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Title */}
      <div>
        <h1 className="text-2xl font-black text-gray-900 tracking-tight">Tổng Quan Gian Hàng</h1>
        <p className="text-xs text-gray-500 mt-1">
          Theo dõi doanh số, đơn hàng và hiệu suất bán hàng của gian hàng bạn trên Chopee.
        </p>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((s, idx) => {
          const Icon = s.icon;
          return (
            <div
              key={idx}
              className="bg-white rounded-2xl border border-gray-100 p-5 shadow-sm space-y-3"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-gray-500">{s.title}</span>
                <div className={`w-9 h-9 rounded-xl border flex items-center justify-center ${s.color}`}>
                  <Icon className="w-5 h-5" />
                </div>
              </div>
              <div>
                <span className="text-2xl font-black text-gray-900 tracking-tight">{s.value}</span>
                <p className="text-[11px] text-gray-400 mt-1">{s.desc}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Operational Highlights */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Orders Action Box */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 text-chopee-orange font-bold text-sm mb-2">
              <ShoppingBag className="w-4 h-4" />
              <span>Xử Lý Đơn Hàng Mới</span>
            </div>
            <p className="text-xs text-gray-600 leading-relaxed">
              Bạn có <strong>{data?.pendingOrdersCount || 0} đơn hàng</strong> đang chờ xác nhận. Hãy nhanh chóng chuẩn bị hàng để đối tác vận chuyển Chopee Express lấy hàng đúng hạn.
            </p>
          </div>

          <Link
            to="/seller/orders"
            className="mt-6 inline-flex items-center gap-2 px-5 py-2.5 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-xs rounded-xl shadow-md transition-colors w-fit"
          >
            <span>Đến trang Đơn Hàng</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {/* Product Catalog Action Box */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 text-blue-600 font-bold text-sm mb-2">
              <Package className="w-4 h-4" />
              <span>Mở Rộng Sản Phẩm & Tồn Kho</span>
            </div>
            <p className="text-xs text-gray-600 leading-relaxed">
              Thêm mới nông sản thực phẩm tươi sống với đơn vị tính kg, hoặc đăng bán các thiết bị công nghệ kèm thông số JSON linh hoạt.
            </p>
          </div>

          <Link
            to="/seller/products"
            className="mt-6 inline-flex items-center gap-2 px-5 py-2.5 bg-gray-900 hover:bg-black text-white font-bold text-xs rounded-xl shadow-md transition-colors w-fit"
          >
            <span>Quản Lý Sản Phẩm</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </div>
    </div>
  );
};
export default SellerDashboardPage;
