import React, { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Sparkles,
  Flame,
  Apple,
  Filter,
  ArrowUpDown,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
} from 'lucide-react';
import { ProductSummary, Category } from '../types';
import { catalogApi } from '../services/api';
import { ProductCard } from '../components/ProductCard';
import { CategoryNav } from '../components/CategoryNav';

export const HomePage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const keywordFromUrl = searchParams.get('keyword') || '';

  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [freshProducts, setFreshProducts] = useState<ProductSummary[]>([]);
  const [hotDeals, setHotDeals] = useState<ProductSummary[]>([]);

  // Filters & Pagination State
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>(undefined);
  const [selectedStorage, setSelectedStorage] = useState<string | undefined>(undefined);
  const [sortBy, setSortBy] = useState<string>('soldQuantity');
  const [sortDir, setSortDir] = useState<string>('desc');
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);

  // Fetch Categories
  useEffect(() => {
    catalogApi.getCategories().then((res) => {
      if (res.success && res.data) {
        setCategories(res.data);
      }
    }).catch(console.error);
  }, []);

  // Fetch Featured Fresh and Hot Deals on Mount
  useEffect(() => {
    // Top Fresh Produce
    catalogApi.getProducts({ storageType: 'FRESH', size: 6, sortBy: 'soldQuantity', sortDir: 'desc' })
      .then((res) => {
        if (res.success && res.data) {
          setFreshProducts(res.data.content || []);
        }
      }).catch(console.error);

    // Top Hot Deals (Discounted)
    catalogApi.getProducts({ size: 6, sortBy: 'soldQuantity', sortDir: 'desc' })
      .then((res) => {
        if (res.success && res.data) {
          const sorted = [...(res.data.content || [])].sort(
            (a, b) => (b.discountPercent || 0) - (a.discountPercent || 0)
          );
          setHotDeals(sorted);
        }
      }).catch(console.error);
  }, []);

  // Fetch Main Paginated Products
  const fetchMainProducts = useCallback(async () => {
    setLoading(true);
    try {
      const res = await catalogApi.getProducts({
        keyword: keywordFromUrl || undefined,
        categoryId: selectedCategory,
        storageType: selectedStorage,
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
      console.error('Lỗi tải sản phẩm:', err);
    } finally {
      setLoading(false);
    }
  }, [keywordFromUrl, selectedCategory, selectedStorage, sortBy, sortDir, page]);

  useEffect(() => {
    fetchMainProducts();
  }, [fetchMainProducts]);

  return (
    <div className="space-y-8">
      {/* Hero Banner Showcase */}
      {!keywordFromUrl && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Main Hero Slider */}
          <div className="md:col-span-2 rounded-2xl bg-gradient-to-r from-orange-500 via-orange-600 to-amber-500 text-white p-8 md:p-10 shadow-md relative overflow-hidden flex flex-col justify-between min-h-[240px]">
            <div className="relative z-10 max-w-lg">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/20 backdrop-blur-sm text-xs font-bold mb-3">
                <Sparkles className="w-3.5 h-3.5" /> Chợ Nông Sản & Bách Hóa Toàn Diện
              </span>
              <h1 className="text-2xl md:text-4xl font-black tracking-tight leading-snug">
                Siêu Chợ Nông Sản Tươi Ngon & Thiết Bị Gia Đình
              </h1>
              <p className="text-orange-100 text-xs md:text-sm mt-2 font-medium">
                Giao hỏa tốc 2 giờ thực phẩm chuẩn VietGAP, nước ngọt, bia thùng và phụ kiện chính hãng.
              </p>
            </div>
            <div className="relative z-10 mt-6 flex items-center gap-3">
              <span className="text-xs bg-black/25 px-3 py-1.5 rounded-lg backdrop-blur-sm font-semibold">
                Miễn phí vận chuyển đơn từ 100k
              </span>
              <span className="text-xs bg-yellow-400 text-red-900 px-3 py-1.5 rounded-lg font-black shadow-sm">
                Voucher giảm đến 50k
              </span>
            </div>
          </div>

          {/* Side Mini Banners */}
          <div className="grid grid-rows-2 gap-4">
            <div className="bg-emerald-600 rounded-2xl p-5 text-white flex flex-col justify-between shadow-sm">
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider bg-emerald-700 px-2 py-0.5 rounded">
                  Gian Hàng Xanh
                </span>
                <h3 className="font-extrabold text-base mt-1">Rau Củ Đà Lạt Chuẩn VietGAP</h3>
                <p className="text-emerald-100 text-xs mt-0.5">Thu hoạch trong ngày, tươi ngon từng bữa ăn.</p>
              </div>
              <span className="text-xs font-bold text-yellow-300">Khám phá ngay →</span>
            </div>

            <div className="bg-indigo-600 rounded-2xl p-5 text-white flex flex-col justify-between shadow-sm">
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider bg-indigo-700 px-2 py-0.5 rounded">
                  Công Nghệ & Gia Dụng
                </span>
                <h3 className="font-extrabold text-base mt-1">Gia Dụng Thông Minh Philips</h3>
                <p className="text-indigo-100 text-xs mt-0.5">Bảo hành 24 tháng chính hãng toàn sàn.</p>
              </div>
              <span className="text-xs font-bold text-yellow-300">Xem ưu đãi →</span>
            </div>
          </div>
        </div>
      )}

      {/* Category Navigation Bar */}
      <CategoryNav
        categories={categories}
        activeCategoryId={selectedCategory}
        onSelectCategory={(id) => {
          setSelectedCategory(id);
          setPage(0);
        }}
      />

      {/* Section 1: Fresh Produce Highlight */}
      {!keywordFromUrl && !selectedCategory && freshProducts.length > 0 && (
        <section className="bg-gradient-to-b from-emerald-50/60 to-white rounded-2xl border border-emerald-100/80 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-emerald-600 text-white flex items-center justify-center font-bold">
                <Apple className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-base font-black text-gray-900 tracking-tight flex items-center gap-2">
                  <span>Chợ Nông Sản & Thực Phẩm Tươi Sống</span>
                  <span className="text-[10px] font-bold px-2 py-0.5 bg-emerald-600 text-white rounded-full">
                    Giao 2H
                  </span>
                </h2>
                <p className="text-xs text-gray-500">Được phân phối trực tiếp từ các hợp tác xã nông sản sạch</p>
              </div>
            </div>

            <button
              onClick={() => {
                setSelectedStorage('FRESH');
                window.scrollTo({ top: 600, behavior: 'smooth' });
              }}
              className="text-xs text-emerald-700 font-bold hover:underline"
            >
              Xem tất cả ({freshProducts.length}) →
            </button>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3.5">
            {freshProducts.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </section>
      )}

      {/* Section 2: Flash Deals / Hot Discounts */}
      {!keywordFromUrl && !selectedCategory && hotDeals.length > 0 && (
        <section className="bg-gradient-to-b from-orange-50/70 to-white rounded-2xl border border-orange-100 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-red-600 text-white flex items-center justify-center font-bold animate-pulse">
                <Flame className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-base font-black text-gray-900 tracking-tight flex items-center gap-2">
                  <span>Flash Sale - Giảm Giá Sốc Hôm Nay</span>
                </h2>
                <p className="text-xs text-gray-500">Săn hàng ngàn deal hời giá cực ưu đãi</p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3.5">
            {hotDeals.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </section>
      )}

      {/* Section 3: Main Paginated Catalog */}
      <section className="space-y-4">
        {/* Controls and Sort Filters Header */}
        <div className="bg-white rounded-2xl border border-gray-100 p-4 shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-chopee-orange" />
            <span className="text-sm font-bold text-gray-800">
              {keywordFromUrl ? `Kết quả tìm kiếm cho "${keywordFromUrl}"` : 'Tất Cả Sản Phẩm Chopee'}
            </span>
            <span className="text-xs text-gray-400">({totalElements} sản phẩm)</span>
          </div>

          {/* Filter Pills */}
          <div className="flex items-center flex-wrap gap-2 text-xs">
            {/* Storage Type Filter */}
            <select
              value={selectedStorage || ''}
              onChange={(e) => {
                setSelectedStorage(e.target.value || undefined);
                setPage(0);
              }}
              className="px-3 py-1.5 bg-gray-50 border border-gray-200 rounded-lg text-gray-700 font-medium focus:outline-none focus:ring-1 focus:ring-orange-500"
            >
              <option value="">Tất cả loại bảo quản</option>
              <option value="FRESH">🌱 Chỉ đồ tươi sống</option>
              <option value="FROZEN_CHILLED">❄️ Đồ đông / mát</option>
              <option value="NORMAL">Hàng bảo quản thường</option>
            </select>

            {/* Sort Options */}
            <div className="flex items-center gap-1 bg-gray-50 border border-gray-200 rounded-lg p-0.5">
              <button
                onClick={() => {
                  setSortBy('soldQuantity');
                  setSortDir('desc');
                  setPage(0);
                }}
                className={`px-3 py-1 rounded-md font-medium transition-colors ${
                  sortBy === 'soldQuantity'
                    ? 'bg-chopee-orange text-white shadow-sm font-bold'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Bán Chạy
              </button>

              <button
                onClick={() => {
                  setSortBy('createdAt');
                  setSortDir('desc');
                  setPage(0);
                }}
                className={`px-3 py-1 rounded-md font-medium transition-colors ${
                  sortBy === 'createdAt'
                    ? 'bg-chopee-orange text-white shadow-sm font-bold'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Mới Nhất
              </button>

              <button
                onClick={() => {
                  setSortBy('sellingPrice');
                  setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
                  setPage(0);
                }}
                className={`px-3 py-1 rounded-md font-medium transition-colors flex items-center gap-1 ${
                  sortBy === 'sellingPrice'
                    ? 'bg-chopee-orange text-white shadow-sm font-bold'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                <ArrowUpDown className="w-3 h-3" />
                <span>Giá: {sortDir === 'asc' && sortBy === 'sellingPrice' ? 'Tăng dần' : 'Giảm dần'}</span>
              </button>
            </div>
          </div>
        </div>

        {/* Product Grid */}
        {loading ? (
          <div className="min-h-[300px] flex flex-col items-center justify-center text-gray-400 gap-2">
            <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
            <span className="text-xs font-medium">Đang tải danh sách sản phẩm...</span>
          </div>
        ) : products.length === 0 ? (
          <div className="min-h-[300px] bg-white rounded-2xl border border-gray-100 p-8 text-center flex flex-col items-center justify-center">
            <p className="text-base font-bold text-gray-700 mb-1">Không tìm thấy sản phẩm phù hợp</p>
            <p className="text-xs text-gray-400 mb-4">Hãy thử tìm với từ khóa khác hoặc xóa bộ lọc</p>
            <button
              onClick={() => {
                setSelectedCategory(undefined);
                setSelectedStorage(undefined);
                setPage(0);
              }}
              className="px-4 py-2 bg-chopee-orange text-white text-xs font-bold rounded-xl shadow-sm"
            >
              Xem tất cả sản phẩm
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3.5">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 pt-6">
            <button
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="p-2 rounded-lg border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronLeft className="w-4 h-4 text-gray-600" />
            </button>

            <span className="text-xs font-semibold text-gray-700 px-3">
              Trang {page + 1} / {totalPages}
            </span>

            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="p-2 rounded-lg border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronRight className="w-4 h-4 text-gray-600" />
            </button>
          </div>
        )}
      </section>
    </div>
  );
};
export default HomePage;
