import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  TrendingUp,
  Store,
  ShoppingBag,
  Users,
  ShieldCheck,
  AlertTriangle,
  ArrowRight,
  RefreshCw,
  Tag,
} from 'lucide-react';
import { adminApi } from '../../services/api';

export const AdminDashboardPage: React.FC = () => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const fetchDashboard = () => {
    setLoading(true);
    adminApi
      .getDashboard()
      .then((res) => {
        if (res.success && res.data) {
          setData(res.data);
        }
      })
      .catch((err) => console.error('Lỗi tải dashboard admin:', err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchDashboard();
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

  const primaryStats = [
    {
      title: 'Tổng Giá Trị Giao Dịch (GMV)',
      value: formatCurrency(data?.totalGmv || 0),
      icon: TrendingUp,
      color: 'bg-emerald-50 text-emerald-600 border-emerald-200',
      desc: 'Tổng số tiền giao dịch toàn sàn đã được thanh toán',
    },
    {
      title: 'Tổng Số Đơn Hàng',
      value: data?.totalOrders || 0,
      icon: ShoppingBag,
      color: 'bg-blue-50 text-blue-600 border-blue-200',
      desc: 'Đơn hàng phát sinh từ tất cả các gian hàng',
    },
    {
      title: 'Tổng Số Gian Hàng',
      value: data?.totalShops || 0,
      icon: Store,
      color: 'bg-purple-50 text-purple-600 border-purple-200',
      desc: `${data?.approvedShops || 0} đã duyệt, ${data?.pendingShops || 0} chờ duyệt`,
    },
    {
      title: 'Tổng Người Dùng Sàn',
      value: data?.totalUsers || 0,
      icon: Users,
      color: 'bg-orange-50 text-chopee-orange border-orange-200',
      desc: `${data?.totalBuyers || 0} người mua, ${data?.totalSellers || 0} chủ shop`,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">
            Tổng Quan Sàn Chopee
          </h1>
          <p className="text-xs text-gray-500 mt-1">
            Trung tâm giám sát hoạt động kinh doanh, người dùng và đối tác bán hàng toàn nền tảng.
          </p>
        </div>
        <button
          onClick={fetchDashboard}
          disabled={loading}
          className="inline-flex items-center gap-2 px-3.5 py-2 border border-gray-200 bg-white hover:bg-gray-50 rounded-xl text-xs font-bold text-gray-700 shadow-sm transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Làm mới số liệu</span>
        </button>
      </div>

      {/* Primary Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {primaryStats.map((s, idx) => {
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

      {/* Shop Status Distribution */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm space-y-4">
        <h2 className="text-sm font-bold text-gray-900">Tình Trạng Đối Tác Gian Hàng</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl bg-amber-50/60 border border-amber-200 flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-amber-800">Chờ Duyệt (Pending)</p>
              <p className="text-2xl font-black text-amber-900 mt-1">{data?.pendingShops || 0}</p>
              <p className="text-[11px] text-amber-700 mt-0.5">Cần phê duyệt mở bán</p>
            </div>
            <AlertTriangle className="w-8 h-8 text-amber-500 opacity-60" />
          </div>

          <div className="p-4 rounded-xl bg-emerald-50/60 border border-emerald-200 flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-emerald-800">Đang Hoạt Động (Approved)</p>
              <p className="text-2xl font-black text-emerald-900 mt-1">{data?.approvedShops || 0}</p>
              <p className="text-[11px] text-emerald-700 mt-0.5">Đủ điều kiện niêm yết sản phẩm</p>
            </div>
            <ShieldCheck className="w-8 h-8 text-emerald-500 opacity-60" />
          </div>

          <div className="p-4 rounded-xl bg-red-50/60 border border-red-200 flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-red-800">Đã Khóa (Locked)</p>
              <p className="text-2xl font-black text-red-900 mt-1">{data?.lockedShops || 0}</p>
              <p className="text-[11px] text-red-700 mt-0.5">Vi phạm quy định sàn Chopee</p>
            </div>
            <AlertTriangle className="w-8 h-8 text-red-500 opacity-60" />
          </div>
        </div>
      </div>

      {/* Quick Access Portals */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 text-chopee-orange font-bold text-sm mb-2">
              <Store className="w-4 h-4" />
              <span>Duyệt & Quản Lý Gian Hàng</span>
            </div>
            <p className="text-xs text-gray-600 leading-relaxed">
              Kiểm tra hồ sơ đăng ký kinh doanh, thông tin liên hệ và trạng thái hoạt động của các gian hàng thành viên trên Chopee Marketplace.
            </p>
          </div>

          <Link
            to="/admin/shops"
            className="mt-6 inline-flex items-center gap-2 px-5 py-2.5 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-xs rounded-xl shadow-md transition-colors w-fit"
          >
            <span>Đến trang Quản lý gian hàng</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 text-blue-600 font-bold text-sm mb-2">
              <Tag className="w-4 h-4" />
              <span>Mã Giảm Giá Toàn Sàn</span>
            </div>
            <p className="text-xs text-gray-600 leading-relaxed">
              Tạo và quản lý các voucher trợ giá kích cầu tiêu dùng như miễn phí vận chuyển FREESHIP, voucher mở bán tân thủ CHOPEE10K áp dụng toàn sàn.
            </p>
          </div>

          <Link
            to="/admin/vouchers"
            className="mt-6 inline-flex items-center gap-2 px-5 py-2.5 bg-gray-900 hover:bg-black text-white font-bold text-xs rounded-xl shadow-md transition-colors w-fit"
          >
            <span>Quản Lý Voucher Toàn Sàn</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </div>
    </div>
  );
};

