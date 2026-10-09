import React, { useEffect, useState, useCallback } from 'react';
import {
  Package,
  Clock,
  CheckCircle,
  Truck,
  XCircle,
  AlertCircle,
  RefreshCw,
  Search,
} from 'lucide-react';
import { sellerApi } from '../../services/api';
import { Order } from '../../types';

type OrderStatusFilter = 'ALL' | 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED';

export const SellerOrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<OrderStatusFilter>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setActionError(null);
    try {
      const params: { status?: string; page: number; size: number } = {
        page: 0,
        size: 50,
      };
      if (statusFilter !== 'ALL') {
        params.status = statusFilter;
      }
      const res = await sellerApi.getOrders(params);
      if (res.success && res.data) {
        setOrders(res.data.content || []);
      }
    } catch (err: any) {
      console.error('Lỗi tải danh sách đơn hàng seller:', err);
      setActionError(err.response?.data?.message || 'Không thể tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleUpdateStatus = async (orderId: number, nextStatus: string) => {
    if (!window.confirm(`Bạn có chắc muốn chuyển trạng thái đơn hàng sang "${nextStatus}"?`)) {
      return;
    }
    setActionLoading(orderId);
    setActionError(null);
    try {
      const res = await sellerApi.updateOrderStatus(orderId, { status: nextStatus });
      if (res.success) {
        await fetchOrders();
      }
    } catch (err: any) {
      console.error('Lỗi cập nhật trạng thái đơn:', err);
      setActionError(err.response?.data?.message || 'Không thể cập nhật trạng thái đơn hàng');
    } finally {
      setActionLoading(null);
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
            <Clock className="w-3.5 h-3.5" /> Chờ xác nhận
          </span>
        );
      case 'CONFIRMED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200">
            <CheckCircle className="w-3.5 h-3.5" /> Đã xác nhận / Đóng gói
          </span>
        );
      case 'SHIPPING':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-purple-50 text-purple-700 border border-purple-200">
            <Truck className="w-3.5 h-3.5" /> Đang vận chuyển
          </span>
        );
      case 'DELIVERED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle className="w-3.5 h-3.5" /> Giao thành công
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-50 text-red-700 border border-red-200">
            <XCircle className="w-3.5 h-3.5" /> Đã hủy
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-700">
            {status}
          </span>
        );
    }
  };

  const filteredOrders = orders.filter((o) => {
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase();
    const matchCode = o.orderCode?.toLowerCase().includes(term);
    const matchGroup = o.groupOrderCode?.toLowerCase().includes(term);
    const matchReceiver = o.shippingName?.toLowerCase().includes(term);
    const matchPhone = o.shippingPhone?.toLowerCase().includes(term);
    return matchCode || matchGroup || matchReceiver || matchPhone;
  });

  const tabs: { key: OrderStatusFilter; label: string }[] = [
    { key: 'ALL', label: 'Tất cả' },
    { key: 'PENDING', label: 'Chờ xác nhận' },
    { key: 'CONFIRMED', label: 'Đã xác nhận' },
    { key: 'SHIPPING', label: 'Đang giao' },
    { key: 'DELIVERED', label: 'Đã giao' },
    { key: 'CANCELLED', label: 'Đã hủy' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">Quản Lý Đơn Hàng</h1>
          <p className="text-xs text-gray-500 mt-1">
            Theo dõi, xác nhận và cập nhật tiến độ giao vận cho các đơn hàng của gian hàng.
          </p>
        </div>
        <button
          onClick={fetchOrders}
          disabled={loading}
          className="inline-flex items-center gap-2 px-3.5 py-2 border border-gray-200 bg-white hover:bg-gray-50 rounded-xl text-xs font-bold text-gray-700 shadow-sm transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Làm mới</span>
        </button>
      </div>

      {actionError && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {/* Tabs & Search */}
      <div className="bg-white rounded-2xl border border-gray-100 p-4 shadow-sm space-y-4">
        <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-gray-100">
          {tabs.map((t) => (
            <button
              key={t.key}
              onClick={() => setStatusFilter(t.key)}
              className={`px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-colors ${
                statusFilter === t.key
                  ? 'bg-chopee-orange text-white shadow-sm'
                  : 'bg-gray-50 text-gray-600 hover:bg-gray-100'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div className="relative">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Tìm kiếm theo mã đơn, mã nhóm, tên khách hàng hoặc số điện thoại..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-chopee-orange"
          />
        </div>
      </div>

      {/* Orders List */}
      {loading ? (
        <div className="min-h-[300px] flex items-center justify-center text-gray-400">
          <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
        </div>
      ) : filteredOrders.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center shadow-sm">
          <Package className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-sm font-bold text-gray-700">Chưa có đơn hàng nào</p>
          <p className="text-xs text-gray-400 mt-1">
            Không tìm thấy đơn hàng nào phù hợp với bộ lọc hiện tại.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredOrders.map((order) => (
            <div
              key={order.id}
              className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden"
            >
              {/* Order Header */}
              <div className="bg-gray-50/80 px-5 py-3 border-b border-gray-100 flex flex-wrap items-center justify-between gap-3 text-xs">
                <div className="flex items-center gap-3">
                  <span className="font-mono font-bold text-gray-900">
                    Mã đơn: {order.orderCode}
                  </span>
                  {order.groupOrderCode && (
                    <span className="text-gray-400 font-mono text-[11px]">
                      (Nhóm: {order.groupOrderCode})
                    </span>
                  )}
                  <span className="text-gray-400">•</span>
                  <span className="text-gray-500">{formatDate(order.createdAt)}</span>
                </div>
                <div>{getStatusBadge(order.status)}</div>
              </div>

              {/* Order Body */}
              <div className="p-5 grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Items column */}
                <div className="lg:col-span-2 space-y-3">
                  <span className="text-xs font-bold text-gray-700">Danh sách sản phẩm:</span>
                  <div className="divide-y divide-gray-100">
                    {order.items?.map((item) => (
                      <div key={item.id} className="py-2.5 flex items-center gap-3">
                        <img
                          src={item.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=100'}
                          alt={item.productName}
                          className="w-12 h-12 object-cover rounded-lg border border-gray-100 shrink-0"
                        />
                        <div className="flex-1 min-w-0">
                          <p className="text-xs font-bold text-gray-900 truncate">
                            {item.productName}
                          </p>
                          <p className="text-[11px] text-gray-500 mt-0.5">
                            Số lượng: <strong className="text-gray-700">{item.quantity}</strong>{' '}
                            {item.unit || 'món'} × {formatCurrency(item.unitPrice)}
                          </p>
                        </div>
                        <span className="text-xs font-bold text-gray-900">
                          {formatCurrency(item.subtotal)}
                        </span>
                      </div>
                    ))}
                  </div>

                  {order.note && (
                    <div className="p-3 bg-amber-50/60 border border-amber-100 rounded-xl text-[11px] text-amber-800">
                      <strong>Ghi chú của người mua:</strong> {order.note}
                    </div>
                  )}
                </div>

                {/* Receiver & Pricing Summary */}
                <div className="bg-gray-50/60 rounded-xl p-4 border border-gray-100 space-y-3 text-xs flex flex-col justify-between">
                  <div className="space-y-2">
                    <span className="font-bold text-gray-800">Thông tin nhận hàng</span>
                    <div className="text-gray-600 space-y-1 text-[11px]">
                      <p className="font-semibold text-gray-900">{order.shippingName}</p>
                      <p>SĐT: {order.shippingPhone}</p>
                      <p className="line-clamp-2">Địa chỉ: {order.shippingAddress}</p>
                      <p className="text-chopee-orange font-medium mt-1">
                        Vận chuyển:{' '}
                        {order.shippingMethod === 'EXPRESS_FRESH'
                          ? 'Giao Nhanh Thực Phẩm Tươi Sống 2H'
                          : 'Giao Tiêu Chuẩn'}
                      </p>
                    </div>

                    <div className="pt-2 border-t border-gray-200 space-y-1 text-[11px]">
                      <div className="flex justify-between text-gray-500">
                        <span>Tiền hàng:</span>
                        <span>{formatCurrency(order.totalAmount)}</span>
                      </div>
                      <div className="flex justify-between text-gray-500">
                        <span>Phí giao hàng:</span>
                        <span>{formatCurrency(order.shippingFee)}</span>
                      </div>
                      {order.discountAmount > 0 && (
                        <div className="flex justify-between text-emerald-600 font-semibold">
                          <span>Giảm giá voucher:</span>
                          <span>-{formatCurrency(order.discountAmount)}</span>
                        </div>
                      )}
                      <div className="flex justify-between text-gray-900 font-black text-xs pt-1 border-t border-gray-200">
                        <span>Tổng thu:</span>
                        <span className="text-chopee-orange">
                          {formatCurrency(order.finalAmount)}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Actions depending on status */}
                  <div className="pt-3 border-t border-gray-200 space-y-2">
                    {order.status === 'PENDING' && (
                      <button
                        onClick={() => handleUpdateStatus(order.id, 'CONFIRMED')}
                        disabled={actionLoading === order.id}
                        className="w-full py-2 bg-chopee-orange hover:bg-orange-600 text-white rounded-xl text-xs font-bold shadow-sm transition-colors flex items-center justify-center gap-1.5"
                      >
                        {actionLoading === order.id ? (
                          <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                          <CheckCircle className="w-3.5 h-3.5" />
                        )}
                        <span>Xác Nhận & Đóng Gói</span>
                      </button>
                    )}

                    {order.status === 'CONFIRMED' && (
                      <button
                        onClick={() => handleUpdateStatus(order.id, 'SHIPPING')}
                        disabled={actionLoading === order.id}
                        className="w-full py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-xl text-xs font-bold shadow-sm transition-colors flex items-center justify-center gap-1.5"
                      >
                        {actionLoading === order.id ? (
                          <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                          <Truck className="w-3.5 h-3.5" />
                        )}
                        <span>Giao Cho Đơn Vị Vận Chuyển</span>
                      </button>
                    )}

                    {order.status === 'SHIPPING' && (
                      <button
                        onClick={() => handleUpdateStatus(order.id, 'DELIVERED')}
                        disabled={actionLoading === order.id}
                        className="w-full py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-sm transition-colors flex items-center justify-center gap-1.5"
                      >
                        {actionLoading === order.id ? (
                          <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                          <CheckCircle className="w-3.5 h-3.5" />
                        )}
                        <span>Xác Nhận Đã Giao Thành Công</span>
                      </button>
                    )}

                    {order.status === 'DELIVERED' && (
                      <div className="text-center text-[11px] text-emerald-600 font-bold py-1">
                        ✓ Đơn hàng đã hoàn tất
                      </div>
                    )}

                    {order.status === 'CANCELLED' && (
                      <div className="text-center text-[11px] text-red-600 font-bold py-1">
                        Đơn hàng đã bị hủy
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
