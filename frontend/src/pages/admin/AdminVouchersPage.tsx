import React, { useEffect, useState, useCallback } from 'react';
import {
  Tag,
  Plus,
  RefreshCw,
  AlertCircle,
  Calendar,
  Percent,
  DollarSign,
  Users,
  X,
  CheckCircle,
  ShieldCheck,
} from 'lucide-react';
import { voucherApi } from '../../services/api';
import { Voucher } from '../../types';

export const AdminVouchersPage: React.FC = () => {
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modal create state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createSubmitting, setCreateSubmitting] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    code: '',
    name: '',
    discountType: 'FIXED_AMOUNT',
    discountValue: 20000,
    minOrderAmount: 100000,
    maxDiscountAmount: 50000,
    usageLimit: 500,
    startDate: new Date().toISOString().slice(0, 10),
    endDate: new Date(Date.now() + 60 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10),
  });

  const fetchVouchers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await voucherApi.getPlatformVouchers();
      if (res.success && res.data) {
        setVouchers(res.data);
      }
    } catch (err: any) {
      console.error('Lỗi tải danh sách voucher sàn:', err);
      setError(err.response?.data?.message || 'Không thể tải danh sách voucher sàn');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchVouchers();
  }, [fetchVouchers]);

  const handleCreateVoucher = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateSubmitting(true);
    setCreateError(null);

    try {
      const payload = {
        code: formData.code.toUpperCase().trim(),
        name: formData.name.trim(),
        discountType: formData.discountType,
        discountValue: Number(formData.discountValue),
        minOrderAmount: Number(formData.minOrderAmount) || 0,
        maxDiscountAmount:
          formData.discountType === 'PERCENT'
            ? Number(formData.maxDiscountAmount) || null
            : null,
        usageLimit: Number(formData.usageLimit) || 500,
        startDate: formData.startDate ? `${formData.startDate}T00:00:00` : null,
        endDate: formData.endDate ? `${formData.endDate}T23:59:59` : null,
      };

      const res = await voucherApi.createAdminVoucher(payload);
      if (res.success) {
        setShowCreateModal(false);
        setFormData({
          code: '',
          name: '',
          discountType: 'FIXED_AMOUNT',
          discountValue: 20000,
          minOrderAmount: 100000,
          maxDiscountAmount: 50000,
          usageLimit: 500,
          startDate: new Date().toISOString().slice(0, 10),
          endDate: new Date(Date.now() + 60 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10),
        });
        await fetchVouchers();
      }
    } catch (err: any) {
      console.error('Lỗi tạo mã giảm giá sàn:', err);
      setCreateError(err.response?.data?.message || 'Không thể tạo mã giảm giá sàn');
    } finally {
      setCreateSubmitting(false);
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('vi-VN');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">
            Mã Khuyến Mãi Toàn Sàn
          </h1>
          <p className="text-xs text-gray-500 mt-1">
            Quản lý các chương trình trợ giá, mã giảm giá mở bán và voucher toàn sàn Chopee.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={fetchVouchers}
            disabled={loading}
            className="p-2 border border-gray-200 bg-white hover:bg-gray-50 rounded-xl text-gray-700 shadow-sm transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-chopee-orange hover:bg-orange-600 text-white rounded-xl text-xs font-bold shadow-md transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>Tạo Voucher Sàn Mới</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Notice info */}
      <div className="p-4 bg-orange-50/60 border border-orange-200 rounded-2xl flex items-start gap-3 text-xs text-orange-950">
        <ShieldCheck className="w-5 h-5 text-chopee-orange shrink-0 mt-0.5" />
        <div>
          <strong className="font-bold">Quyền hạn Quản Trị Viên Sàn:</strong>
          <p className="text-[11px] text-orange-900 mt-0.5">
            Các mã giảm giá tạo tại trang này có phạm vi áp dụng toàn sàn (áp dụng trên tổng giá trị
            đơn hàng từ mọi gian hàng), ngân sách trợ giá do Chopee bảo trợ.
          </p>
        </div>
      </div>

      {/* Voucher Grid */}
      {loading ? (
        <div className="min-h-[300px] flex items-center justify-center text-gray-400">
          <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
        </div>
      ) : vouchers.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center shadow-sm">
          <Tag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-sm font-bold text-gray-700">Chưa có mã giảm giá toàn sàn nào</p>
          <p className="text-xs text-gray-400 mt-1">
            Bấm "Tạo Voucher Sàn Mới" để kích hoạt chiến dịch khuyến mãi đầu tiên!
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {vouchers.map((v) => (
            <div
              key={v.id}
              className="bg-white rounded-2xl border border-gray-100 p-5 shadow-sm space-y-4 hover:shadow-md transition-shadow relative overflow-hidden"
            >
              {/* Badge type */}
              <div className="flex items-center justify-between">
                <span className="font-mono font-black text-sm px-2.5 py-1 bg-blue-50 text-blue-700 rounded-lg border border-blue-200">
                  {v.code}
                </span>
                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    v.isValid
                      ? 'bg-emerald-50 text-emerald-600 border border-emerald-200'
                      : 'bg-gray-100 text-gray-500'
                  }`}
                >
                  {v.isValid ? 'Đang hoạt động' : 'Hết hạn / Đã dừng'}
                </span>
              </div>

              <div>
                <h3 className="text-sm font-bold text-gray-900">{v.name || v.code}</h3>
                <p className="text-xs text-chopee-orange font-bold mt-1">
                  {v.discountType === 'PERCENT'
                    ? `Giảm ${v.discountValue}% ${
                        v.maxDiscountAmount ? `(Tối đa ${formatCurrency(v.maxDiscountAmount)})` : ''
                      }`
                    : `Giảm ${formatCurrency(v.discountValue)}`}
                </p>
              </div>

              <div className="space-y-1.5 pt-3 border-t border-gray-100 text-[11px] text-gray-500">
                <div className="flex items-center justify-between">
                  <span>Đơn tối thiểu:</span>
                  <strong className="text-gray-800">{formatCurrency(v.minOrderAmount || 0)}</strong>
                </div>
                <div className="flex items-center justify-between">
                  <span>Lượt sử dụng:</span>
                  <strong className="text-gray-800">
                    {v.usedCount || 0} / {v.usageLimit || 'Không giới hạn'}
                  </strong>
                </div>
                <div className="flex items-center gap-1.5 pt-1 text-gray-400">
                  <Calendar className="w-3.5 h-3.5" />
                  <span>
                    {formatDate(v.startDate)} - {formatDate(v.endDate)}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal Create Platform Voucher */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-5 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3">
              <div className="flex items-center gap-2">
                <Tag className="w-5 h-5 text-chopee-orange" />
                <h3 className="text-base font-bold text-gray-900">Tạo Mã Giảm Giá Toàn Sàn</h3>
              </div>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-400 hover:text-gray-600 p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {createError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{createError}</span>
              </div>
            )}

            <form onSubmit={handleCreateVoucher} className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-gray-700 mb-1">Mã Voucher (Code) *</label>
                  <input
                    type="text"
                    required
                    placeholder="VD: FREESHIPCHO"
                    value={formData.code}
                    onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-200 rounded-xl uppercase font-mono font-bold focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                  />
                </div>
                <div>
                  <label className="block font-bold text-gray-700 mb-1">Tên chương trình *</label>
                  <input
                    type="text"
                    required
                    placeholder="VD: Khuyến mãi mừng khai trương"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-gray-700 mb-1">Loại giảm giá</label>
                  <select
                    value={formData.discountType}
                    onChange={(e) => setFormData({ ...formData, discountType: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-200 rounded-xl bg-white focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                  >
                    <option value="FIXED_AMOUNT">Số tiền cố định (đ)</option>
                    <option value="PERCENT">Theo phần trăm (%)</option>
                  </select>
                </div>
                <div>
                  <label className="block font-bold text-gray-700 mb-1">
                    Giá trị giảm * ({formData.discountType === 'PERCENT' ? '%' : 'đ'})
                  </label>
                  <div className="relative">
                    <input
                      type="number"
                      min={1}
                      required
                      value={formData.discountValue}
                      onChange={(e) =>
                        setFormData({ ...formData, discountValue: Number(e.target.value) })
                      }
                      className="w-full pl-8 pr-3 py-2 border border-gray-200 rounded-xl font-bold focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                    />
                    <div className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400">
                      {formData.discountType === 'PERCENT' ? (
                        <Percent className="w-3.5 h-3.5" />
                      ) : (
                        <DollarSign className="w-3.5 h-3.5" />
                      )}
                    </div>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-gray-700 mb-1">
                    Đơn hàng tối thiểu (đ)
                  </label>
                  <input
                    type="number"
                    min={0}
                    step={10000}
                    value={formData.minOrderAmount}
                    onChange={(e) =>
                      setFormData({ ...formData, minOrderAmount: Number(e.target.value) })
                    }
                    className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                  />
                </div>
                {formData.discountType === 'PERCENT' && (
                  <div>
                    <label className="block font-bold text-gray-700 mb-1">Giảm tối đa (đ)</label>
                    <input
                      type="number"
                      min={0}
                      step={10000}
                      value={formData.maxDiscountAmount}
                      onChange={(e) =>
                        setFormData({ ...formData, maxDiscountAmount: Number(e.target.value) })
                      }
                      className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                    />
                  </div>
                )}
                {formData.discountType !== 'PERCENT' && (
                  <div>
                    <label className="block font-bold text-gray-700 mb-1">Tổng lượt dùng</label>
                    <div className="relative">
                      <input
                        type="number"
                        min={1}
                        value={formData.usageLimit}
                        onChange={(e) =>
                          setFormData({ ...formData, usageLimit: Number(e.target.value) })
                        }
                        className="w-full pl-8 pr-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                      />
                      <Users className="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
                    </div>
                  </div>
                )}
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-gray-700 mb-1">Ngày bắt đầu</label>
                  <input
                    type="date"
                    required
                    value={formData.startDate}
                    onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                  />
                </div>
                <div>
                  <label className="block font-bold text-gray-700 mb-1">Ngày kết thúc</label>
                  <input
                    type="date"
                    required
                    value={formData.endDate}
                    onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border border-gray-200 rounded-xl text-gray-600 hover:bg-gray-50 font-bold transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={createSubmitting}
                  className="px-5 py-2 bg-chopee-orange hover:bg-orange-600 text-white rounded-xl font-bold shadow-md transition-colors flex items-center gap-1.5"
                >
                  {createSubmitting ? (
                    <RefreshCw className="w-4 h-4 animate-spin" />
                  ) : (
                    <CheckCircle className="w-4 h-4" />
                  )}
                  <span>Xác Nhận Tạo</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
