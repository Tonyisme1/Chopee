import React, { useState, useEffect } from 'react';
import { X, Ticket, AlertCircle } from 'lucide-react';
import { useVoucherStore } from '../stores/useVoucherStore';
import { Voucher } from '../types';

interface VoucherModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApply: (voucher: Voucher) => void;
  shopId?: number;
  shopName?: string;
  orderAmount: number;
  selectedCode?: string;
}

export const VoucherModal: React.FC<VoucherModalProps> = ({
  isOpen,
  onClose,
  onApply,
  shopId,
  shopName,
  orderAmount,
  selectedCode,
}) => {
  const {
    platformVouchers,
    shopVouchers,
    fetchPlatformVouchers,
    fetchShopVouchers,
    validateVoucher,
  } = useVoucherStore();

  const [inputCode, setInputCode] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [checking, setChecking] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setErrorMsg(null);
      if (shopId) {
        fetchShopVouchers(shopId);
      } else {
        fetchPlatformVouchers();
      }
    }
  }, [isOpen, shopId, fetchShopVouchers, fetchPlatformVouchers]);

  if (!isOpen) return null;

  const currentVouchers: Voucher[] = shopId
    ? shopVouchers[shopId] || []
    : platformVouchers;

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  const handleApplyCustomCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputCode.trim()) return;

    setErrorMsg(null);
    setChecking(true);
    try {
      const res = await validateVoucher(inputCode.trim(), orderAmount, shopId);
      if (res.isValid) {
        // Build mock Voucher object
        const appliedVoucher: Voucher = {
          id: 0,
          code: res.code,
          discountType: 'FIXED_AMOUNT',
          discountValue: res.discountAmount,
          isValid: true,
          shopId,
          shopName,
        };
        onApply(appliedVoucher);
        onClose();
      } else {
        setErrorMsg(res.message || 'Mã giảm giá không hợp lệ hoặc chưa đạt điều kiện');
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Mã giảm giá không hợp lệ');
    } finally {
      setChecking(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white rounded-2xl shadow-2xl border border-gray-100 max-w-lg w-full max-h-[85vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Ticket className="w-5 h-5 text-chopee-orange" />
            <h3 className="font-bold text-gray-900 text-sm">
              {shopId ? `Mã Giảm Giá Shop: ${shopName || ''}` : 'Mã Khuyến Mãi Toàn Sàn Chopee'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Input Promo Code Bar */}
        <div className="p-4 border-b border-gray-100 bg-gray-50/60">
          <form onSubmit={handleApplyCustomCode} className="flex gap-2">
            <input
              type="text"
              value={inputCode}
              onChange={(e) => setInputCode(e.target.value.toUpperCase())}
              placeholder="Nhập mã giảm giá..."
              className="flex-1 px-3 py-2 text-xs bg-white border border-gray-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-orange-500 uppercase font-mono"
            />
            <button
              type="submit"
              disabled={checking || !inputCode.trim()}
              className="px-4 py-2 bg-chopee-orange hover:bg-orange-600 disabled:opacity-50 text-white font-bold text-xs rounded-xl transition-colors shadow-sm"
            >
              {checking ? 'Kiểm tra...' : 'Áp Dụng'}
            </button>
          </form>

          {errorMsg && (
            <div className="mt-2 text-[11px] text-red-600 flex items-center gap-1">
              <AlertCircle className="w-3.5 h-3.5 flex-shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}
        </div>

        {/* Voucher List */}
        <div className="p-4 overflow-y-auto flex-1 space-y-3">
          <p className="text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
            Mã Ưu Đãi Khả Dụng
          </p>

          {currentVouchers.length === 0 ? (
            <div className="text-center py-8 text-gray-400">
              <p className="text-xs">Hiện chưa có mã giảm giá công khai nào.</p>
            </div>
          ) : (
            currentVouchers.map((v) => {
              const isSelected = selectedCode === v.code;
              const isEligible = !v.minOrderAmount || orderAmount >= v.minOrderAmount;

              return (
                <div
                  key={v.id || v.code}
                  className={`p-3.5 rounded-xl border flex items-center justify-between gap-3 transition-all ${
                    !isEligible
                      ? 'border-gray-100 bg-gray-50 opacity-60'
                      : isSelected
                      ? 'border-orange-500 bg-orange-50/40 shadow-sm'
                      : 'border-gray-200 bg-white hover:border-orange-200'
                  }`}
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-black text-xs text-gray-900 font-mono tracking-wider bg-orange-100/70 text-chopee-orange px-2 py-0.5 rounded">
                        {v.code}
                      </span>
                      <span className="font-bold text-xs text-gray-800">
                        {v.discountType === 'PERCENT'
                          ? `Giảm ${v.discountValue}%`
                          : `Giảm ${formatCurrency(v.discountValue)}`}
                      </span>
                    </div>

                    <p className="text-[11px] text-gray-500">
                      {v.minOrderAmount
                        ? `Đơn tối thiểu ${formatCurrency(v.minOrderAmount)}`
                        : 'Không giới hạn giá trị đơn'}
                      {v.maxDiscountAmount
                        ? ` • Tối đa ${formatCurrency(v.maxDiscountAmount)}`
                        : ''}
                    </p>

                    {!isEligible && v.minOrderAmount && (
                      <p className="text-[10px] text-red-500 font-medium">
                        Cần mua thêm {formatCurrency(v.minOrderAmount - orderAmount)} để áp dụng
                      </p>
                    )}
                  </div>

                  <button
                    type="button"
                    disabled={!isEligible}
                    onClick={() => {
                      onApply(v);
                      onClose();
                    }}
                    className={`px-4 py-1.5 text-xs font-bold rounded-lg transition-colors disabled:cursor-not-allowed ${
                      isSelected
                        ? 'bg-chopee-orange text-white'
                        : isEligible
                        ? 'bg-orange-100 text-chopee-orange hover:bg-chopee-orange hover:text-white'
                        : 'bg-gray-200 text-gray-400'
                    }`}
                  >
                    {isSelected ? 'Đang dùng' : 'Dùng'}
                  </button>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
};
export default VoucherModal;
