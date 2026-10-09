import React from 'react';
import { Category } from '../types';
import {
  Apple,
  Coffee,
  Tv,
  Smartphone,
  Shirt,
  Sparkles,
  LayoutGrid,
} from 'lucide-react';

interface CategoryNavProps {
  categories: Category[];
  activeCategoryId?: number;
  onSelectCategory?: (categoryId?: number) => void;
}

export const CategoryNav: React.FC<CategoryNavProps> = ({
  categories,
  activeCategoryId,
  onSelectCategory,
}) => {
  const getCategoryIcon = (name: string) => {
    const lower = name.toLowerCase();
    if (lower.includes('thực phẩm') || lower.includes('nông sản') || lower.includes('rau') || lower.includes('tươi')) {
      return <Apple className="w-5 h-5 text-emerald-600" />;
    }
    if (lower.includes('nước') || lower.includes('uống') || lower.includes('giải khát') || lower.includes('bia')) {
      return <Coffee className="w-5 h-5 text-amber-600" />;
    }
    if (lower.includes('gia dụng') || lower.includes('thiết bị') || lower.includes('nhà bếp')) {
      return <Tv className="w-5 h-5 text-indigo-600" />;
    }
    if (lower.includes('công nghệ') || lower.includes('phụ kiện') || lower.includes('điện thoại')) {
      return <Smartphone className="w-5 h-5 text-blue-600" />;
    }
    if (lower.includes('thời trang') || lower.includes('quần áo')) {
      return <Shirt className="w-5 h-5 text-rose-600" />;
    }
    return <Sparkles className="w-5 h-5 text-orange-500" />;
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 mb-6">
      <div className="flex items-center justify-between mb-3 px-1">
        <h2 className="text-sm font-bold text-gray-800 uppercase tracking-wider flex items-center gap-2">
          <LayoutGrid className="w-4 h-4 text-chopee-orange" />
          <span>Danh Mục Ngành Hàng Chopee</span>
        </h2>
        {activeCategoryId && onSelectCategory && (
          <button
            onClick={() => onSelectCategory(undefined)}
            className="text-xs text-chopee-orange hover:underline font-semibold"
          >
            Xem tất cả
          </button>
        )}
      </div>

      {/* Categories Grid */}
      <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-7 gap-3">
        {/* All Categories Option */}
        {onSelectCategory && (
          <button
            onClick={() => onSelectCategory(undefined)}
            className={`p-3 rounded-xl border flex flex-col items-center justify-center gap-2 text-center transition-all ${
              !activeCategoryId
                ? 'bg-orange-50/80 border-orange-300 text-chopee-orange shadow-sm font-bold'
                : 'border-gray-100 hover:border-gray-200 text-gray-700 hover:bg-gray-50'
            }`}
          >
            <div className="w-10 h-10 rounded-full bg-orange-100/60 flex items-center justify-center">
              <LayoutGrid className="w-5 h-5 text-chopee-orange" />
            </div>
            <span className="text-xs truncate w-full">Tất cả</span>
          </button>
        )}

        {categories.map((cat) => {
          const isSelected = activeCategoryId === cat.id;

          return (
            <button
              key={cat.id}
              onClick={() => onSelectCategory && onSelectCategory(cat.id)}
              className={`p-3 rounded-xl border flex flex-col items-center justify-center gap-2 text-center transition-all ${
                isSelected
                  ? 'bg-orange-50/80 border-orange-300 text-chopee-orange shadow-sm font-bold'
                  : 'border-gray-100 hover:border-gray-200 text-gray-700 hover:bg-gray-50'
              }`}
            >
              <div className="w-10 h-10 rounded-full bg-gray-50 flex items-center justify-center group-hover:scale-110 transition-transform">
                {getCategoryIcon(cat.name)}
              </div>
              <span className="text-xs truncate w-full" title={cat.name}>
                {cat.name}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
export default CategoryNav;

