import React, { useEffect, useState, useCallback } from 'react';
import {
  Package,
  Plus,
  Trash2,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  Apple,
  Snowflake,
  X,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';
import { ProductSummary, PageResponse, Category } from '../../types';
import { sellerApi, catalogApi } from '../../services/api';

export const SellerProductsPage: React.FC = () => {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [loading, setLoading] = useState<boolean>(true);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  // Form State
  const [name, setName] = useState('');
  const [categoryId, setCategoryId] = useState<number>(1);
  const [sellingPrice, setSellingPrice] = useState<number>(50000);
  const [originalPrice, setOriginalPrice] = useState<number>(60000);
  const [stockQuantity, setStockQuantity] = useState<number>(100);
  const [unit, setUnit] = useState('kg');
  const [stepQuantity, setStepQuantity] = useState<number>(0.5);
  const [minOrderQuantity, setMinOrderQuantity] = useState<number>(1);
  const [storageType, setStorageType] = useState<'NORMAL' | 'FRESH' | 'FROZEN_CHILLED'>('FRESH');
  const [origin, setOrigin] = useState('Đà Lạt, Lâm Đồng');
  const [shelfLife, setShelfLife] = useState('5 ngày');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    catalogApi.getCategories().then((res) => {
      if (res.success && res.data) {
        setCategories(res.data);
      }
    }).catch(console.error);
  }, []);

  const fetchSellerProducts = useCallback(async () => {
    setLoading(true);
    try {
      const res = await sellerApi.getProducts({ page, size: 8 });
      if (res.success && res.data) {
        const pageData = res.data as PageResponse<ProductSummary>;
        setProducts(pageData.content || []);
        setTotalPages(pageData.totalPages || 1);
      }
    } catch (err) {
      console.error('Lỗi tải sản phẩm của shop:', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchSellerProducts();
  }, [fetchSellerProducts]);

  const handleOpenCreate = () => {
    setEditingId(null);
    setName('');
    setSellingPrice(50000);
    setOriginalPrice(60000);
    setStockQuantity(100);
    setUnit('kg');
    setStepQuantity(0.5);
    setMinOrderQuantity(1);
    setStorageType('FRESH');
    setOrigin('Đà Lạt, Lâm Đồng');
    setShelfLife('5 ngày');
    setThumbnailUrl('');
    setDescription('');
    setFormError(null);
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setFormError('Vui lòng nhập tên sản phẩm');
      return;
    }

    setSubmitting(true);
    setFormError(null);

    const payload = {
      name: name.trim(),
      categoryId: categoryId || 1,
      sellingPrice,
      originalPrice,
      stockQuantity,
      unit,
      stepQuantity,
      minOrderQuantity,
      storageType,
      origin: origin.trim() || undefined,
      shelfLife: shelfLife.trim() || undefined,
      thumbnailUrl: thumbnailUrl.trim() || 'https://images.unsplash.com/photo-1542838132-92c53300491e',
      description: description.trim() || undefined,
    };

    try {
      if (editingId) {
        await sellerApi.updateProduct(editingId, payload);
        setSuccessMsg('Đã cập nhật sản phẩm thành công!');
      } else {
        await sellerApi.createProduct(payload);
        setSuccessMsg('Đã thêm mới sản phẩm thành công!');
      }
      setIsModalOpen(false);
      fetchSellerProducts();
      setTimeout(() => setSuccessMsg(null), 3000);
    } catch (err: any) {
      setFormError(err.message || 'Thao tác sản phẩm thất bại');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    const confirm = window.confirm('Bạn có chắc chắn muốn xóa sản phẩm này?');
    if (!confirm) return;

    try {
      await sellerApi.deleteProduct(id);
      setSuccessMsg('Đã xóa sản phẩm thành công!');
      fetchSellerProducts();
      setTimeout(() => setSuccessMsg(null), 3000);
    } catch (err: any) {
      alert(err.message || 'Không thể xóa sản phẩm');
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">Quản Lý Sản Phẩm</h1>
          <p className="text-xs text-gray-500 mt-1">
            Đăng bán mặt hàng mới, cập nhật tồn kho và thiết lập đơn vị bán cho nông sản thực phẩm.
          </p>
        </div>

        <button
          onClick={handleOpenCreate}
          className="px-5 py-2.5 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-xs rounded-xl shadow-md flex items-center gap-2 transition-colors w-fit"
        >
          <Plus className="w-4 h-4" />
          <span>Thêm Sản Phẩm Mới</span>
        </button>
      </div>

      {successMsg && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-xl flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Product Table */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        {loading ? (
          <div className="min-h-[300px] flex items-center justify-center text-gray-400">
            <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
          </div>
        ) : products.length === 0 ? (
          <div className="p-12 text-center text-gray-400">
            <Package className="w-12 h-12 mx-auto mb-2 text-gray-300" />
            <p className="text-sm font-bold text-gray-700">Chưa có sản phẩm nào trong kho</p>
            <p className="text-xs mt-1">Bấm "Thêm Sản Phẩm Mới" để bắt đầu mở bán.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50 border-b border-gray-100 text-gray-500 font-bold uppercase tracking-wider text-[11px]">
                <tr>
                  <th className="py-3 px-4">Sản Phẩm</th>
                  <th className="py-3 px-4">Giá Bán</th>
                  <th className="py-3 px-4">Tồn Kho</th>
                  <th className="py-3 px-4">Bảo Quản</th>
                  <th className="py-3 px-4">Đã Bán</th>
                  <th className="py-3 px-4 text-right">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 text-gray-700">
                {products.map((p) => (
                  <tr key={p.id} className="hover:bg-gray-50/70 transition-colors">
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-3">
                        <img
                          src={p.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e'}
                          alt={p.name}
                          className="w-10 h-10 object-cover rounded-lg border border-gray-200"
                        />
                        <div>
                          <p className="font-bold text-gray-900 line-clamp-1">{p.name}</p>
                          <span className="text-[10px] text-gray-400">
                            Đơn vị: {p.unit} (bước {p.stepQuantity || 1})
                          </span>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-4 font-bold text-chopee-orange">
                      {formatCurrency(p.sellingPrice)}
                    </td>
                    <td className="py-3 px-4 font-semibold">
                      {p.stockQuantity} {p.unit}
                    </td>
                    <td className="py-3 px-4">
                      {p.storageType === 'FRESH' ? (
                        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1 w-fit">
                          <Apple className="w-3 h-3" /> Tươi sống
                        </span>
                      ) : p.storageType === 'FROZEN_CHILLED' ? (
                        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-cyan-50 text-cyan-700 border border-cyan-200 flex items-center gap-1 w-fit">
                          <Snowflake className="w-3 h-3" /> Đông mát
                        </span>
                      ) : (
                        <span className="text-gray-500">Thường</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-gray-500">{p.soldQuantity}</td>
                    <td className="py-3 px-4 text-right space-x-2">
                      <button
                        onClick={() => handleDelete(p.id)}
                        className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Xóa"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
            <span>Trang {page + 1} / {totalPages}</span>
            <div className="flex items-center gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Add / Edit Product Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
          <div className="bg-white rounded-3xl shadow-2xl border border-gray-100 max-w-xl w-full max-h-[90vh] flex flex-col overflow-hidden">
            {/* Header */}
            <div className="p-5 border-b border-gray-100 flex items-center justify-between">
              <h3 className="font-bold text-gray-900 text-sm">
                {editingId ? 'Chỉnh Sửa Thông Tin Sản Phẩm' : 'Thêm Sản Phẩm Mới Vào Gian Hàng'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Body */}
            <form onSubmit={handleSubmit} className="p-6 overflow-y-auto flex-1 space-y-4 text-xs">
              {formError && (
                <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  <span>{formError}</span>
                </div>
              )}

              <div>
                <label className="block font-semibold text-gray-700 mb-1">
                  Tên sản phẩm <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ví dụ: Cà Chua Beef Đà Lạt Chuẩn VietGAP"
                  className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-orange-500"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Danh mục *</label>
                  <select
                    value={categoryId}
                    onChange={(e) => setCategoryId(Number(e.target.value))}
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Loại bảo quản *</label>
                  <select
                    value={storageType}
                    onChange={(e) => setStorageType(e.target.value as any)}
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                  >
                    <option value="FRESH">🌱 Thực phẩm tươi sống</option>
                    <option value="FROZEN_CHILLED">❄️ Đông lạnh / Mát</option>
                    <option value="NORMAL">Bảo quản thường</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Giá bán (VNĐ) *</label>
                  <input
                    type="number"
                    value={sellingPrice}
                    onChange={(e) => setSellingPrice(Number(e.target.value))}
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                    required
                  />
                </div>

                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Giá gốc (gạch ngang)</label>
                  <input
                    type="number"
                    value={originalPrice}
                    onChange={(e) => setOriginalPrice(Number(e.target.value))}
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Tồn kho *</label>
                  <input
                    type="number"
                    value={stockQuantity}
                    onChange={(e) => setStockQuantity(Number(e.target.value))}
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                    required
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block font-semibold text-gray-700 mb-1">
                    Đơn vị tính & Bước nhảy (Step) *
                  </label>
                  <div className="flex flex-wrap gap-1 mb-2">
                    {['kg', 'hộp', 'thùng', 'chiếc', 'bó', 'vỉ', 'lốc', 'chai'].map((u) => (
                      <button
                        key={u}
                        type="button"
                        onClick={() => {
                          setUnit(u);
                          const isW = u === 'kg';
                          setStepQuantity(isW ? 0.5 : 1);
                          setMinOrderQuantity(isW ? 0.5 : 1);
                        }}
                        className={`px-2 py-0.5 rounded-lg text-xs font-semibold border transition-colors ${
                          unit.toLowerCase().trim() === u
                            ? 'bg-orange-50 border-chopee-orange text-chopee-orange font-bold'
                            : 'bg-gray-50 border-gray-200 text-gray-600 hover:bg-gray-100'
                        }`}
                      >
                        {u}
                      </button>
                    ))}
                  </div>
                  <div className="grid grid-cols-2 gap-2">
                    <input
                      type="text"
                      value={unit}
                      onChange={(e) => {
                        const val = e.target.value;
                        setUnit(val);
                        if (!val.toLowerCase().includes('kg') && !val.toLowerCase().includes('g')) {
                          setStepQuantity(1);
                          setMinOrderQuantity(1);
                        }
                      }}
                      placeholder="kg, thùng, chiếc..."
                      className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none text-xs"
                      required
                    />
                    <input
                      type="number"
                      step={unit.toLowerCase().includes('kg') ? '0.1' : '1'}
                      min={unit.toLowerCase().includes('kg') ? '0.1' : '1'}
                      value={stepQuantity}
                      disabled={!unit.toLowerCase().includes('kg') && !unit.toLowerCase().includes('g')}
                      onChange={(e) => setStepQuantity(Number(e.target.value))}
                      placeholder="Bước nhảy"
                      className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none text-xs disabled:opacity-60 disabled:bg-gray-100"
                      required
                    />
                  </div>
                  <p className="text-[11px] text-gray-400 mt-1">
                    {unit.toLowerCase().includes('kg') || unit.toLowerCase().includes('g')
                      ? '💡 Đơn vị cân (kg): Người mua có thể chọn đóng gói 0.5kg (500g), 1kg, 2kg hoặc bước cân lẻ.'
                      : '💡 Đơn vị đếm nguyên: Khách hàng mua số lượng nguyên chiếc (1, 2, 3...), bước nhảy cố định là 1.'}
                  </p>
                </div>
              </div>

              <div>
                <label className="block font-semibold text-gray-700 mb-1">Link Ảnh Thumbnail</label>
                <input
                  type="url"
                  value={thumbnailUrl}
                  onChange={(e) => setThumbnailUrl(e.target.value)}
                  placeholder="https://..."
                  className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Nguồn gốc / Xuất xứ</label>
                  <input
                    type="text"
                    value={origin}
                    onChange={(e) => setOrigin(e.target.value)}
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Hạn sử dụng</label>
                  <input
                    type="text"
                    value={shelfLife}
                    onChange={(e) => setShelfLife(e.target.value)}
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-6 py-2.5 bg-chopee-orange hover:bg-orange-600 text-white font-bold rounded-xl shadow-md disabled:opacity-60"
                >
                  {submitting ? 'Đang lưu...' : 'Lưu Sản Phẩm'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default SellerProductsPage;
