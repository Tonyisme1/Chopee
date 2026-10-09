import React from 'react';
import { Link } from 'react-router-dom';
import { Star, Store } from 'lucide-react';
import { ProductSummary } from '../types';

interface ProductCardProps {
  product: ProductSummary;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  const getStorageBadge = () => {
    switch (product.storageType) {
      case 'FRESH':
        return (
          <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
            🌱 Tươi sống
          </span>
        );
      case 'FROZEN_CHILLED':
        return (
          <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-cyan-50 text-cyan-700 border border-cyan-200">
            ❄️ Đông / Mát
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <Link
      to={`/products/${product.id}`}
      className="group bg-white rounded-xl border border-gray-100 hover:border-orange-300 shadow-sm hover:shadow-lg transition-all duration-200 flex flex-col overflow-hidden relative"
    >
      {/* Product Image Container */}
      <div className="relative aspect-square w-full bg-gray-50 overflow-hidden">
        <img
          src={product.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80'}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          loading="lazy"
        />

        {/* Discount Badge */}
        {product.discountPercent && product.discountPercent > 0 ? (
          <div className="absolute top-2 right-2 bg-yellow-400 text-red-700 text-[11px] font-black px-1.5 py-0.5 rounded shadow-sm">
            -{product.discountPercent}%
          </div>
        ) : null}

        {/* Storage Type Tag */}
        <div className="absolute bottom-2 left-2 flex gap-1">
          {getStorageBadge()}
          <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold bg-black/60 text-white backdrop-blur-sm">
            {product.unit}
          </span>
        </div>
      </div>

      {/* Content Details */}
      <div className="p-3.5 flex-1 flex flex-col justify-between">
        <div>
          {/* Shop Name */}
          <div className="flex items-center gap-1 text-[11px] text-gray-500 mb-1 truncate">
            <Store className="w-3 h-3 text-chopee-orange flex-shrink-0" />
            <span className="truncate">{product.shopName}</span>
          </div>

          {/* Product Title */}
          <h3 className="text-xs font-semibold text-gray-800 line-clamp-2 leading-relaxed group-hover:text-chopee-orange transition-colors">
            {product.name}
          </h3>
        </div>

        {/* Pricing and Stats */}
        <div className="mt-3 pt-2 border-t border-gray-50">
          <div className="flex items-baseline gap-1.5 flex-wrap">
            <span className="text-sm font-black text-chopee-orange">
              {formatCurrency(product.sellingPrice)}
            </span>
            {product.originalPrice && product.originalPrice > product.sellingPrice && (
              <span className="text-[10px] text-gray-400 line-through">
                {formatCurrency(product.originalPrice)}
              </span>
            )}
          </div>

          {/* Rating & Sold count */}
          <div className="flex items-center justify-between text-[11px] text-gray-500 mt-2">
            <div className="flex items-center gap-0.5 text-amber-500">
              <Star className="w-3 h-3 fill-amber-400 text-amber-400" />
              <span className="font-semibold text-gray-700">
                {product.ratingAvg ? product.ratingAvg.toFixed(1) : '5.0'}
              </span>
            </div>
            <span>Đã bán {product.soldQuantity >= 1000 ? `${(product.soldQuantity / 1000).toFixed(1)}k` : product.soldQuantity}</span>
          </div>
        </div>
      </div>
    </Link>
  );
};
export default ProductCard;
