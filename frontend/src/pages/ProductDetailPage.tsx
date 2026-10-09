import React, { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Star,
  Store,
  ShieldCheck,
  Truck,
  Plus,
  Minus,
  ShoppingCart,
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
  const [selectedOptionIndex, setSelectedOptionIndex] = useState<number>(0);
  const [selectedVariantId, setSelectedVariantId] = useState<number | undefined>(undefined);
  const [selectedTier1, setSelectedTier1] = useState<string>('');
  const [selectedTier2, setSelectedTier2] = useState<string>('');
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
          setQuantity(1);
          if (p.variants && p.variants.length > 0) {
            setSelectedVariantId(p.variants[0].id);
            setSelectedOptionIndex(0);
          }
        }
      })
      .catch((err) => {
        console.error('Lỗi tải chi tiết sản phẩm:', err);
      })
      .finally(() => setLoading(false));
  }, [id]);

  interface PackagingOption {
    id?: number;
    name: string;
    price: number;
    originalPrice?: number;
    stockQuantity?: number;
  }

  interface TierGroup {
    name: string;
    options: string[];
  }

  const tierGroups: TierGroup[] = useMemo(() => {
    if (!product || !product.tierVariation) return [];
    try {
      const parsed = JSON.parse(product.tierVariation);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed.filter((g) => g.name && Array.isArray(g.options) && g.options.length > 0);
      }
    } catch (e) {
      console.error('Lỗi parse tierVariation JSON:', e);
    }
    return [];
  }, [product]);

  useEffect(() => {
    if (tierGroups.length > 0) {
      setSelectedTier1(tierGroups[0].options[0] || '');
      if (tierGroups.length > 1) {
        setSelectedTier2(tierGroups[1].options[0] || '');
      } else {
        setSelectedTier2('');
      }
    }
  }, [tierGroups]);

  const matchedTierVariant = useMemo(() => {
    if (!product || !product.variants || tierGroups.length === 0) return null;

    const t1Name = tierGroups[0]?.name;
    const t2Name = tierGroups[1]?.name;

    return (
      product.variants.find((v) => {
        if (v.attributes) {
          try {
            const attrs = JSON.parse(v.attributes);
            const matchT1 = !selectedTier1 || attrs[t1Name] === selectedTier1;
            const matchT2 = !t2Name || !selectedTier2 || attrs[t2Name] === selectedTier2;
            if (matchT1 && matchT2) return true;
          } catch {
            // fallback
          }
        }
        if (t2Name && selectedTier2) {
          if (v.variantName === `${selectedTier1} - ${selectedTier2}`) return true;
        } else {
          if (v.variantName === selectedTier1) return true;
        }
        return false;
      }) || null
    );
  }, [product, tierGroups, selectedTier1, selectedTier2]);

  const packagingOptions: PackagingOption[] = useMemo(() => {
    if (!product) return [];

    if (product.variants && product.variants.length > 0) {
      return product.variants.map((v) => {
        const price = v.price || product.sellingPrice;
        const ratio = product.sellingPrice > 0 ? price / product.sellingPrice : 1;
        const origPrice = product.originalPrice ? Math.round(product.originalPrice * ratio) : undefined;

        return {
          id: v.id,
          name: v.variantName,
          price,
          originalPrice: origPrice,
          stockQuantity: v.stockQuantity,
        };
      });
    }

    return [
      {
        name: product.name,
        price: product.sellingPrice,
        originalPrice: product.originalPrice,
        stockQuantity: product.stockQuantity,
      },
    ];
  }, [product]);

  const activeTierOption: PackagingOption | null = useMemo(() => {
    if (tierGroups.length === 0 || !product) return null;

    const currentPrice = matchedTierVariant ? matchedTierVariant.price : product.sellingPrice;
    const currentOrigPrice = product.originalPrice
      ? Math.round(product.originalPrice * (product.sellingPrice > 0 ? currentPrice / product.sellingPrice : 1))
      : undefined;
    const currentStock = matchedTierVariant ? matchedTierVariant.stockQuantity : product.stockQuantity;
    const currentVariantId = matchedTierVariant ? matchedTierVariant.id : undefined;
    const currentVariantName = matchedTierVariant
      ? matchedTierVariant.variantName
      : [selectedTier1, selectedTier2].filter(Boolean).join(' - ') || product.name;

    return {
      id: currentVariantId,
      name: currentVariantName,
      price: currentPrice,
      originalPrice: currentOrigPrice,
      stockQuantity: currentStock,
    };
  }, [tierGroups, matchedTierVariant, product, selectedTier1, selectedTier2]);

  const activeOption: PackagingOption =
    activeTierOption ||
    packagingOptions[selectedOptionIndex] ||
    packagingOptions[0] || {
      name: product?.name || 'Sản phẩm',
      price: product?.sellingPrice || 0,
      originalPrice: product?.originalPrice,
      stockQuantity: product?.stockQuantity || 0,
    };

  const handleSelectOption = (idx: number) => {
    setSelectedOptionIndex(idx);
    const opt = packagingOptions[idx];
    if (opt && opt.id) {
      setSelectedVariantId(opt.id);
    } else {
      setSelectedVariantId(undefined);
    }
  };

  const handleIncrease = () => {
    setQuantity((prev) => Math.max(1, Math.round(prev)) + 1);
  };

  const handleDecrease = () => {
    setQuantity((prev) => Math.max(1, Math.round(prev) - 1));
  };

  const handleAddToCart = async () => {
    if (!product) return;
    if (!isAuthenticated) {
      navigate(`/login?redirect=/products/${product.id}`);
      return;
    }
    setActionSuccess(null);
    setActionError(null);

    try {
      const variantIdToUse = activeOption.id || selectedVariantId;
      await addToCart(product.id, Math.round(quantity), variantIdToUse);
      setActionSuccess('Đã thêm sản phẩm vào giỏ hàng thành công!');
      setTimeout(() => setActionSuccess(null), 3000);
    } catch (err: any) {
      setActionError(err.message || 'Không thể thêm vào giỏ hàng');
    }
  };

  const handleBuyNow = async () => {
    if (!product) return;
    if (!isAuthenticated) {
      navigate(`/login?redirect=/products/${product.id}`);
      return;
    }
    try {
      const variantIdToUse = activeOption.id || selectedVariantId;
      await addToCart(product.id, Math.round(quantity), variantIdToUse);
      navigate('/cart');
    } catch (err: any) {
      setActionError(err.message || 'Không thể tiến hành đặt mua');
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  // Parse JSON attributes
  let parsedAttributes: Record<string, string> = {};
  if (product?.attributes) {
    try {
      parsedAttributes = JSON.parse(product.attributes);
    } catch (e) {
      console.error('Không thể parse attributes JSON', e);
    }
  }

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

            {/* Shopee Style Price Box */}
            <div className="my-4 p-4 bg-[#fafafa] rounded flex items-center gap-3">
              {activeOption.originalPrice && activeOption.originalPrice > activeOption.price && (
                <span className="text-sm text-gray-400 line-through">
                  {formatCurrency(activeOption.originalPrice)}
                </span>
              )}
              <span className="text-3xl font-bold text-[#ee4d2d]">
                {formatCurrency(activeOption.price)}
              </span>
              {product.discountPercent && product.discountPercent > 0 && (
                <span className="bg-[#ee4d2d] text-white text-[10px] font-bold px-1.5 py-0.5 rounded uppercase">
                  -{product.discountPercent}% GIẢM
                </span>
              )}
            </div>

            {/* Shopee Attribute & Option Rows */}
            <div className="space-y-5 my-6 text-sm">
              {/* Vận Chuyển Row */}
              <div className="flex items-start">
                <span className="w-24 md:w-28 text-gray-500 text-sm flex-shrink-0 pt-0.5">
                  Vận Chuyển
                </span>
                <div className="flex-1 space-y-1">
                  <div className="flex items-center gap-2 text-gray-800">
                    <Truck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                    <span className="font-medium text-sm">
                      {product.storageType === 'FRESH'
                        ? 'Giao Hỏa Tốc Chợ Tươi 2H (Chopee Express Fresh)'
                        : 'Miễn Phí Vận Chuyển Toàn Quốc'}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400">
                    Cam kết giao đúng hẹn, bảo quản tối ưu tiêu chuẩn Chopee
                  </p>
                </div>
              </div>

              {/* Multi-tier Variations */}
              {tierGroups.length > 0 ? (
                <>
                  {/* Tier 1 Row (e.g. Mẫu, Màu sắc, Loại) */}
                  <div className="flex items-start">
                    <span className="w-24 md:w-28 text-gray-500 text-sm flex-shrink-0 pt-1.5">
                      {tierGroups[0].name}
                    </span>
                    <div className="flex-1 flex flex-wrap gap-2.5">
                      {tierGroups[0].options.map((opt) => {
                        const isSelected = selectedTier1 === opt;
                        return (
                          <button
                            key={opt}
                            type="button"
                            onClick={() => setSelectedTier1(opt)}
                            className={`min-h-[38px] px-4 py-1.5 border rounded-sm text-sm transition-all relative flex items-center justify-center cursor-pointer ${
                              isSelected
                                ? 'border-[#ee4d2d] text-[#ee4d2d] bg-white font-medium'
                                : 'border-gray-200 text-gray-800 bg-white hover:border-[#ee4d2d] hover:text-[#ee4d2d]'
                            }`}
                          >
                            <span>{opt}</span>
                            {isSelected && (
                              <div className="absolute bottom-0 right-0 overflow-hidden w-3 h-3">
                                <div className="absolute bottom-0 right-0 w-0 h-0 border-b-[12px] border-l-[12px] border-b-[#ee4d2d] border-l-transparent" />
                                <svg className="absolute bottom-0 right-0 w-2 h-2 text-white" viewBox="0 0 10 10" fill="currentColor">
                                  <path d="M8.5 2.5L3.8 7.2 1.5 4.9 2.5 3.9 3.8 5.2 7.5 1.5z" />
                                </svg>
                              </div>
                            )}
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  {/* Tier 2 Row (if exists, e.g. Kích cỡ, Quy cách, Cấu hình) */}
                  {tierGroups.length > 1 && (
                    <div className="flex items-start">
                      <span className="w-24 md:w-28 text-gray-500 text-sm flex-shrink-0 pt-1.5">
                        {tierGroups[1].name}
                      </span>
                      <div className="flex-1 flex flex-wrap gap-2.5">
                        {tierGroups[1].options.map((opt) => {
                          const isSelected = selectedTier2 === opt;
                          return (
                            <button
                              key={opt}
                              type="button"
                              onClick={() => setSelectedTier2(opt)}
                              className={`min-h-[38px] px-4 py-1.5 border rounded-sm text-sm transition-all relative flex items-center justify-center cursor-pointer ${
                                isSelected
                                  ? 'border-[#ee4d2d] text-[#ee4d2d] bg-white font-medium'
                                  : 'border-gray-200 text-gray-800 bg-white hover:border-[#ee4d2d] hover:text-[#ee4d2d]'
                              }`}
                            >
                              <span>{opt}</span>
                              {isSelected && (
                                <div className="absolute bottom-0 right-0 overflow-hidden w-3 h-3">
                                  <div className="absolute bottom-0 right-0 w-0 h-0 border-b-[12px] border-l-[12px] border-b-[#ee4d2d] border-l-transparent" />
                                  <svg className="absolute bottom-0 right-0 w-2 h-2 text-white" viewBox="0 0 10 10" fill="currentColor">
                                    <path d="M8.5 2.5L3.8 7.2 1.5 4.9 2.5 3.9 3.8 5.2 7.5 1.5z" />
                                  </svg>
                                </div>
                              )}
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </>
              ) : packagingOptions.length > 1 ? (
                /* 1-Tier Legacy Variations */
                <div className="flex items-start">
                  <span className="w-24 md:w-28 text-gray-500 text-sm flex-shrink-0 pt-1.5">
                    Phân Loại
                  </span>
                  <div className="flex-1 flex flex-wrap gap-2.5">
                    {packagingOptions.map((opt, idx) => {
                      const isSelected = selectedOptionIndex === idx;
                      return (
                        <button
                          key={opt.id || opt.name}
                          type="button"
                          onClick={() => handleSelectOption(idx)}
                          className={`min-h-[38px] px-4 py-1.5 border rounded-sm text-sm transition-all relative flex items-center justify-center cursor-pointer ${
                            isSelected
                              ? 'border-[#ee4d2d] text-[#ee4d2d] bg-white font-medium'
                              : 'border-gray-200 text-gray-800 bg-white hover:border-[#ee4d2d] hover:text-[#ee4d2d]'
                          }`}
                        >
                          <span>{opt.name}</span>
                          {isSelected && (
                            <div className="absolute bottom-0 right-0 overflow-hidden w-3 h-3">
                              <div className="absolute bottom-0 right-0 w-0 h-0 border-b-[12px] border-l-[12px] border-b-[#ee4d2d] border-l-transparent" />
                              <svg className="absolute bottom-0 right-0 w-2 h-2 text-white" viewBox="0 0 10 10" fill="currentColor">
                                <path d="M8.5 2.5L3.8 7.2 1.5 4.9 2.5 3.9 3.8 5.2 7.5 1.5z" />
                              </svg>
                            </div>
                          )}
                        </button>
                      );
                    })}
                  </div>
                </div>
              ) : null}

              {/* Số Lượng (Quantity Stepper - Clean Shopee Standard) */}
              <div className="flex items-center">
                <span className="w-24 md:w-28 text-gray-500 text-sm flex-shrink-0">
                  Số Lượng
                </span>
                <div className="flex-1 flex items-center gap-4">
                  <div className="inline-flex items-center border border-gray-300 rounded-sm overflow-hidden bg-white">
                    <button
                      type="button"
                      onClick={handleDecrease}
                      disabled={quantity <= 1}
                      className="w-8 h-8 flex items-center justify-center text-gray-600 hover:bg-gray-100 disabled:opacity-30 disabled:hover:bg-transparent transition-colors border-r border-gray-300 cursor-pointer"
                      title="Giảm"
                    >
                      <Minus className="w-3.5 h-3.5" />
                    </button>
                    <input
                      type="text"
                      value={quantity}
                      onChange={(e) => {
                        const val = parseInt(e.target.value, 10);
                        if (!isNaN(val) && val >= 1) {
                          const maxStock = activeOption.stockQuantity !== undefined ? activeOption.stockQuantity : product.stockQuantity;
                          setQuantity(Math.min(val, maxStock > 0 ? maxStock : 9999));
                        }
                      }}
                      className="w-12 h-8 text-center text-sm font-normal text-gray-800 focus:outline-none"
                    />
                    <button
                      type="button"
                      onClick={handleIncrease}
                      disabled={activeOption.stockQuantity !== undefined && quantity >= activeOption.stockQuantity}
                      className="w-8 h-8 flex items-center justify-center text-gray-600 hover:bg-gray-100 disabled:opacity-30 disabled:hover:bg-transparent transition-colors border-l border-gray-300 cursor-pointer"
                      title="Tăng"
                    >
                      <Plus className="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <span className="text-xs text-gray-500">
                    {activeOption.stockQuantity !== undefined ? activeOption.stockQuantity : product.stockQuantity} sản phẩm có sẵn
                  </span>
                </div>
              </div>

              {/* Action Feedback Alerts */}
              {actionSuccess && (
                <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded flex items-center gap-2 animate-in fade-in">
                  <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                  <span>{actionSuccess}</span>
                </div>
              )}
              {actionError && (
                <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  <span>{actionError}</span>
                </div>
              )}

              {/* Action Buttons (Shopee Standard) */}
              <div className="flex items-center gap-4 pt-4">
                <button
                  type="button"
                  onClick={handleAddToCart}
                  className="px-6 py-3 bg-[#ee4d2d]/10 hover:bg-[#ee4d2d]/15 text-[#ee4d2d] border border-[#ee4d2d] rounded-sm font-medium text-sm transition-colors flex items-center gap-2 shadow-xs cursor-pointer"
                >
                  <ShoppingCart className="w-4 h-4" />
                  <span>Thêm Vào Giỏ Hàng</span>
                </button>
                <button
                  type="button"
                  onClick={handleBuyNow}
                  className="px-10 py-3 bg-[#ee4d2d] hover:bg-[#d73211] text-white rounded-sm font-medium text-sm transition-colors shadow-xs cursor-pointer"
                >
                  <span>Mua Ngay</span>
                </button>
              </div>

              {/* Shopping Guarantee (Shopee Standard) */}
              <div className="pt-6 border-t border-gray-100 flex flex-wrap items-center gap-6 text-xs text-gray-600">
                <div className="flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4 text-[#ee4d2d]" />
                  <span>15 Ngày Đổi Trả Miễn Phí</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-[#ee4d2d]" />
                  <span>Chính Hãng 100%</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <Truck className="w-4 h-4 text-[#ee4d2d]" />
                  <span>Miễn Phí Vận Chuyển</span>
                </div>
              </div>
            </div>
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
