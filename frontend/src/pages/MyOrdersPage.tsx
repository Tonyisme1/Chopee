import React, { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Package,
  Store,
  Clock,
  Truck,
  CheckCircle2,
  XCircle,
  Star,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
} from 'lucide-react';
import { Order, OrderItem, PageResponse } from '../types';
import { orderApi } from '../services/api';
import { useAuthStore } from '../stores/useAuthStore';
import { ReviewModal } from '../components/ReviewModal';

export const MyOrdersPage: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();

  const [activeTab, setActiveTab] = useState<string>('');
  const [orders, setOrders] = useState<Order[]>([]);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [loading, setLoading] = useState<boolean>(true);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  // Review Modal state
  const [reviewingItem, setReviewingItem] = useState<OrderItem | null>(null);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    try {
      const res = await orderApi.getOrders({
        status: activeTab || undefined,
        page,
        size: 5,
      });

      if (res.success && res.data) {
        const pageData = res.data as PageResponse<Order>;
        setOrders(pageData.content || []);
        setTotalPages(pageData.totalPages || 1);
      }
    } catch (err) {
      console.error('Lỗi tải danh sách đơn hàng:', err);
    } finally {
      setLoading(false);
    }
  }, [activeTab, page]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchOrders();
    } else {
      navigate('/login?redirect=/orders/my');
    }
  }, [isAuthenticated, fetchOrders, navigate]);

  const handleCancelOrder = async (orderCode: string) => {
    const confirm = window.confirm(`Bạn có chắc chắn muốn hủy đơn hàng ${orderCode}?`);
    if (!confirm) return;

    try {
      await orderApi.cancelOrder(orderCode, 'Người mua tự hủy');
      setActionSuccess(`Đã hủy thành công đơn hàng ${orderCode}`);
      setTimeout(() => setActionSuccess(null), 3000);
      fetchOrders();
    } catch (err: any) {
      alert(err.message || 'Không thể hủy đơn hàng');
    }
  };

  const getStatusPill = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200 flex items-center gap-1">
            <Clock className="w-3.5 h-3.5" /> Chờ xác nhận
          </span>
        );
      case 'CONFIRMED':
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200 flex items-center gap-1">
            <Package className="w-3.5 h-3.5" /> Đã xác nhận
          </span>
        );
      case 'SHIPPING':
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-cyan-50 text-cyan-700 border border-cyan-200 flex items-center gap-1">
            <Truck className="w-3.5 h-3.5" /> Đang vận chuyển
          </span>
        );
      case 'DELIVERED':
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1">
            <CheckCircle2 className="w-3.5 h-3.5" /> Giao thành công
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-gray-100 text-gray-500 border border-gray-200 flex items-center gap-1">
            <XCircle className="w-3.5 h-3.5" /> Đã hủy
          </span>
        );
      default:
        return null;
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  const tabs = [
    { key: '', label: 'Tất cả' },
    { key: 'PENDING', label: 'Chờ xác nhận' },
    { key: 'CONFIRMED', label: 'Đã xác nhận' },
    { key: 'SHIPPING', label: 'Đang giao' },
    { key: 'DELIVERED', label: 'Đã giao' },
    { key: 'CANCELLED', label: 'Đã hủy' },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-black text-gray-900 tracking-tight flex items-center gap-2">
          <Package className="w-6 h-6 text-chopee-orange" />
          <span>Đơn Mua Của Tôi</span>
        </h1>
      </div>

      {actionSuccess && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-xl flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* Tabs */}
      <div className="bg-white rounded-2xl border border-gray-100 p-2 shadow-sm flex items-center gap-1 overflow-x-auto text-xs font-semibold">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => {
              setActiveTab(tab.key);
              setPage(0);
            }}
            className={`px-4 py-2.5 rounded-xl whitespace-nowrap transition-all ${
              activeTab === tab.key
                ? 'bg-chopee-orange text-white shadow-sm font-bold'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Order Cards List */}
      {loading ? (
        <div className="min-h-[300px] flex items-center justify-center text-gray-400">
          <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
        </div>
      ) : orders.length === 0 ? (
        <div className="min-h-[350px] bg-white rounded-2xl border border-gray-100 p-8 text-center flex flex-col items-center justify-center">
          <div className="w-16 h-16 bg-orange-50 text-chopee-orange rounded-full flex items-center justify-center mb-3">
            <Package className="w-8 h-8" />
          </div>
          <p className="text-base font-bold text-gray-800 mb-1">Chưa có đơn hàng nào</p>
          <p className="text-xs text-gray-400 mb-4">Các đơn hàng bạn đặt sẽ xuất hiện tại đây.</p>
          <Link
            to="/"
            className="px-5 py-2 bg-chopee-orange text-white font-bold text-xs rounded-xl shadow-sm"
          >
            Khám phá mua sắm ngay
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div
              key={order.id}
              className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden"
            >
              {/* Order Header */}
              <div className="p-4 bg-gray-50/70 border-b border-gray-100 flex items-center justify-between text-xs">
                <div className="flex items-center gap-3">
                  <div className="flex items-center gap-1.5 font-bold text-gray-900">
                    <Store className="w-4 h-4 text-chopee-orange" />
                    <span>{order.shopName}</span>
                  </div>
                  <span className="text-gray-300">|</span>
                  <span className="font-mono text-gray-500 font-medium">#{order.orderCode}</span>
                </div>

                <div className="flex items-center gap-2">{getStatusPill(order.status)}</div>
              </div>

              {/* Order Items */}
              <div className="p-4 divide-y divide-gray-50">
                {order.items.map((item) => (
                  <div key={item.id} className="py-3 flex items-center justify-between text-xs">
                    <div className="flex items-center gap-3">
                      <img
                        src={item.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e'}
                        alt={item.productName}
                        className="w-14 h-14 object-cover rounded-xl border border-gray-100"
                      />
                      <div className="space-y-1">
                        <Link
                          to={`/products/${item.productId}`}
                          className="font-bold text-gray-800 hover:text-chopee-orange line-clamp-1 transition-colors"
                        >
                          {item.productName}
                        </Link>
                        <p className="text-[11px] text-gray-400">
                          {item.quantity} {item.unit} x {formatCurrency(item.unitPrice)}
                        </p>
                      </div>
                    </div>

                    <div className="flex flex-col items-end gap-2">
                      <span className="font-black text-gray-900">{formatCurrency(item.subtotal)}</span>

                      {/* Review button if DELIVERED */}
                      {order.status === 'DELIVERED' && (
                        <button
                          type="button"
                          onClick={() => setReviewingItem(item)}
                          className="px-3 py-1 bg-amber-50 text-amber-700 hover:bg-amber-100 border border-amber-200 rounded-lg text-xs font-bold flex items-center gap-1 transition-colors"
                        >
                          <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                          <span>Đánh Giá</span>
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              {/* Order Footer & Actions */}
              <div className="p-4 bg-gray-50/40 border-t border-gray-100 flex flex-col md:flex-row items-center justify-between gap-3 text-xs">
                <div className="text-gray-500 text-[11px]">
                  <span>Vận chuyển: {order.shippingMethod === 'EXPRESS_FRESH' ? 'Hỏa tốc 2H' : 'Tiêu chuẩn'}</span>
                  <span className="mx-2">•</span>
                  <span>Thanh toán: {order.paymentMethod === 'VNPAY' ? 'VNPay Sandbox' : 'COD (Tiền mặt)'}</span>
                </div>

                <div className="flex items-center gap-4">
                  <div className="text-right">
                    <span className="text-gray-500 mr-2">Tổng số tiền:</span>
                    <span className="text-base font-black text-chopee-orange">
                      {formatCurrency(order.finalAmount)}
                    </span>
                  </div>

                  {order.status === 'PENDING' && (
                    <button
                      type="button"
                      onClick={() => handleCancelOrder(order.orderCode)}
                      className="px-4 py-2 bg-red-50 text-red-600 hover:bg-red-100 border border-red-200 font-bold rounded-xl transition-colors"
                    >
                      Hủy Đơn Hàng
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-6">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="p-2 rounded-lg border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4 text-gray-600" />
              </button>
              <span className="text-xs font-semibold text-gray-700 px-3">
                Trang {page + 1} / {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="p-2 rounded-lg border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4 text-gray-600" />
              </button>
            </div>
          )}
        </div>
      )}

      {/* Review Modal */}
      <ReviewModal
        isOpen={!!reviewingItem}
        onClose={() => setReviewingItem(null)}
        orderItem={reviewingItem}
        onSuccess={() => {
          setActionSuccess('Cảm ơn bạn! Đánh giá đã được ghi nhận thành công.');
          setTimeout(() => setActionSuccess(null), 3000);
        }}
      />
    </div>
  );
};
export default MyOrdersPage;
