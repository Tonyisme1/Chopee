import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Filter, ArrowUpDown, ChevronLeft, ChevronRight, RefreshCw, Layers } from 'lucide-react';
import { ProductSummary, Category } from '../types';
import { catalogApi } from '../services/api';
import { ProductCard } from '../components/ProductCard';

export const CategoryPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const categoryId = Number(id);

  const [category, setCategory] = useState<Category | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Filters
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [storageType, setStorageType] = useState<string | undefined>(undefined);
  const [rating, setRating] = useState<number | undefined>(undefined);
  const [sortBy, setSortBy] = useState<string>('soldQuantity');
  const [sortDir, setSortDir] = useState<string>('desc');
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalElements, setTotalElements] = useState<number>(0);

  // Load category meta
  useEffect(() => {
    catalogApi.getCategories().then((res) => {
      if (res.success && res.data) {
        const found = res.data.find((c) => c.id === categoryId);
        setCategory(found || null);
      }
    }).catch(console.error);
  }, [categoryId]);

  const fetchCategoryProducts = useCallback(async () => {
    setLoading(true);
    try {
      const res = await catalogApi.getProducts({
        categoryId,
        minPrice,
        maxPrice,
        storageType,
        rating,
        sortBy,
        sortDir,
        page,
        size: 12,
      });

      if (res.success && res.data) {
        setProducts(res.data.content || []);
        setTotalPages(res.data.totalPages || 1);
        setTotalElements(res.data.totalElements || 0);
      }
    } catch (err) {
      console.error('Lỗi tải sản phẩm theo danh mục:', err);
    } finally {
      setLoading(false);
    }
  }, [categoryId, minPrice, maxPrice, storageType, rating, sortBy, sortDir, page]);

  useEffect(() => {
    fetchCategoryProducts();
  }, [fetchCategoryProducts]);

  return (
    <div className="space-y-6">
      {/* Breadcrumb & Header */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs text-gray-400 mb-1">
            <Link to="/" className="hover:text-chopee-orange">Trang chủ</Link>
            <span>/</span>
            <span className="text-gray-700 font-semibold">{category?.name || `Danh mục #${categoryId}`}</span>
          </div>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight flex items-center gap-2">
            <Layers className="w-6 h-6 text-chopee-orange" />
            <span>{category?.name || 'Danh Mục Sản Phẩm'}</span>
          </h1>
        </div>

        <span className="text-xs bg-orange-50 text-chopee-orange font-bold px-3 py-1.5 rounded-xl border border-orange-200">
          {totalElements} sản phẩm đang mở bán
        </span>
      </div>

      {/* Main Filter + Products Layout */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Sidebar Filter */}
        <aside className="bg-white rounded-2xl border border-gray-100 p-5 shadow-sm space-y-6 h-fit">
          <div className="flex items-center justify-between border-b border-gray-100 pb-3">
            <div className="flex items-center gap-2 font-bold text-sm text-gray-800">
              <Filter className="w-4 h-4 text-chopee-orange" />
              <span>BỘ LỌC TÌM KIẾM</span>
            </div>
            <button
              onClick={() => {
                setMinPrice(undefined);
                setMaxPrice(undefined);
                setStorageType(undefined);
                setRating(undefined);
                setPage(0);
              }}
              className="text-xs text-chopee-orange hover:underline font-semibold"
            >
              Thiết lập lại
            </button>
          </div>

          {/* Storage Type */}
          <div>
            <h4 className="text-xs font-bold text-gray-700 uppercase tracking-wider mb-2.5">
              Loại Bảo Quản
            </h4>
            <div className="space-y-1.5 text-xs text-gray-600">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="storage"
                  checked={!storageType}
                  onChange={() => {
                    setStorageType(undefined);
                    setPage(0);
                  }}
                  className="text-chopee-orange focus:ring-orange-500"
                />
                <span>Tất cả</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="storage"
                  checked={storageType === 'FRESH'}
                  onChange={() => {
                    setStorageType('FRESH');
                    setPage(0);
                  }}
                  className="text-chopee-orange focus:ring-orange-500"
                />
                <span>🌱 Thực phẩm tươi sống</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="storage"
                  checked={storageType === 'FROZEN_CHILLED'}
                  onChange={() => {
                    setStorageType('FROZEN_CHILLED');
                    setPage(0);
                  }}
                  className="text-chopee-orange focus:ring-orange-500"
                />
                <span>❄️ Đông lạnh / Mát</span>
              </label>
            </div>
          </div>

          {/* Price Range */}
          <div>
            <h4 className="text-xs font-bold text-gray-700 uppercase tracking-wider mb-2.5">
              Khoảng Giá (VNĐ)
            </h4>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <input
                type="number"
                placeholder="Từ"
                value={minPrice || ''}
                onChange={(e) => {
                  setMinPrice(e.target.value ? Number(e.target.value) : undefined);
                  setPage(0);
                }}
                className="w-full px-2.5 py-1.5 bg-gray-50 border border-gray-200 rounded-lg text-gray-700 focus:outline-none"
              />
              <input
                type="number"
                placeholder="Đến"
                value={maxPrice || ''}
                onChange={(e) => {
                  setMaxPrice(e.target.value ? Number(e.target.value) : undefined);
                  setPage(0);
                }}
                className="w-full px-2.5 py-1.5 bg-gray-50 border border-gray-200 rounded-lg text-gray-700 focus:outline-none"
              />
            </div>
          </div>

          {/* Rating */}
          <div>
            <h4 className="text-xs font-bold text-gray-700 uppercase tracking-wider mb-2.5">
              Đánh Giá Sao
            </h4>
            <div className="space-y-1.5 text-xs text-gray-600">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="rating"
                  checked={!rating}
                  onChange={() => {
                    setRating(undefined);
                    setPage(0);
                  }}
                />
                <span>Tất cả</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="rating"
                  checked={rating === 4}
                  onChange={() => {
                    setRating(4);
                    setPage(0);
                  }}
                />
                <span>⭐⭐⭐⭐ Từ 4 sao trở lên</span>
              </label>
            </div>
          </div>
        </aside>

        {/* Product Grid Area */}
        <div className="md:col-span-3 space-y-4">
          {/* Sorter Bar */}
          <div className="bg-white rounded-2xl border border-gray-100 p-3 shadow-sm flex items-center justify-between text-xs">
            <span className="font-semibold text-gray-500">Sắp xếp theo:</span>
            <div className="flex items-center gap-1.5">
              <button
                onClick={() => {
                  setSortBy('soldQuantity');
                  setSortDir('desc');
                  setPage(0);
                }}
                className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
                  sortBy === 'soldQuantity'
                    ? 'bg-chopee-orange text-white font-bold'
                    : 'bg-gray-50 text-gray-700 hover:bg-gray-100'
                }`}
              >
                Bán chạy
              </button>
              <button
                onClick={() => {
                  setSortBy('createdAt');
                  setSortDir('desc');
                  setPage(0);
                }}
                className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
                  sortBy === 'createdAt'
                    ? 'bg-chopee-orange text-white font-bold'
                    : 'bg-gray-50 text-gray-700 hover:bg-gray-100'
                }`}
              >
                Mới nhất
              </button>
              <button
                onClick={() => {
                  setSortBy('sellingPrice');
                  setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
                  setPage(0);
                }}
                className={`px-3 py-1.5 rounded-lg font-medium transition-colors flex items-center gap-1 ${
                  sortBy === 'sellingPrice'
                    ? 'bg-chopee-orange text-white font-bold'
                    : 'bg-gray-50 text-gray-700 hover:bg-gray-100'
                }`}
              >
                <ArrowUpDown className="w-3 h-3" />
                <span>Giá: {sortDir === 'asc' && sortBy === 'sellingPrice' ? 'Tăng dần' : 'Giảm dần'}</span>
              </button>
            </div>
          </div>

          {/* Grid */}
          {loading ? (
            <div className="min-h-[300px] flex items-center justify-center text-gray-400">
              <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
            </div>
          ) : products.length === 0 ? (
            <div className="min-h-[300px] bg-white rounded-2xl border border-gray-100 p-8 text-center flex flex-col items-center justify-center">
              <p className="text-sm font-bold text-gray-700">Chưa có sản phẩm nào phù hợp bộ lọc.</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3.5">
              {products.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          )}

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
      </div>
    </div>
  );
};
export default CategoryPage;
