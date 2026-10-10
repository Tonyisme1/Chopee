import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  ShoppingBag,
  Store,
  Trash2,
  Plus,
  Minus,
  ArrowRight,
  ShieldCheck,
  Truck,
  Apple,
  Snowflake,
} from 'lucide-react';
import { useCartStore } from '../stores/useCartStore';
import { useAuthStore } from '../stores/useAuthStore';
import { CartItem } from '../types';

export const CartPage: React.FC = () => {
  const navigate = useNavigate();
  const { items, fetchCart, updateQuantity, removeItem, getItemsByShop } = useCartStore();
  const { isAuthenticated } = useAuthStore();

  const [selectedItemIds, setSelectedItemIds] = useState<Set<number>>(new Set());
  const [hasInitializedSelection, setHasInitializedSelection] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
    }
  }, [isAuthenticated, fetchCart]);

  // When items load or change, select all on initial load, otherwise maintain valid selections
  useEffect(() => {
    if (items.length > 0) {
      if (!hasInitializedSelection) {
        setSelectedItemIds(new Set(items.map((i) => i.id)));
        setHasInitializedSelection(true);
      } else {
        setSelectedItemIds((prev) => {
          const next = new Set<number>();
          items.forEach((i) => {
            if (prev.has(i.id)) {
              next.add(i.id);
            }
          });
          return next;
        });
      }
    } else {
      setSelectedItemIds(new Set());
    }
  }, [items, hasInitializedSelection]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-[400px] flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center">
          <div className="w-16 h-16 bg-orange-100 text-chopee-orange rounded-full flex items-center justify-center mx-auto mb-4">
            <ShoppingBag className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2">Đăng Nhập Để Xem Giỏ Hàng</h2>
          <p className="text-gray-500 text-xs mb-6">
            Đăng nhập để xem danh sách sản phẩm bạn đã thêm và tiếp tục thanh toán đơn hàng.
          </p>
          <Link
            to="/login?redirect=/cart"
            className="w-full inline-block py-2.5 px-4 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-sm rounded-xl transition-colors shadow-sm"
          >
            Đăng Nhập Ngay
          </Link>
        </div>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="min-h-[450px] bg-white rounded-2xl border border-gray-100 p-12 text-center flex flex-col items-center justify-center">
        <div className="w-24 h-24 bg-orange-50 text-chopee-orange rounded-full flex items-center justify-center mb-4">
          <ShoppingBag className="w-12 h-12" />
        </div>
        <h2 className="text-lg font-bold text-gray-800 mb-1">Giỏ hàng của bạn còn trống</h2>
        <p className="text-xs text-gray-500 mb-6 max-w-sm">
          Khám phá ngay hàng ngàn sản phẩm nông sản tươi sống, đồ uống giải khát và thiết bị gia dụng giá tốt!
        </p>
        <Link
          to="/"
          className="px-6 py-3 bg-chopee-orange hover:bg-orange-600 text-white text-xs font-bold rounded-xl shadow-md transition-colors"
        >
          Mua Sắm Ngay
        </Link>
      </div>
    );
  }

  const shopGroups = getItemsByShop();

  const handleToggleItem = (id: number) => {
    setSelectedItemIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleToggleShop = (shopItems: CartItem[]) => {
    const allSelected = shopItems.every((i) => selectedItemIds.has(i.id));
    setSelectedItemIds((prev) => {
      const next = new Set(prev);
      shopItems.forEach((i) => {
        if (allSelected) {
          next.delete(i.id);
        } else {
          next.add(i.id);
        }
      });
      return next;
    });
  };

  const handleToggleAll = () => {
    if (selectedItemIds.size === items.length) {
      setSelectedItemIds(new Set());
    } else {
      setSelectedItemIds(new Set(items.map((i) => i.id)));
    }
  };

  // Calculate totals for selected items
  const selectedItems = items.filter((i) => selectedItemIds.has(i.id));
  const selectedTotalAmount = selectedItems.reduce((sum, item) => sum + item.subtotal, 0);

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  const handleProceedToCheckout = () => {
    if (selectedItems.length === 0) {
      alert('Vui lòng chọn ít nhất 1 sản phẩm để thanh toán!');
      return;
    }
    navigate('/checkout', { state: { selectedItemIds: Array.from(selectedItemIds) } });
  };

  return (
    <div className="space-y-6 pb-20">
      {/* Page Title */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-black text-gray-900 tracking-tight flex items-center gap-2">
          <ShoppingBag className="w-6 h-6 text-chopee-orange" />
          <span>Giỏ Hàng Chopee</span>
        </h1>
        <span className="text-xs text-gray-500 font-medium">
          {items.length} món hàng từ {shopGroups.length} gian hàng
        </span>
      </div>

      {/* Cart Items Header Table */}
      <div className="hidden md:grid grid-cols-12 gap-4 bg-white rounded-xl p-4 text-xs font-semibold text-gray-500 border border-gray-100 shadow-sm">
        <div className="col-span-6 flex items-center gap-3">
          <input
            type="checkbox"
            checked={selectedItemIds.size === items.length && items.length > 0}
            onChange={handleToggleAll}
            className="w-4 h-4 text-chopee-orange rounded focus:ring-orange-500"
          />
          <span>Sản phẩm ({items.length})</span>
        </div>
        <div className="col-span-2 text-center">Đơn giá</div>
        <div className="col-span-2 text-center">Số lượng</div>
        <div className="col-span-2 text-right pr-4">Thành tiền</div>
      </div>

      {/* Cart Groups by Shop */}
      <div className="space-y-4">
        {shopGroups.map((group) => {
          const isShopAllSelected = group.items.every((i) => selectedItemIds.has(i.id));

          return (
            <div
              key={group.shopId}
              className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden"
            >
              {/* Shop Header */}
              <div className="p-4 bg-gray-50/70 border-b border-gray-100 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={isShopAllSelected}
                    onChange={() => handleToggleShop(group.items)}
                    className="w-4 h-4 text-chopee-orange rounded focus:ring-orange-500"
                  />
                  <div className="flex items-center gap-2 font-bold text-gray-800 text-sm">
                    <Store className="w-4 h-4 text-chopee-orange" />
                    <span>{group.shopName}</span>
                  </div>
                  <span className="text-[10px] bg-orange-100/70 text-chopee-orange px-2 py-0.5 rounded font-semibold">
                    Đơn hàng riêng #{group.shopId}
                  </span>
                </div>

                <div className="flex items-center gap-2 text-xs text-emerald-700 font-medium">
                  <Truck className="w-3.5 h-3.5" />
                  <span>Hỗ trợ Chopee Express Fresh</span>
                </div>
              </div>

              {/* Items in this Shop */}
              <div className="divide-y divide-gray-100">
                {group.items.map((item) => {
                  const isChecked = selectedItemIds.has(item.id);

                  return (
                    <div
                      key={item.id}
                      className="p-4 grid grid-cols-1 md:grid-cols-12 gap-4 items-center hover:bg-orange-50/20 transition-colors"
                    >
                      {/* Product details */}
                      <div className="col-span-6 flex items-center gap-3">
                        <input
                          type="checkbox"
                          checked={isChecked}
                          onChange={() => handleToggleItem(item.id)}
                          className="w-4 h-4 text-chopee-orange rounded focus:ring-orange-500"
                        />
                        <img
                          src={item.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e'}
                          alt={item.productName}
                          className="w-16 h-16 object-cover rounded-xl border border-gray-100 flex-shrink-0"
                        />
                        <div className="space-y-1">
                          <Link
                            to={`/products/${item.productId}`}
                            className="text-xs font-bold text-gray-800 hover:text-chopee-orange transition-colors line-clamp-2"
                          >
                            {item.productName}
                          </Link>
                          <div className="flex items-center gap-1.5 flex-wrap">
                            {item.storageType === 'FRESH' && (
                              <span className="text-[10px] bg-emerald-50 text-emerald-700 px-1.5 py-0.2 rounded border border-emerald-200 flex items-center gap-0.5">
                                <Apple className="w-3 h-3" /> Tươi sống
                              </span>
                            )}
                            {item.storageType === 'FROZEN_CHILLED' && (
                              <span className="text-[10px] bg-cyan-50 text-cyan-700 px-1.5 py-0.2 rounded border border-cyan-200 flex items-center gap-0.5">
                                <Snowflake className="w-3 h-3" /> Đông mát
                              </span>
                            )}
                            {item.variantName ? (
                              <span className="text-[11px] font-semibold bg-orange-50 text-chopee-orange px-2 py-0.5 rounded border border-orange-200">
                                Quy cách: {item.variantName}
                              </span>
                            ) : (
                              <span className="text-[10px] text-gray-400">
                                Đơn vị: {item.unit}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>

                      {/* Unit Price */}
                      <div className="col-span-2 text-center text-xs font-semibold text-gray-700">
                        {formatCurrency(item.sellingPrice)}
                      </div>

                      {/* Quantity Modifier (Strictly Integer) */}
                      <div className="col-span-2 flex items-center justify-center">
                        <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden bg-white shadow-sm">
                          <button
                            type="button"
                            onClick={() => {
                              const nextQty = Math.max(1, Math.round(item.quantity) - 1);
                              updateQuantity(item.id, nextQty);
                            }}
                            disabled={item.quantity <= 1}
                            className="p-1.5 text-gray-500 hover:bg-gray-100 disabled:opacity-40 disabled:hover:bg-transparent"
                            title="Giảm 1"
                          >
                            <Minus className="w-3 h-3" />
                          </button>
                          <span className="px-2 text-xs font-bold text-gray-800 min-w-[50px] text-center">
                            {Math.round(item.quantity)}
                          </span>
                          <button
                            type="button"
                            onClick={() => {
                              const nextQty = Math.round(item.quantity) + 1;
                              updateQuantity(item.id, nextQty);
                            }}
                            className="p-1.5 text-gray-500 hover:bg-gray-100"
                            title="Tăng 1"
                          >
                            <Plus className="w-3 h-3" />
                          </button>
                        </div>
                      </div>

                      {/* Subtotal & Delete Action */}
                      <div className="col-span-2 flex items-center justify-between md:justify-end gap-3 pr-2">
                        <span className="text-sm font-bold text-chopee-orange">
                          {formatCurrency(item.subtotal)}
                        </span>
                        <button
                          onClick={() => removeItem(item.id)}
                          className="p-1.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                          title="Xóa khỏi giỏ hàng"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>

      {/* Sticky Bottom Checkout Action Bar */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-2xl z-40 py-3.5 px-4">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-4 text-xs text-gray-600">
            <label className="flex items-center gap-2 cursor-pointer font-semibold text-gray-800">
              <input
                type="checkbox"
                checked={selectedItemIds.size === items.length && items.length > 0}
                onChange={handleToggleAll}
                className="w-4 h-4 text-chopee-orange rounded focus:ring-orange-500"
              />
              <span>Chọn tất cả ({items.length})</span>
            </label>
            <span className="text-gray-300">|</span>
            <div className="flex items-center gap-1.5 text-gray-500 text-[11px]">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>Đơn hàng sẽ được tự động tách theo từng Shop khi thanh toán</span>
            </div>
          </div>

          <div className="flex items-center gap-6">
            <div className="text-right">
              <div className="flex items-baseline gap-2">
                <span className="text-xs text-gray-500 font-medium">
                  Tổng thanh toán ({selectedItems.length} sản phẩm):
                </span>
                <span className="text-xl font-black text-chopee-orange">
                  {formatCurrency(selectedTotalAmount)}
                </span>
              </div>
              <p className="text-[10px] text-gray-400">Tiết kiệm phí vận chuyển khi áp voucher ở bước kế tiếp</p>
            </div>

            <button
              onClick={handleProceedToCheckout}
              disabled={selectedItems.length === 0}
              className="py-3 px-8 bg-chopee-orange hover:bg-orange-600 disabled:opacity-50 text-white font-extrabold text-sm rounded-xl shadow-lg shadow-orange-500/25 transition-all flex items-center gap-2"
            >
              <span>Mua Hàng</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default CartPage;

