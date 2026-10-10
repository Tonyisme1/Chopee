import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import {
  MapPin,
  Store,
  Truck,
  Ticket,
  CreditCard,
  Banknote,
  ArrowRight,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';
import { useCartStore, ShopCartGroup } from '../stores/useCartStore';
import { useAddressStore } from '../stores/useAddressStore';
import { useAuthStore } from '../stores/useAuthStore';
import { orderApi, paymentApi } from '../services/api';
import { CheckoutPreviewResponse, UserAddress, Voucher } from '../types';
import { AddressModal } from '../components/AddressModal';
import { VoucherModal } from '../components/VoucherModal';

export const CheckoutPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { items, fetchCart } = useCartStore();
  const { defaultAddress, selectedAddress, selectAddress } = useAddressStore();
  const { isAuthenticated } = useAuthStore();

  const selectedItemIdsFromNav = (location.state as any)?.selectedItemIds as number[] | undefined;

  // Filter items selected for checkout
  const checkoutItems = useMemo(() => {
    if (selectedItemIdsFromNav && selectedItemIdsFromNav.length > 0) {
      const filtered = items.filter((i) => selectedItemIdsFromNav.includes(i.id));
      return filtered.length > 0 ? filtered : items;
    }
    return items;
  }, [items, selectedItemIdsFromNav]);

  // Group checkout items by shop
  const checkoutShopGroups = useMemo(() => {
    const groupMap = new Map<number, ShopCartGroup>();
    checkoutItems.forEach((item) => {
      if (!groupMap.has(item.shopId)) {
        groupMap.set(item.shopId, {
          shopId: item.shopId,
          shopName: item.shopName,
          items: [],
          subtotal: 0,
        });
      }
      const group = groupMap.get(item.shopId)!;
      group.items.push(item);
      group.subtotal += item.subtotal;
    });
    return Array.from(groupMap.values());
  }, [checkoutItems]);

  // Modals state
  const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
  const [activeVoucherShopId, setActiveVoucherShopId] = useState<number | null>(null);
  const [isPlatformVoucherOpen, setIsPlatformVoucherOpen] = useState(false);

  // Vouchers state
  const [shopVouchers, setShopVouchers] = useState<Record<number, Voucher>>({});
  const [platformVoucher, setPlatformVoucher] = useState<Voucher | null>(null);

  // Shipping & Payment Methods
  const [shippingMethod, setShippingMethod] = useState<'STANDARD' | 'EXPRESS_FRESH'>('STANDARD');
  const [paymentMethod, setPaymentMethod] = useState<'COD' | 'VNPAY'>('COD');
  const [note, setNote] = useState('');

  // Checkout Preview State
  const [preview, setPreview] = useState<CheckoutPreviewResponse | null>(null);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [submittingOrder, setSubmittingOrder] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const activeAddress: UserAddress | null = selectedAddress || defaultAddress;

  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
    }
  }, [isAuthenticated, fetchCart]);

  // If any item is FRESH, default shippingMethod to EXPRESS_FRESH
  useEffect(() => {
    const hasFresh = checkoutItems.some((i) => i.storageType === 'FRESH');
    if (hasFresh) {
      setShippingMethod('EXPRESS_FRESH');
    }
  }, [checkoutItems]);

  // Recalculate checkout preview whenever items, vouchers or shipping changes
  const calculatePreview = useCallback(async () => {
    if (checkoutItems.length === 0) return;
    setLoadingPreview(true);
    setErrorMsg(null);

    const shopVoucherCodes: Record<number, string> = {};
    Object.entries(shopVouchers).forEach(([sId, v]) => {
      if (v?.code) {
        shopVoucherCodes[Number(sId)] = v.code;
      }
    });

    try {
      const res = await orderApi.checkoutPreview({
        cartItemIds: checkoutItems.map((i) => i.id),
        shippingMethod,
        paymentMethod,
        voucherCode: platformVoucher?.code || undefined,
        shopVoucherCodes,
        platformVoucherCode: platformVoucher?.code || undefined,
      });

      if (res.success && res.data) {
        setPreview(res.data);
      }
    } catch (err: any) {
      console.warn('Lỗi tính toán xem trước đơn hàng từ máy chủ:', err);
    } finally {
      setLoadingPreview(false);
    }
  }, [checkoutItems, shippingMethod, paymentMethod, shopVouchers, platformVoucher]);

  useEffect(() => {
    calculatePreview();
  }, [calculatePreview]);

  // Reactive price calculations guaranteeing no 0đ state
  const shippingFeePerShop = shippingMethod === 'EXPRESS_FRESH' ? 25000 : 15000;
  const itemsTotal = preview?.groupSubtotal ?? preview?.totalItemsAmount ?? checkoutItems.reduce((acc, i) => acc + i.subtotal, 0);
  const totalShipping = preview?.totalShippingFee ?? (checkoutShopGroups.length * shippingFeePerShop);

  // Shop vouchers discount calculation
  const calculatedShopDiscount = useMemo(() => {
    let totalDiscount = 0;
    checkoutShopGroups.forEach((group) => {
      const v = shopVouchers[group.shopId];
      if (v) {
        if (v.discountType === 'PERCENT') {
          let d = (group.subtotal * (v.discountValue || 0)) / 100;
          if (v.maxDiscountAmount && d > v.maxDiscountAmount) {
            d = v.maxDiscountAmount;
          }
          totalDiscount += Math.min(d, group.subtotal);
        } else {
          totalDiscount += Math.min(v.discountValue || 0, group.subtotal);
        }
      }
    });
    return totalDiscount;
  }, [checkoutShopGroups, shopVouchers]);

  // Platform voucher discount calculation
  const calculatedPlatformDiscount = useMemo(() => {
    if (!platformVoucher) return 0;
    const baseAmount = Math.max(0, itemsTotal - calculatedShopDiscount);
    if (baseAmount <= 0) return 0;
    if (platformVoucher.discountType === 'PERCENT') {
      let d = (baseAmount * (platformVoucher.discountValue || 0)) / 100;
      if (platformVoucher.maxDiscountAmount && d > platformVoucher.maxDiscountAmount) {
        d = platformVoucher.maxDiscountAmount;
      }
      return Math.min(d, baseAmount);
    }
    return Math.min(platformVoucher.discountValue || 0, baseAmount);
  }, [itemsTotal, calculatedShopDiscount, platformVoucher]);

  const totalDiscount = preview?.totalDiscount ?? preview?.totalDiscountAmount ?? (calculatedShopDiscount + calculatedPlatformDiscount);
  const finalTotalAmount = preview?.finalTotalAmount ?? preview?.grandFinalAmount ?? Math.max(0, itemsTotal + totalShipping - totalDiscount);

  if (!isAuthenticated) {
    navigate('/login?redirect=/checkout');
    return null;
  }

  if (checkoutItems.length === 0) {
    return (
      <div className="min-h-[400px] flex flex-col items-center justify-center p-8 bg-white rounded-2xl border border-gray-100 text-center">
        <h2 className="text-lg font-bold text-gray-800 mb-2">Giỏ hàng trống</h2>
        <p className="text-xs text-gray-500 mb-4">Vui lòng chọn sản phẩm trước khi thanh toán.</p>
        <Link to="/" className="px-5 py-2.5 bg-chopee-orange text-white text-xs font-bold rounded-xl">
          Quay lại mua sắm
        </Link>
      </div>
    );
  }

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  const handleCreateOrder = async () => {
    if (!activeAddress) {
      alert('Vui lòng chọn hoặc thêm địa chỉ nhận hàng!');
      setIsAddressModalOpen(true);
      return;
    }

    setSubmittingOrder(true);
    setErrorMsg(null);

    const shopVoucherCodes: Record<number, string> = {};
    Object.entries(shopVouchers).forEach(([sId, v]) => {
      if (v?.code) {
        shopVoucherCodes[Number(sId)] = v.code;
      }
    });

    try {
      const fullAddressString = `${activeAddress.detailAddress}, ${activeAddress.ward}, ${activeAddress.district}, ${activeAddress.province}`;

      const res = await orderApi.createOrder({
        shippingName: activeAddress.receiverName,
        shippingPhone: activeAddress.phone,
        shippingAddress: fullAddressString,
        paymentMethod,
        shippingMethod,
        note: note.trim() || undefined,
        cartItemIds: checkoutItems.map((i) => i.id),
        shopVoucherCodes,
        platformVoucherCode: platformVoucher?.code || undefined,
      });

      if (res.success && res.data) {
        const { groupOrderCode } = res.data;
        await fetchCart();

        if (paymentMethod === 'VNPAY') {
          const amountToPay = finalTotalAmount > 0 ? finalTotalAmount : 100000;
          const vnpRes = await paymentApi.createVNPayPayment({
            groupOrderCode,
            amount: amountToPay,
            orderInfo: `Thanh toan don hang Chopee ${groupOrderCode}`,
          });

          if (vnpRes.success && vnpRes.data?.paymentUrl) {
            window.location.href = vnpRes.data.paymentUrl;
            return;
          }
        }

        // COD or fallback -> Success Page
        navigate(`/orders/success?groupOrderCode=${groupOrderCode}`);
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Đặt hàng thất bại. Vui lòng thử lại sau.');
    } finally {
      setSubmittingOrder(false);
    }
  };

  return (
    <div className="space-y-6 pb-20">
      <h1 className="text-2xl font-black text-gray-900 tracking-tight">Thanh Toán Đơn Hàng</h1>

      {errorMsg && (
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* 1. Delivery Address Card */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm">
        <div className="flex items-center justify-between mb-3 border-b border-gray-100 pb-3">
          <div className="flex items-center gap-2 text-chopee-orange font-bold text-sm">
            <MapPin className="w-5 h-5" />
            <span>Địa Chỉ Nhận Hàng</span>
          </div>
          <button
            onClick={() => setIsAddressModalOpen(true)}
            className="text-xs text-blue-600 font-semibold hover:underline"
          >
            {activeAddress ? 'Thay đổi' : '+ Thêm địa chỉ'}
          </button>
        </div>

        {activeAddress ? (
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-2 text-xs">
            <div className="space-y-1">
              <span className="font-extrabold text-gray-900 text-sm">
                {activeAddress.receiverName} ({activeAddress.phone})
              </span>
              <p className="text-gray-600">
                {activeAddress.detailAddress}, {activeAddress.ward}, {activeAddress.district}, {activeAddress.province}
              </p>
            </div>
            {activeAddress.isDefault && (
              <span className="text-[10px] bg-orange-50 text-chopee-orange font-bold px-2 py-0.5 rounded border border-orange-200 self-start md:self-auto">
                Mặc định
              </span>
            )}
          </div>
        ) : (
          <div className="text-xs text-gray-400 py-2">
            Chưa chọn địa chỉ giao hàng. Nhấp "Thêm địa chỉ" để tiếp tục.
          </div>
        )}
      </div>

      {/* 2. Order Breakdown Per Shop */}
      <div className="space-y-4">
        {checkoutShopGroups.map((group) => {
          const appliedShopVoucher = shopVouchers[group.shopId];

          return (
            <div
              key={group.shopId}
              className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden"
            >
              <div className="p-4 bg-gray-50 border-b border-gray-100 flex items-center justify-between text-xs">
                <div className="flex items-center gap-2 font-bold text-gray-900">
                  <Store className="w-4 h-4 text-chopee-orange" />
                  <span>{group.shopName}</span>
                </div>
                <span className="text-[11px] text-gray-500">Đơn tách riêng #{group.shopId}</span>
              </div>

              {/* Items */}
              <div className="divide-y divide-gray-50 p-4">
                {group.items.map((item) => (
                  <div key={item.id} className="py-2.5 flex items-center justify-between text-xs">
                    <div className="flex items-center gap-3">
                      <img
                        src={item.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e'}
                        alt={item.productName}
                        className="w-12 h-12 object-cover rounded-lg border border-gray-100"
                      />
                      <div>
                        <p className="font-semibold text-gray-800 line-clamp-1">{item.productName}</p>
                        {item.variantName && (
                          <p className="text-[11px] text-chopee-orange font-medium">
                            Phân loại: {item.variantName}
                          </p>
                        )}
                        <p className="text-[10px] text-gray-400">
                          {Math.round(item.quantity)} {item.unit} x {formatCurrency(item.sellingPrice)}
                        </p>
                      </div>
                    </div>
                    <span className="font-bold text-gray-900">{formatCurrency(item.subtotal)}</span>
                  </div>
                ))}
              </div>

              {/* Shop Voucher Trigger */}
              <div className="p-4 bg-gray-50/50 border-t border-gray-100 flex items-center justify-between text-xs">
                <div className="flex items-center gap-2 text-gray-700">
                  <Ticket className="w-4 h-4 text-chopee-orange" />
                  <span>Voucher của Shop:</span>
                  {appliedShopVoucher ? (
                    <span className="font-bold text-chopee-orange bg-orange-100 px-2 py-0.5 rounded font-mono">
                      {appliedShopVoucher.code} (-
                      {appliedShopVoucher.discountType === 'PERCENT'
                        ? `${appliedShopVoucher.discountValue}%`
                        : formatCurrency(appliedShopVoucher.discountValue)}
                      )
                    </span>
                  ) : (
                    <span className="text-gray-400">Chưa chọn mã</span>
                  )}
                </div>

                <button
                  type="button"
                  onClick={() => setActiveVoucherShopId(group.shopId)}
                  className="text-xs text-chopee-orange font-bold hover:underline"
                >
                  {appliedShopVoucher ? 'Đổi mã' : 'Chọn Voucher'}
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* 3. Shipping & Platform Voucher Controls */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Shipping Method */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm space-y-3">
          <h3 className="text-sm font-bold text-gray-900 flex items-center gap-2">
            <Truck className="w-4 h-4 text-emerald-600" />
            <span>Phương Thức Vận Chuyển</span>
          </h3>

          <div className="space-y-2 text-xs">
            <label className="p-3 border rounded-xl flex items-center justify-between cursor-pointer hover:bg-gray-50 transition-colors">
              <div className="flex items-center gap-2.5">
                <input
                  type="radio"
                  name="shipping"
                  checked={shippingMethod === 'STANDARD'}
                  onChange={() => setShippingMethod('STANDARD')}
                  className="text-chopee-orange focus:ring-orange-500"
                />
                <div>
                  <span className="font-bold text-gray-800 block">Giao Hàng Tiêu Chuẩn</span>
                  <span className="text-gray-500 text-[11px]">Nhận hàng sau 2-3 ngày</span>
                </div>
              </div>
              <span className="font-bold text-gray-700">15.000 đ/shop</span>
            </label>

            <label className="p-3 border border-emerald-200 bg-emerald-50/30 rounded-xl flex items-center justify-between cursor-pointer hover:bg-emerald-50 transition-colors">
              <div className="flex items-center gap-2.5">
                <input
                  type="radio"
                  name="shipping"
                  checked={shippingMethod === 'EXPRESS_FRESH'}
                  onChange={() => setShippingMethod('EXPRESS_FRESH')}
                  className="text-chopee-orange focus:ring-orange-500"
                />
                <div>
                  <span className="font-bold text-emerald-800 block">Chopee Express Fresh (2 Giờ)</span>
                  <span className="text-emerald-600 text-[11px]">Đặc quyền cho rau củ tươi & đồ ăn</span>
                </div>
              </div>
              <span className="font-bold text-emerald-800">25.000 đ/shop</span>
            </label>
          </div>
        </div>

        {/* Platform Voucher & Note */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm space-y-4">
          <h3 className="text-sm font-bold text-gray-900 flex items-center gap-2">
            <Ticket className="w-4 h-4 text-chopee-orange" />
            <span>Voucher Toàn Sàn Chopee</span>
          </h3>

          <div className="p-3 border border-dashed border-orange-300 rounded-xl bg-orange-50/40 flex items-center justify-between text-xs">
            <div>
              {platformVoucher ? (
                <div className="flex items-center gap-2">
                  <span className="font-black text-chopee-orange bg-orange-100 px-2 py-0.5 rounded font-mono">
                    {platformVoucher.code}
                  </span>
                  <span className="text-gray-700 font-bold">
                    (-{formatCurrency(platformVoucher.discountValue)})
                  </span>
                </div>
              ) : (
                <span className="text-gray-500">Áp dụng mã giảm giá toàn sàn để tiết kiệm thêm</span>
              )}
            </div>

            <button
              type="button"
              onClick={() => setIsPlatformVoucherOpen(true)}
              className="text-xs text-chopee-orange font-bold hover:underline"
            >
              {platformVoucher ? 'Đổi mã' : 'Chọn Voucher'}
            </button>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">
              Lời nhắn cho người bán (Tùy chọn)
            </label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Ví dụ: Giao giờ hành chính, gọi trước khi đến..."
              className="w-full p-2 bg-gray-50 border border-gray-200 rounded-xl text-xs focus:outline-none"
            />
          </div>
        </div>
      </div>

      {/* 4. Payment Method Selection */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm space-y-3">
        <h3 className="text-sm font-bold text-gray-900">Phương Thức Thanh Toán</h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
          <label className={`p-4 border rounded-xl flex items-center gap-3 cursor-pointer transition-all ${
            paymentMethod === 'COD' ? 'border-orange-500 bg-orange-50/40 font-bold' : 'border-gray-200'
          }`}>
            <input
              type="radio"
              name="payment"
              checked={paymentMethod === 'COD'}
              onChange={() => setPaymentMethod('COD')}
              className="text-chopee-orange focus:ring-orange-500"
            />
            <Banknote className="w-5 h-5 text-emerald-600" />
            <div>
              <p className="text-gray-800">Thanh toán khi nhận hàng (COD)</p>
              <p className="text-[11px] text-gray-400 font-normal">Thanh toán tiền mặt cho shipper</p>
            </div>
          </label>

          <label className={`p-4 border rounded-xl flex items-center gap-3 cursor-pointer transition-all ${
            paymentMethod === 'VNPAY' ? 'border-orange-500 bg-orange-50/40 font-bold' : 'border-gray-200'
          }`}>
            <input
              type="radio"
              name="payment"
              checked={paymentMethod === 'VNPAY'}
              onChange={() => setPaymentMethod('VNPAY')}
              className="text-chopee-orange focus:ring-orange-500"
            />
            <CreditCard className="w-5 h-5 text-blue-600" />
            <div>
              <p className="text-gray-800">VNPay Sandbox QR / Thẻ ATM</p>
              <p className="text-[11px] text-gray-400 font-normal">Quét mã VNPAY-QR hoặc thẻ nội địa</p>
            </div>
          </label>
        </div>
      </div>

      {/* 5. Cost Summary & Final CTA */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm space-y-3 text-xs">
        <div className="flex justify-between text-gray-600">
          <span>Tổng tiền hàng ({checkoutItems.length} món):</span>
          <span className="font-semibold text-gray-800">{formatCurrency(itemsTotal)}</span>
        </div>
        <div className="flex justify-between text-gray-600">
          <span>Phí vận chuyển ({checkoutShopGroups.length} shop):</span>
          <span className="font-semibold text-gray-800">+{formatCurrency(totalShipping)}</span>
        </div>
        {totalDiscount > 0 && (
          <div className="flex justify-between text-emerald-600 font-medium">
            <span>Tổng giảm giá voucher:</span>
            <span>-{formatCurrency(totalDiscount)}</span>
          </div>
        )}
        <div className="pt-3 border-t border-gray-100 flex items-baseline justify-between">
          <span className="text-sm font-bold text-gray-800">Tổng thanh toán:</span>
          <span className="text-2xl font-black text-chopee-orange">
            {formatCurrency(finalTotalAmount)}
          </span>
        </div>
      </div>

      {/* Action Button */}
      <div className="flex justify-end">
        <button
          onClick={handleCreateOrder}
          disabled={submittingOrder || loadingPreview}
          className="py-3.5 px-10 bg-chopee-orange hover:bg-orange-600 text-white font-black text-sm rounded-xl shadow-xl shadow-orange-500/25 transition-all flex items-center gap-2 disabled:opacity-60"
        >
          {submittingOrder ? (
            <>
              <RefreshCw className="w-4 h-4 animate-spin" />
              <span>Đang xử lý đơn hàng...</span>
            </>
          ) : (
            <>
              <span>Đặt Hàng Ngay</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </div>

      {/* Modals */}
      <AddressModal
        isOpen={isAddressModalOpen}
        onClose={() => setIsAddressModalOpen(false)}
        onSelect={(addr) => selectAddress(addr)}
        selectedId={activeAddress?.id}
      />

      {activeVoucherShopId && (
        <VoucherModal
          isOpen={!!activeVoucherShopId}
          onClose={() => setActiveVoucherShopId(null)}
          shopId={activeVoucherShopId}
          shopName={checkoutShopGroups.find((g) => g.shopId === activeVoucherShopId)?.shopName}
          orderAmount={
            checkoutShopGroups.find((g) => g.shopId === activeVoucherShopId)?.subtotal || 0
          }
          selectedCode={shopVouchers[activeVoucherShopId]?.code}
          onApply={(v) => {
            setShopVouchers((prev) => ({ ...prev, [activeVoucherShopId]: v }));
          }}
        />
      )}

      {isPlatformVoucherOpen && (
        <VoucherModal
          isOpen={isPlatformVoucherOpen}
          onClose={() => setIsPlatformVoucherOpen(false)}
          orderAmount={itemsTotal}
          selectedCode={platformVoucher?.code}
          onApply={(v) => setPlatformVoucher(v)}
        />
      )}
    </div>
  );
};
export default CheckoutPage;
