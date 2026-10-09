import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Star,
  Store,
  ShieldCheck,
  Truck,
  Plus,
  Minus,
  ShoppingCart,
  Zap,
  CheckCircle2,
  AlertCircle,
  Apple,
  Snowflake,
} from 'lucide-react';
import { ProductDetail } from '../types';
import { catalogApi } from '../services/api';
import { useCartStore } from '../stores/useCartStore';
import { useAuthStore } from '../stores/useAuthStore';
import { ReviewList } from '../components/ReviewList';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addToCart } = useCartStore();
  const { isAuthenticated } = useAuthStore();

  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedImage, setSelectedImage] = useState<string>('');
  const [quantity, setQuantity] = useState<number>(1);
  const [selectedVariantId, setSelectedVariantId] = useState<number | undefined>(undefined);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    catalogApi
      .getProductById(Number(id))
      .then((res) => {
        if (res.success && res.data) {
          const p = res.data;
          setProduct(p);
          setSelectedImage(p.thumbnailUrl || '');
          setQuantity(p.minOrderQuantity || 1);
        }
      })
      .catch((err) => {
        console.error('Lỗi tải chi tiết sản phẩm:', err);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-[400px] flex items-center justify-center text-gray-500">
        <p className="text-sm font-semibold animate-pulse">Đang tải thông tin sản phẩm...</p>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-[400px] flex flex-col items-center justify-center text-center p-8 bg-white rounded-2xl border border-gray-100">
        <AlertCircle className="w-12 h-12 text-gray-400 mb-3" />
        <h2 className="text-lg font-bold text-gray-800">Không tìm thấy sản phẩm</h2>
        <p className="text-xs text-gray-500 mb-4">Sản phẩm này có thể đã bị xóa hoặc tạm ngừng kinh doanh.</p>
        <Link to="/" className="px-5 py-2.5 bg-chopee-orange text-white text-xs font-bold rounded-xl">
          Quay lại trang chủ
        </Link>
      </div>
    );
  }

  const isWeightUnit =
    product.unit?.toLowerCase().trim() === 'kg' ||
    product.unit?.toLowerCase().trim() === 'kí' ||
    product.unit?.toLowerCase().trim() === 'ký' ||
    product.unit?.toLowerCase().trim() === 'g' ||
    product.unit?.toLowerCase().trim() === 'gram';

  const step = isWeightUnit ? (product.stepQuantity || 0.5) : 1;
  const minQty = isWeightUnit
    ? (product.minOrderQuantity || 0.5)
    : Math.max(1, Math.round(product.minOrderQuantity || 1));

  const weightPresets = [
    { label: '0.5 kg (500g)', value: 0.5 },
    { label: '1.0 kg (1 ký)', value: 1.0 },
    { label: '1.5 kg', value: 1.5 },
    { label: '2.0 kg (Túi 2kg)', value: 2.0 },
    { label: '3.0 kg', value: 3.0 },
    { label: '5.0 kg (Thùng 5kg)', value: 5.0 },
  ];

  const countPresets = [
    { label: `1 ${product.unit}`, value: 1 },
    { label: `2 ${product.unit}`, value: 2 },
    { label: `3 ${product.unit}`, value: 3 },
    { label: `5 ${product.unit}`, value: 5 },
    { label: `10 ${product.unit}`, value: 10 },
  ];

  const handleIncrease = () => {
    if (isWeightUnit) {
      setQuantity((prev) => Math.round((prev + step) * 100) / 100);
    } else {
      setQuantity((prev) => Math.round(prev + 1));
    }
  };

  const handleDecrease = () => {
    if (isWeightUnit) {
      setQuantity((prev) => Math.max(minQty, Math.round((prev - step) * 100) / 100));
    } else {
      setQuantity((prev) => Math.max(minQty, Math.round(prev - 1)));
    }
  };

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate(`/login?redirect=/products/${product.id}`);
      return;
    }
    setActionSuccess(null);
    setActionError(null);

    try {
      await addToCart(product.id, quantity, selectedVariantId);
      setActionSuccess(`Đã thêm ${quantity} ${product.unit} vào giỏ hàng!`);
      setTimeout(() => setActionSuccess(null), 3000);
    } catch (err: any) {
      setActionError(err.message || 'Không thể thêm vào giỏ hàng');
    }
  };

  const handleBuyNow = async () => {
    if (!isAuthenticated) {
      navigate(`/login?redirect=/products/${product.id}`);
      return;
    }
    try {
      await addToCart(product.id, quantity, selectedVariantId);
      navigate('/cart');
    } catch (err: any) {
      setActionError(err.message || 'Không thể tiến hành đặt mua');
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  // Parse JSON attributes
  let parsedAttributes: Record<string, string> = {};
  if (product.attributes) {
    try {
      parsedAttributes = JSON.parse(product.attributes);
    } catch (e) {
      console.error('Không thể parse attributes JSON', e);
    }
  }

  return (
    <div className="space-y-6">
      {/* Product Overview Card */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 md:p-8 shadow-sm grid grid-cols-1 md:grid-cols-12 gap-8">
        {/* Left: Product Images Gallery */}
        <div className="md:col-span-5 space-y-4">
          <div className="aspect-square rounded-2xl overflow-hidden bg-gray-50 border border-gray-100 relative group">
            <img
              src={selectedImage || product.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e'}
              alt={product.name}
              className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
            />
            {product.discountPercent && product.discountPercent > 0 && (
              <span className="absolute top-3 right-3 bg-yellow-400 text-red-700 font-black text-xs px-2 py-1 rounded-lg shadow-sm">
                GIẢM {product.discountPercent}%
              </span>
            )}
          </div>

          {/* Thumbnail list */}
          {product.images && product.images.length > 0 && (
            <div className="flex gap-2.5 overflow-x-auto pb-1">
              <button
                onClick={() => setSelectedImage(product.thumbnailUrl || '')}
                className={`w-16 h-16 rounded-xl overflow-hidden border-2 flex-shrink-0 ${
                  selectedImage === product.thumbnailUrl ? 'border-chopee-orange' : 'border-gray-200'
                }`}
              >
                <img src={product.thumbnailUrl} alt="Thumbnail" className="w-full h-full object-cover" />
              </button>
              {product.images.map((img) => (
                <button
                  key={img.id}
                  onClick={() => setSelectedImage(img.imageUrl)}
                  className={`w-16 h-16 rounded-xl overflow-hidden border-2 flex-shrink-0 ${
                    selectedImage === img.imageUrl ? 'border-chopee-orange' : 'border-gray-200'
                  }`}
                >
                  <img src={img.imageUrl} alt="Sub image" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Right: Product Details & Buying Actions */}
        <div className="md:col-span-7 flex flex-col justify-between">
          <div>
            {/* Badges */}
            <div className="flex items-center gap-2 mb-2 flex-wrap">
              {product.storageType === 'FRESH' && (
                <span className="px-2 py-0.5 rounded-md text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1">
                  <Apple className="w-3.5 h-3.5" /> Thực phẩm tươi sống
                </span>
              )}
              {product.storageType === 'FROZEN_CHILLED' && (
                <span className="px-2 py-0.5 rounded-md text-xs font-bold bg-cyan-50 text-cyan-700 border border-cyan-200 flex items-center gap-1">
                  <Snowflake className="w-3.5 h-3.5" /> Bảo quản mát / đông lạnh
                </span>
              )}
              {product.origin && (
                <span className="px-2 py-0.5 rounded-md text-xs font-semibold bg-gray-100 text-gray-600">
                  Xuất xứ: {product.origin}
                </span>
              )}
            </div>

            {/* Title */}
            <h1 className="text-xl md:text-2xl font-black text-gray-900 leading-snug">
              {product.name}
            </h1>

            {/* Ratings & Sold stats */}
            <div className="flex items-center gap-4 text-xs text-gray-500 mt-3 pb-4 border-b border-gray-100">
              <div className="flex items-center gap-1 text-chopee-orange font-bold">
                <span className="underline">{product.ratingAvg ? product.ratingAvg.toFixed(1) : '5.0'}</span>
                <div className="flex text-amber-400">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <Star key={s} className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                  ))}
                </div>
              </div>
              <span className="text-gray-300">|</span>
              <span>{product.reviewCount || 0} Đánh giá</span>
              <span className="text-gray-300">|</span>
              <span className="text-gray-700 font-semibold">{product.soldQuantity} Đã bán</span>
            </div>

            {/* Price Box */}
            <div className="my-5 p-4 bg-orange-50/60 rounded-2xl flex items-baseline gap-3">
              <span className="text-3xl font-black text-chopee-orange">
                {formatCurrency(product.sellingPrice)}
              </span>
              {product.originalPrice && product.originalPrice > product.sellingPrice && (
                <span className="text-sm text-gray-400 line-through">
                  {formatCurrency(product.originalPrice)}
                </span>
              )}
              <span className="text-xs text-gray-500 font-semibold">/ {product.unit}</span>
            </div>

            {/* Shipping & Delivery Guarantee */}
            <div className="space-y-2 mb-6 text-xs text-gray-600 bg-gray-50/60 p-3.5 rounded-xl border border-gray-100">
              <div className="flex items-center gap-2">
                <Truck className="w-4 h-4 text-emerald-600" />
                <span className="font-semibold text-gray-800">
                  {product.storageType === 'FRESH'
                    ? 'Giao Hỏa Tốc Chợ Tươi 2H (Chopee Express Fresh)'
                    : 'Giao Hàng Tiêu Chuẩn Toàn Quốc (2-3 ngày)'}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-chopee-orange" />
                <span>Chopee Đảm Bảo: Hoàn tiền 100% nếu sản phẩm bị hư hỏng hoặc không đúng cam kết.</span>
              </div>
            </div>

            {/* Variants Selector if available */}
            {product.variants && product.variants.length > 0 && (
              <div className="flex items-center gap-3 mb-5">
                <span className="text-xs font-semibold text-gray-700">Phân loại:</span>
                <div className="flex flex-wrap gap-2">
                  {product.variants.map((v) => (
                    <button
                      key={v.id}
                      type="button"
                      onClick={() => setSelectedVariantId(v.id)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                        selectedVariantId === v.id
                          ? 'bg-orange-50 border-chopee-orange text-chopee-orange font-bold'
                          : 'border-gray-200 text-gray-700 hover:border-gray-300'
                      }`}
                    >
                      {v.variantName}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Weight / Packaging Presets for KG products */}
            {isWeightUnit && (
              <div className="mb-5 space-y-2">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-700">
                  <Apple className="w-3.5 h-3.5 text-emerald-600" />
                  <span>Chọn quy cách đóng gói & Khối lượng (kg):</span>
                </div>
                <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
                  {weightPresets.map((preset) => (
                    <button
                      key={preset.value}
                      type="button"
                      onClick={() => setQuantity(preset.value)}
                      className={`px-2.5 py-2 rounded-xl text-xs font-bold border transition-all text-center ${
                        quantity === preset.value
                          ? 'bg-orange-50 border-chopee-orange text-chopee-orange shadow-sm scale-102 ring-1 ring-chopee-orange'
                          : 'border-gray-200 bg-gray-50/60 text-gray-700 hover:border-gray-300 hover:bg-white'
                      }`}
                    >
                      {preset.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Quick Count Presets for discrete unit products */}
            {!isWeightUnit && countPresets.length > 0 && (
              <div className="mb-5 space-y-2">
                <span className="block text-xs font-semibold text-gray-700">
                  Chọn số lượng nhanh ({product.unit}):
                </span>
                <div className="flex flex-wrap gap-2">
                  {countPresets.map((preset) => (
                    <button
                      key={preset.value}
                      type="button"
                      onClick={() => setQuantity(preset.value)}
                      className={`px-3 py-1.5 rounded-xl text-xs font-bold border transition-all ${
                        quantity === preset.value
                          ? 'bg-orange-50 border-chopee-orange text-chopee-orange shadow-sm ring-1 ring-chopee-orange'
                          : 'border-gray-200 bg-gray-50/60 text-gray-700 hover:border-gray-300 hover:bg-white'
                      }`}
                    >
                      {preset.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Quantity Selector Stepper */}
            <div className="flex flex-wrap items-center gap-4 mb-6">
              <span className="text-xs font-semibold text-gray-700">
                {isWeightUnit ? 'Cân ký tùy chỉnh:' : 'Số lượng đặt mua:'}
              </span>
              <div className="flex items-center border border-gray-200 rounded-xl overflow-hidden bg-white shadow-sm">
                <button
                  type="button"
                  onClick={handleDecrease}
                  className="px-3.5 py-2 text-gray-600 hover:bg-gray-100 transition-colors"
                  title="Giảm số lượng"
                >
                  <Minus className="w-3.5 h-3.5" />
                </button>
                <span className="px-4 py-1 text-sm font-bold text-gray-900 min-w-[70px] text-center">
                  {isWeightUnit ? `${quantity} kg` : `${quantity} ${product.unit}`}
                </span>
                <button
                  type="button"
                  onClick={handleIncrease}
                  className="px-3.5 py-2 text-gray-600 hover:bg-gray-100 transition-colors"
                  title="Tăng số lượng"
                >
                  <Plus className="w-3.5 h-3.5" />
                </button>
              </div>

              {/* Subtotal preview */}
              <div className="text-xs text-gray-500">
                <span>Tạm tính: </span>
                <strong className="text-sm font-black text-chopee-orange">
                  {formatCurrency(Math.round(product.sellingPrice * quantity))}
                </strong>
                {isWeightUnit && (
                  <span className="text-[11px] text-gray-400 ml-1.5">
                    ({quantity} kg × {formatCurrency(product.sellingPrice)}/kg)
                  </span>
                )}
              </div>
            </div>

            {/* Action Feedback Alerts */}
            {actionSuccess && (
              <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-xl flex items-center gap-2 animate-in fade-in">
                <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                <span>{actionSuccess}</span>
              </div>
            )}
            {actionError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{actionError}</span>
              </div>
            )}
          </div>

          {/* CTA Buttons */}
          <div className="grid grid-cols-2 gap-3 pt-4 border-t border-gray-100">
            <button
              onClick={handleAddToCart}
              className="py-3 px-4 bg-orange-100 hover:bg-orange-200 text-chopee-orange font-bold text-xs md:text-sm rounded-xl transition-colors flex items-center justify-center gap-2 border border-orange-300"
            >
              <ShoppingCart className="w-4 h-4" />
              <span>Thêm Vào Giỏ Hàng</span>
            </button>
            <button
              onClick={handleBuyNow}
              className="py-3 px-4 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-xs md:text-sm rounded-xl transition-colors shadow-md shadow-orange-500/20 flex items-center justify-center gap-2"
            >
              <Zap className="w-4 h-4" />
              <span>Mua Ngay</span>
            </button>
          </div>
        </div>
      </div>

      {/* Shop Information Card */}
      {product.shop && (
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-orange-100 border-2 border-orange-200 text-chopee-orange flex items-center justify-center font-bold text-xl flex-shrink-0">
              <Store className="w-8 h-8" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-gray-900">{product.shop.name}</h3>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-orange-50 text-chopee-orange border border-orange-200">
                  {product.shop.shopType === 'FOOD_FRESH' ? 'Chợ Thực Phẩm' : 'Gian Hàng Chuẩn'}
                </span>
              </div>
              <p className="text-xs text-gray-500 mt-1">{product.shop.address}</p>
              <div className="flex items-center gap-3 text-xs text-gray-600 mt-2 font-medium">
                <span>⭐ Đánh giá: {product.shop.rating ? product.shop.rating.toFixed(1) : '5.0'}</span>
                <span className="text-gray-300">|</span>
                <span>Phản hồi chat: 100%</span>
              </div>
            </div>
          </div>

          <Link
            to={`/?keyword=${encodeURIComponent(product.shop.name)}`}
            className="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-800 text-xs font-bold rounded-xl transition-colors"
          >
            Xem Gian Hàng
          </Link>
        </div>
      )}

      {/* Dynamic Attributes & Specifications Table */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm">
        <h3 className="text-base font-bold text-gray-900 mb-4 uppercase tracking-wider">
          Chi Tiết Sản Phẩm & Thông Số Kỹ Thuật
        </h3>

        <div className="divide-y divide-gray-100 text-xs">
          <div className="py-2.5 grid grid-cols-3">
            <span className="text-gray-400 font-medium">Danh mục</span>
            <span className="col-span-2 text-gray-800 font-semibold">{product.categoryName}</span>
          </div>
          <div className="py-2.5 grid grid-cols-3">
            <span className="text-gray-400 font-medium">Tồn kho khả dụng</span>
            <span className="col-span-2 text-gray-800 font-semibold">
              {product.stockQuantity} {product.unit}
            </span>
          </div>
          <div className="py-2.5 grid grid-cols-3">
            <span className="text-gray-400 font-medium">Hạn sử dụng</span>
            <span className="col-span-2 text-gray-800 font-semibold">{product.shelfLife || 'Theo hướng dẫn trên bao bì'}</span>
          </div>
          <div className="py-2.5 grid grid-cols-3">
            <span className="text-gray-400 font-medium">Điều kiện lưu kho</span>
            <span className="col-span-2 text-gray-800 font-semibold">
              {product.storageType === 'FRESH'
                ? 'Nhiệt độ phòng mát (15-20°C)'
                : product.storageType === 'FROZEN_CHILLED'
                ? 'Ngăn đông lạnh (-18°C) hoặc ngăn mát (0-4°C)'
                : 'Nhiệt độ phòng thoáng khí'}
            </span>
          </div>

          {/* Dynamic JSON Specifications */}
          {Object.entries(parsedAttributes).map(([key, value]) => (
            <div key={key} className="py-2.5 grid grid-cols-3">
              <span className="text-gray-400 font-medium capitalize">{key}</span>
              <span className="col-span-2 text-gray-800 font-semibold">{String(value)}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Product Reviews List */}
      <ReviewList
        productId={product.id}
        ratingAvg={product.ratingAvg}
        reviewCount={product.reviewCount}
      />
    </div>
  );
};
export default ProductDetailPage;
