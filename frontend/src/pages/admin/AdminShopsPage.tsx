import React, { useEffect, useState, useCallback } from 'react';
import {
  Store,
  CheckCircle,
  XCircle,
  Lock,
  Unlock,
  AlertCircle,
  RefreshCw,
  Search,
  Phone,
  MapPin,
} from 'lucide-react';
import { adminApi } from '../../services/api';
import { Shop } from '../../types';

type ShopFilterStatus = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'LOCKED';

export const AdminShopsPage: React.FC = () => {
  const [shops, setShops] = useState<Shop[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<ShopFilterStatus>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchShops = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params: { status?: string; page: number; size: number } = {
        page: 0,
        size: 50,
      };
      if (statusFilter !== 'ALL') {
        params.status = statusFilter;
      }
      const res = await adminApi.getShops(params);
      if (res.success && res.data) {
        setShops(res.data.content || []);
      }
    } catch (err: any) {
      console.error('Lỗi tải danh sách gian hàng:', err);
      setError(err.response?.data?.message || 'Không thể tải danh sách gian hàng');
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    fetchShops();
  }, [fetchShops]);

  const handleUpdateStatus = async (shopId: number, nextStatus: string, shopName: string) => {
    const actionLabel =
      nextStatus === 'APPROVED' ? 'phê duyệt' : nextStatus === 'REJECTED' ? 'từ chối' : 'khóa';
    if (!window.confirm(`Bạn có chắc muốn ${actionLabel} gian hàng "${shopName}"?`)) {
      return;
    }

    setActionLoading(shopId);
    setError(null);
    try {
      const res = await adminApi.updateShopStatus(shopId, { status: nextStatus });
      if (res.success) {
        await fetchShops();
      }
    } catch (err: any) {
      console.error('Lỗi cập nhật trạng thái gian hàng:', err);
      setError(err.response?.data?.message || 'Không thể cập nhật trạng thái gian hàng');
    } finally {
      setActionLoading(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-50 text-amber-700 border border-amber-200">
            Chờ duyệt
          </span>
        );
      case 'APPROVED':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
            Đang hoạt động
          </span>
        );
      case 'REJECTED':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-gray-100 text-gray-700 border border-gray-200">
            Bị từ chối
          </span>
        );
      case 'LOCKED':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-red-50 text-red-700 border border-red-200">
            Bị khóa
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-gray-100 text-gray-700">
            {status}
          </span>
        );
    }
  };

  const filteredShops = shops.filter((s) => {
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase();
    const matchName = s.name?.toLowerCase().includes(term);
    const matchSlug = s.slug?.toLowerCase().includes(term);
    const matchPhone = s.phone?.toLowerCase().includes(term);
    return matchName || matchSlug || matchPhone;
  });

  const tabs: { key: ShopFilterStatus; label: string }[] = [
    { key: 'ALL', label: 'Tất cả' },
    { key: 'PENDING', label: 'Chờ duyệt' },
    { key: 'APPROVED', label: 'Đang hoạt động' },
    { key: 'LOCKED', label: 'Đã khóa' },
    { key: 'REJECTED', label: 'Từ chối' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">
            Duyệt & Quản Lý Gian Hàng
          </h1>
          <p className="text-xs text-gray-500 mt-1">
            Kiểm tra thông tin pháp lý, kiểm duyệt gian hàng mới và quản lý trạng thái đối tác.
          </p>
        </div>
        <button
          onClick={fetchShops}
          disabled={loading}
          className="inline-flex items-center gap-2 px-3.5 py-2 border border-gray-200 bg-white hover:bg-gray-50 rounded-xl text-xs font-bold text-gray-700 shadow-sm transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Làm mới</span>
        </button>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
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
            placeholder="Tìm kiếm theo tên gian hàng, đường dẫn slug hoặc số điện thoại..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-chopee-orange"
          />
        </div>
      </div>

      {/* Shop Table */}
      {loading ? (
        <div className="min-h-[300px] flex items-center justify-center text-gray-400">
          <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
        </div>
      ) : filteredShops.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center shadow-sm">
          <Store className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-sm font-bold text-gray-700">Chưa có gian hàng nào</p>
          <p className="text-xs text-gray-400 mt-1">
            Không tìm thấy gian hàng nào phù hợp với bộ lọc hiện tại.
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50 text-gray-500 font-bold uppercase border-b border-gray-100">
                <tr>
                  <th className="px-5 py-3">Gian hàng</th>
                  <th className="px-5 py-3">Liên hệ & Địa chỉ</th>
                  <th className="px-5 py-3">Đánh giá</th>
                  <th className="px-5 py-3">Trạng thái</th>
                  <th className="px-5 py-3 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 text-gray-700">
                {filteredShops.map((shop) => (
                  <tr key={shop.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <img
                          src={
                            shop.logoUrl ||
                            'https://images.unsplash.com/photo-1542838132-92c53300491e?w=100'
                          }
                          alt={shop.name}
                          className="w-11 h-11 rounded-xl object-cover border border-gray-200 shrink-0"
                        />
                        <div>
                          <p className="font-bold text-gray-900 text-sm">{shop.name}</p>
                          <p className="text-gray-400 font-mono text-[11px]">@{shop.slug}</p>
                          {shop.description && (
                            <p className="text-gray-500 text-[11px] line-clamp-1 mt-0.5 max-w-xs">
                              {shop.description}
                            </p>
                          )}
                        </div>
                      </div>
                    </td>

                    <td className="px-5 py-4 space-y-1 text-[11px]">
                      {shop.phone && (
                        <div className="flex items-center gap-1.5 text-gray-600">
                          <Phone className="w-3.5 h-3.5 text-gray-400" />
                          <span>{shop.phone}</span>
                        </div>
                      )}
                      {shop.address && (
                        <div className="flex items-center gap-1.5 text-gray-600">
                          <MapPin className="w-3.5 h-3.5 text-gray-400 shrink-0" />
                          <span className="line-clamp-1">{shop.address}</span>
                        </div>
                      )}
                    </td>

                    <td className="px-5 py-4">
                      <span className="font-bold text-amber-600">
                        ★ {shop.rating ? shop.rating.toFixed(1) : '5.0'}
                      </span>
                    </td>

                    <td className="px-5 py-4">{getStatusBadge(shop.status)}</td>

                    <td className="px-5 py-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        {shop.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleUpdateStatus(shop.id, 'APPROVED', shop.name)}
                              disabled={actionLoading === shop.id}
                              className="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-bold text-xs shadow-sm transition-colors"
                            >
                              <CheckCircle className="w-3.5 h-3.5" />
                              <span>Phê Duyệt</span>
                            </button>
                            <button
                              onClick={() => handleUpdateStatus(shop.id, 'REJECTED', shop.name)}
                              disabled={actionLoading === shop.id}
                              className="inline-flex items-center gap-1 px-3 py-1.5 bg-gray-200 hover:bg-gray-300 text-gray-800 rounded-lg font-bold text-xs transition-colors"
                            >
                              <XCircle className="w-3.5 h-3.5" />
                              <span>Từ Chối</span>
                            </button>
                          </>
                        )}

                        {shop.status === 'APPROVED' && (
                          <button
                            onClick={() => handleUpdateStatus(shop.id, 'LOCKED', shop.name)}
                            disabled={actionLoading === shop.id}
                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 rounded-lg font-bold text-xs transition-colors"
                          >
                            <Lock className="w-3.5 h-3.5" />
                            <span>Khóa Shop</span>
                          </button>
                        )}

                        {(shop.status === 'LOCKED' || shop.status === 'REJECTED') && (
                          <button
                            onClick={() => handleUpdateStatus(shop.id, 'APPROVED', shop.name)}
                            disabled={actionLoading === shop.id}
                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 rounded-lg font-bold text-xs transition-colors"
                          >
                            <Unlock className="w-3.5 h-3.5" />
                            <span>Mở Khóa</span>
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

