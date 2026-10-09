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
  Copy,
  Edit3,
  Layers,
  Sparkles,
  Check,
  Tag,
} from 'lucide-react';
import { ProductSummary, PageResponse, Category } from '../../types';
import { sellerApi, catalogApi } from '../../services/api';

interface MatrixRow {
  variantName: string;
  sku: string;
  price: number;
  stockQuantity: number;
  attributes: string;
}

interface IndustryPreset {
  id: string;
  name: string;
  icon: string;
  tier1Name: string;
  tier1Options: string[];
  hasTier2: boolean;
  tier2Name: string;
  tier2Options: string[];
  unit: string;
  storageType: 'NORMAL' | 'FRESH' | 'FROZEN_CHILLED';
  defaultPrice: number;
  defaultStock: number;
}

const INDUSTRY_PRESETS: IndustryPreset[] = [
  {
    id: 'veggies',
    name: 'Rau củ / Trái cây',
    icon: '🥬',
    tier1Name: 'Loại',
    tier1Options: ['Tươi ngon', 'Khô / Sấy'],
    hasTier2: true,
    tier2Name: 'Quy cách đóng gói',
    tier2Options: ['Túi 500g', 'Túi 1.0 kg', 'Túi 2.0 kg', 'Thùng 5.0 kg'],
    unit: 'túi',
    storageType: 'FRESH',
    defaultPrice: 25000,
    defaultStock: 50,
  },
  {
    id: 'meat',
    name: 'Thịt tươi / Thủy hải sản',
    icon: '🥩',
    tier1Name: 'Phân loại thịt',
    tier1Options: ['Ba chỉ rút sườn', 'Sườn non', 'Nạc dăm'],
    hasTier2: true,
    tier2Name: 'Khối lượng khay',
    tier2Options: ['Khay 300g', 'Khay 500g', 'Khay 1.0 kg'],
    unit: 'khay',
    storageType: 'FRESH',
    defaultPrice: 65000,
    defaultStock: 30,
  },
  {
    id: 'rice',
    name: 'Gạo / Nông sản khô',
    icon: '🌾',
    tier1Name: 'Loại hạt',
    tier1Options: ['ST25 Thượng Hạng', 'Gạo Thơm Lài'],
    hasTier2: true,
    tier2Name: 'Đóng gói',
    tier2Options: ['Túi 1.0 kg', 'Bao 5.0 kg', 'Bao 10 kg', 'Bao 25 kg'],
    unit: 'bao',
    storageType: 'NORMAL',
    defaultPrice: 180000,
    defaultStock: 25,
  },
  {
    id: 'phone',
    name: 'Điện thoại / Tablet',
    icon: '📱',
    tier1Name: 'Màu sắc',
    tier1Options: ['Titan Tự Nhiên', 'Đen Midnight', 'Trắng Starlight'],
    hasTier2: true,
    tier2Name: 'Cấu hình',
    tier2Options: ['8GB / 128GB', '8GB / 256GB', '12GB / 512GB'],
    unit: 'chiếc',
    storageType: 'NORMAL',
    defaultPrice: 24990000,
    defaultStock: 15,
  },
  {
    id: 'keyboard',
    name: 'Bàn phím cơ',
    icon: '⌨️',
    tier1Name: 'Layout',
    tier1Options: ['Layout 75%', 'TKL 87 phím', 'Fullsize 108 phím'],
    hasTier2: true,
    tier2Name: 'Switch',
    tier2Options: ['Red Switch (Êm)', 'Blue Switch (Clicky)', 'Brown Switch (Khấc)'],
    unit: 'chiếc',
    storageType: 'NORMAL',
    defaultPrice: 1650000,
    defaultStock: 30,
  },
  {
    id: 'headphone',
    name: 'Tai nghe / Loa',
    icon: '🎧',
    tier1Name: 'Màu sắc',
    tier1Options: ['Đen Nhám', 'Trắng Bạc', 'Xanh Navy'],
    hasTier2: false,
    tier2Name: '',
    tier2Options: [],
    unit: 'chiếc',
    storageType: 'NORMAL',
    defaultPrice: 890000,
    defaultStock: 40,
  },
  {
    id: 'drinks',
    name: 'Đồ uống / Nước giải khát',
    icon: '🧃',
    tier1Name: 'Quy cách đóng gói',
    tier1Options: ['Lon lẻ 330ml', 'Lốc 6 lon', 'Thùng 24 lon'],
    hasTier2: false,
    tier2Name: '',
    tier2Options: [],
    unit: 'lon',
    storageType: 'NORMAL',
    defaultPrice: 12000,
    defaultStock: 100,
  },
  {
    id: 'fashion',
    name: 'Thời trang / May mặc',
    icon: '👕',
    tier1Name: 'Màu sắc',
    tier1Options: ['Trắng Basic', 'Đen Tuyền', 'Xám Tiêu'],
    hasTier2: true,
    tier2Name: 'Kích cỡ',
    tier2Options: ['Size M', 'Size L', 'Size XL'],
    unit: 'chiếc',
    storageType: 'NORMAL',
    defaultPrice: 180000,
    defaultStock: 60,
  },
];

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

  // Form Basic Info
  const [name, setName] = useState('');
  const [categoryId, setCategoryId] = useState<number>(1);
  const [sellingPrice, setSellingPrice] = useState<number>(50000);
  const [originalPrice, setOriginalPrice] = useState<number>(60000);
  const [stockQuantity, setStockQuantity] = useState<number>(100);
  const [unit, setUnit] = useState('kg');
  const [stepQuantity, setStepQuantity] = useState<number>(1);
  const [minOrderQuantity, setMinOrderQuantity] = useState<number>(1);
  const [storageType, setStorageType] = useState<'NORMAL' | 'FRESH' | 'FROZEN_CHILLED'>('FRESH');
  const [origin, setOrigin] = useState('Đà Lạt, Lâm Đồng');
  const [shelfLife, setShelfLife] = useState('5 ngày');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // Multi-tier Matrix State
  const [hasTierVariation, setHasTierVariation] = useState<boolean>(false);
  const [tier1Name, setTier1Name] = useState<string>('Loại');
  const [tier1Options, setTier1Options] = useState<string[]>([]);
  const [tier1Input, setTier1Input] = useState<string>('');

  const [hasTier2, setHasTier2] = useState<boolean>(false);
  const [tier2Name, setTier2Name] = useState<string>('Quy cách đóng gói');
  const [tier2Options, setTier2Options] = useState<string[]>([]);
  const [tier2Input, setTier2Input] = useState<string>('');

  const [matrixVariants, setMatrixVariants] = useState<MatrixRow[]>([]);
  const [bulkPrice, setBulkPrice] = useState<number | ''>('');
  const [bulkStock, setBulkStock] = useState<number | ''>('');

  useEffect(() => {
    catalogApi
      .getCategories()
      .then((res) => {
        if (res.success && res.data) {
          setCategories(res.data);
        }
      })
      .catch(console.error);
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

  // Compute Cartesian Matrix
  const computeMatrix = (
    t1Name: string,
    t1Opts: string[],
    t2Active: boolean,
    t2Name: string,
    t2Opts: string[],
    existing: MatrixRow[],
    defaultP: number,
    defaultS: number
  ): MatrixRow[] => {
    if (t1Opts.length === 0) return [];
    const existingMap = new Map<string, MatrixRow>();
    existing.forEach((r) => existingMap.set(r.variantName, r));

    const result: MatrixRow[] = [];

    if (t2Active && t2Opts.length > 0) {
      for (const opt1 of t1Opts) {
        for (const opt2 of t2Opts) {
          const vName = `${opt1} - ${opt2}`;
          const prev = existingMap.get(vName);
          result.push({
            variantName: vName,
            sku: prev?.sku || '',
            price: prev?.price !== undefined ? prev.price : defaultP,
            stockQuantity: prev?.stockQuantity !== undefined ? prev.stockQuantity : defaultS,
            attributes: JSON.stringify({ [t1Name || 'Nhóm 1']: opt1, [t2Name || 'Nhóm 2']: opt2 }),
          });
        }
      }
    } else {
      for (const opt1 of t1Opts) {
        const vName = opt1;
        const prev = existingMap.get(vName);
        result.push({
          variantName: vName,
          sku: prev?.sku || '',
          price: prev?.price !== undefined ? prev.price : defaultP,
          stockQuantity: prev?.stockQuantity !== undefined ? prev.stockQuantity : defaultS,
          attributes: JSON.stringify({ [t1Name || 'Nhóm 1']: opt1 }),
        });
      }
    }
    return result;
  };

  const handleOpenCreate = () => {
    setEditingId(null);
    setName('');
    setSellingPrice(50000);
    setOriginalPrice(60000);
    setStockQuantity(100);
    setUnit('kg');
    setStepQuantity(1);
    setMinOrderQuantity(1);
    setStorageType('FRESH');
    setOrigin('Đà Lạt, Lâm Đồng');
    setShelfLife('5 ngày');
    setThumbnailUrl('');
    setDescription('');
    setHasTierVariation(false);
    setTier1Name('Loại');
    setTier1Options([]);
    setTier1Input('');
    setHasTier2(false);
    setTier2Name('Quy cách đóng gói');
    setTier2Options([]);
    setTier2Input('');
    setMatrixVariants([]);
    setBulkPrice('');
    setBulkStock('');
    setFormError(null);
    setIsModalOpen(true);
  };

  const handleOpenEdit = async (id: number) => {
    try {
      setLoading(true);
      const res = await catalogApi.getProductById(id);
      if (res.success && res.data) {
        const p = res.data;
        setEditingId(p.id);
        setName(p.name);
        setCategoryId(p.categoryId || 1);
        setSellingPrice(p.sellingPrice);
        setOriginalPrice(p.originalPrice || p.sellingPrice);
        setStockQuantity(p.stockQuantity);
        setUnit(p.unit || 'kg');
        setStepQuantity(p.stepQuantity || 1);
        setMinOrderQuantity(p.minOrderQuantity || 1);
        setStorageType(p.storageType || 'FRESH');
        setOrigin(p.origin || '');
        setShelfLife(p.shelfLife || '');
        setThumbnailUrl(p.thumbnailUrl || '');
        setDescription(p.description || '');

        // Parse Tier Variation
        if (p.tierVariation) {
          try {
            const tiers = JSON.parse(p.tierVariation);
            if (Array.isArray(tiers) && tiers.length > 0) {
              setHasTierVariation(true);
              setTier1Name(tiers[0].name || 'Loại');
              setTier1Options(tiers[0].options || []);
              if (tiers.length > 1) {
                setHasTier2(true);
                setTier2Name(tiers[1].name || 'Quy cách đóng gói');
                setTier2Options(tiers[1].options || []);
              } else {
                setHasTier2(false);
                setTier2Name('');
                setTier2Options([]);
              }
            } else {
              setHasTierVariation(false);
            }
          } catch {
            setHasTierVariation(false);
          }
        } else {
          setHasTierVariation(false);
        }

        if (p.variants && p.variants.length > 0) {
          setMatrixVariants(
            p.variants.map((v) => ({
              variantName: v.variantName,
              sku: v.sku || '',
              price: v.price || p.sellingPrice,
              stockQuantity: v.stockQuantity || 0,
              attributes: v.attributes || '',
            }))
          );
        } else {
          setMatrixVariants([]);
        }

        setBulkPrice('');
        setBulkStock('');
        setFormError(null);
        setIsModalOpen(true);
      }
    } catch (err: any) {
      alert(err.message || 'Không thể tải thông tin sản phẩm');
    } finally {
      setLoading(false);
    }
  };

  const handleApplyPreset = (preset: IndustryPreset) => {
    setHasTierVariation(true);
    setTier1Name(preset.tier1Name);
    setTier1Options(preset.tier1Options);
    setHasTier2(preset.hasTier2);
    setTier2Name(preset.tier2Name);
    setTier2Options(preset.tier2Options);
    setUnit(preset.unit);
    setStorageType(preset.storageType);
    setSellingPrice(preset.defaultPrice);
    setOriginalPrice(Math.round(preset.defaultPrice * 1.2));
    setBulkPrice(preset.defaultPrice);
    setBulkStock(preset.defaultStock);

    const generated = computeMatrix(
      preset.tier1Name,
      preset.tier1Options,
      preset.hasTier2,
      preset.tier2Name,
      preset.tier2Options,
      [],
      preset.defaultPrice,
      preset.defaultStock
    );
    setMatrixVariants(generated);
  };

  const handleAddTier1Option = () => {
    const val = tier1Input.trim();
    if (!val || tier1Options.includes(val)) return;
    const nextOpts = [...tier1Options, val];
    setTier1Options(nextOpts);
    setTier1Input('');
    const curP = bulkPrice !== '' ? Number(bulkPrice) : sellingPrice;
    const curS = bulkStock !== '' ? Number(bulkStock) : 50;
    setMatrixVariants(
      computeMatrix(tier1Name, nextOpts, hasTier2, tier2Name, tier2Options, matrixVariants, curP, curS)
    );
  };

  const handleRemoveTier1Option = (idx: number) => {
    const nextOpts = tier1Options.filter((_, i) => i !== idx);
    setTier1Options(nextOpts);
    const curP = bulkPrice !== '' ? Number(bulkPrice) : sellingPrice;
    const curS = bulkStock !== '' ? Number(bulkStock) : 50;
    setMatrixVariants(
      computeMatrix(tier1Name, nextOpts, hasTier2, tier2Name, tier2Options, matrixVariants, curP, curS)
    );
  };

  const handleAddTier2Option = () => {
    const val = tier2Input.trim();
    if (!val || tier2Options.includes(val)) return;
    const nextOpts = [...tier2Options, val];
    setTier2Options(nextOpts);
    setTier2Input('');
    const curP = bulkPrice !== '' ? Number(bulkPrice) : sellingPrice;
    const curS = bulkStock !== '' ? Number(bulkStock) : 50;
    setMatrixVariants(
      computeMatrix(tier1Name, tier1Options, true, tier2Name, nextOpts, matrixVariants, curP, curS)
    );
  };

  const handleRemoveTier2Option = (idx: number) => {
    const nextOpts = tier2Options.filter((_, i) => i !== idx);
    setTier2Options(nextOpts);
    const curP = bulkPrice !== '' ? Number(bulkPrice) : sellingPrice;
    const curS = bulkStock !== '' ? Number(bulkStock) : 50;
    setMatrixVariants(
      computeMatrix(tier1Name, tier1Options, true, tier2Name, nextOpts, matrixVariants, curP, curS)
    );
  };

  const handleBulkApply = () => {
    if (bulkPrice === '' && bulkStock === '') return;
    setMatrixVariants((prev) =>
      prev.map((r) => ({
        ...r,
        price: bulkPrice !== '' ? Math.max(0, Number(bulkPrice)) : r.price,
        stockQuantity: bulkStock !== '' ? Math.max(0, Math.round(Number(bulkStock))) : r.stockQuantity,
      }))
    );
    if (bulkPrice !== '') setSellingPrice(Math.max(0, Number(bulkPrice)));
  };

  const handleUpdateMatrixRow = (
    idx: number,
    field: 'price' | 'stockQuantity' | 'sku',
    val: any
  ) => {
    setMatrixVariants((prev) => {
      const next = [...prev];
      next[idx] = {
        ...next[idx],
        [field]: val,
      };
      return next;
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setFormError('Vui lòng nhập tên sản phẩm');
      return;
    }

    if (hasTierVariation && matrixVariants.length === 0) {
      setFormError('Bạn đã bật phân loại hàng nhưng chưa thêm tùy chọn nào vào ma trận.');
      return;
    }

    setSubmitting(true);
    setFormError(null);

    let finalSellingPrice = sellingPrice;
    let finalStockQuantity = stockQuantity;
    let tierVariationJson: string | undefined = undefined;
    let variantsPayload: any[] | undefined = undefined;

    if (hasTierVariation && matrixVariants.length > 0) {
      const tiers = [];
      if (tier1Name && tier1Options.length > 0) {
        tiers.push({ name: tier1Name.trim(), options: tier1Options });
      }
      if (hasTier2 && tier2Name && tier2Options.length > 0) {
        tiers.push({ name: tier2Name.trim(), options: tier2Options });
      }
      tierVariationJson = JSON.stringify(tiers);

      variantsPayload = matrixVariants.map((m) => ({
        variantName: m.variantName,
        sku: m.sku.trim() || undefined,
        price: Number(m.price),
        stockQuantity: Number(m.stockQuantity),
        attributes: m.attributes,
      }));

      const minPrice = Math.min(...matrixVariants.map((m) => Number(m.price)));
      if (minPrice > 0) finalSellingPrice = minPrice;
      finalStockQuantity = matrixVariants.reduce((sum, m) => sum + Number(m.stockQuantity), 0);
    }

    const payload = {
      name: name.trim(),
      categoryId: categoryId || 1,
      sellingPrice: finalSellingPrice,
      originalPrice,
      stockQuantity: finalStockQuantity,
      unit,
      stepQuantity,
      minOrderQuantity,
      storageType,
      origin: origin.trim() || undefined,
      shelfLife: shelfLife.trim() || undefined,
      thumbnailUrl: thumbnailUrl.trim() || 'https://images.unsplash.com/photo-1542838132-92c53300491e',
      description: description.trim() || undefined,
      tierVariation: tierVariationJson,
      variants: variantsPayload,
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

  const handleDuplicate = async (id: number) => {
    try {
      setLoading(true);
      const res = await sellerApi.duplicateProduct(id);
      if (res.success && res.data) {
        setSuccessMsg(`Nhân bản thành công! Đã tạo "${res.data.name}".`);
        await fetchSellerProducts();
        setTimeout(() => setSuccessMsg(null), 3500);
      }
    } catch (err: any) {
      alert(err.message || 'Lỗi khi nhân bản sản phẩm');
    } finally {
      setLoading(false);
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
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">Quản Lý Sản Phẩm & Kho Hàng</h1>
          <p className="text-xs text-gray-500 mt-1">
            Đăng bán đa ngành hàng (nông sản, thực phẩm tươi, thiết bị, gia dụng), tạo ma trận phân loại siêu tốc và nhân bản sản phẩm 1-click.
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
                    <td className="py-3 px-4 text-right space-x-1">
                      <button
                        onClick={() => handleOpenEdit(p.id)}
                        className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors inline-flex"
                        title="Chỉnh sửa sản phẩm"
                      >
                        <Edit3 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDuplicate(p.id)}
                        className="p-1.5 text-gray-500 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors inline-flex"
                        title="Nhân bản sản phẩm nhanh (1-Click Duplicate)"
                      >
                        <Copy className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(p.id)}
                        className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors inline-flex"
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
          <div className="bg-white rounded-3xl shadow-2xl border border-gray-100 max-w-4xl w-full max-h-[92vh] flex flex-col overflow-hidden">
            {/* Header */}
            <div className="p-5 border-b border-gray-100 flex items-center justify-between bg-gradient-to-r from-orange-50/50 to-white">
              <div>
                <h3 className="font-extrabold text-gray-900 text-sm flex items-center gap-2">
                  <Layers className="w-4 h-4 text-chopee-orange" />
                  {editingId ? 'Chỉnh Sửa Thông Tin Sản Phẩm & Phân Loại' : 'Đăng Sản Phẩm Mới & Thiết Lập Phân Loại'}
                </h3>
                <p className="text-[11px] text-gray-500 mt-0.5">
                  Quy ước sàn Chopee: Số lượng đặt mua luôn là số nguyên (1, 2, 3...). Các sản phẩm dạng kg, lít, bó được đóng gói theo từng quy cách/phân loại để khách chọn.
                </p>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Body */}
            <form onSubmit={handleSubmit} className="p-6 overflow-y-auto flex-1 space-y-5 text-xs">
              {formError && (
                <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  <span>{formError}</span>
                </div>
              )}

              {/* Basic Information */}
              <div className="space-y-4">
                <h4 className="font-bold text-gray-900 text-xs uppercase tracking-wider text-orange-600 border-b border-orange-100 pb-1 flex items-center gap-1.5">
                  <Tag className="w-3.5 h-3.5" /> 1. Thông Tin Cơ Bản
                </h4>

                <div>
                  <label className="block font-semibold text-gray-700 mb-1">
                    Tên sản phẩm <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Ví dụ: Cà Chua Beef Đà Lạt Chuẩn VietGAP, Bàn Phím Cơ Keychron K2 Pro..."
                    className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-orange-500 font-medium"
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
                      <option value="FRESH">🌱 Thực phẩm tươi sống (Hỏa tốc 2H)</option>
                      <option value="FROZEN_CHILLED">❄️ Đông lạnh / Mát (Xe lạnh)</option>
                      <option value="NORMAL">📦 Bảo quản thường (Toàn quốc)</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div>
                    <label className="block font-semibold text-gray-700 mb-1">
                      Đơn vị tính cơ bản *
                    </label>
                    <input
                      type="text"
                      value={unit}
                      onChange={(e) => setUnit(e.target.value)}
                      placeholder="kg, gói, túi, bịch, chiếc, bao..."
                      className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none text-xs"
                      required
                    />
                  </div>

                  <div>
                    <label className="block font-semibold text-gray-700 mb-1">
                      Giá bán mặc định (VNĐ) *
                    </label>
                    <input
                      type="number"
                      value={sellingPrice}
                      onChange={(e) => setSellingPrice(Number(e.target.value))}
                      className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none font-bold text-chopee-orange"
                      required
                    />
                  </div>

                  <div>
                    <label className="block font-semibold text-gray-700 mb-1">
                      Giá gốc niêm yết (gạch ngang)
                    </label>
                    <input
                      type="number"
                      value={originalPrice}
                      onChange={(e) => setOriginalPrice(Number(e.target.value))}
                      className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                    />
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
                      placeholder="Đà Lạt, Lâm Đồng; Chính Hãng..."
                      className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="block font-semibold text-gray-700 mb-1">Hạn sử dụng / Bảo hành</label>
                    <input
                      type="text"
                      value={shelfLife}
                      onChange={(e) => setShelfLife(e.target.value)}
                      placeholder="5 ngày; 12 tháng..."
                      className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none"
                    />
                  </div>
                </div>
              </div>

              {/* Multi-tier Matrix Section */}
              <div className="space-y-4 pt-3">
                <div className="flex items-center justify-between border-b border-orange-100 pb-1">
                  <h4 className="font-bold text-gray-900 text-xs uppercase tracking-wider text-orange-600 flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5" /> 2. Phân Loại Hàng Đa Tầng & Nhập Nhanh (Matrix)
                  </h4>
                  <label className="flex items-center gap-2 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      checked={hasTierVariation}
                      onChange={(e) => {
                        const checked = e.target.checked;
                        setHasTierVariation(checked);
                        if (!checked) {
                          setMatrixVariants([]);
                        } else if (tier1Options.length > 0) {
                          setMatrixVariants(
                            computeMatrix(
                              tier1Name,
                              tier1Options,
                              hasTier2,
                              tier2Name,
                              tier2Options,
                              [],
                              sellingPrice,
                              stockQuantity
                            )
                          );
                        }
                      }}
                      className="rounded text-chopee-orange focus:ring-chopee-orange w-4 h-4 cursor-pointer"
                    />
                    <span className="font-bold text-xs text-gray-700">Bật phân loại nhiều nhóm (2 cấp)</span>
                  </label>
                </div>

                {/* Industry Presets Bar */}
                <div className="bg-orange-50/60 p-3.5 rounded-2xl border border-orange-100 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-[11px] text-orange-800 flex items-center gap-1">
                      <Sparkles className="w-3 h-3 text-chopee-orange" /> Mẫu 1-Click Theo Ngành Hàng (Nhập Nhanh Cực Tốc):
                    </span>
                    <span className="text-[10px] text-gray-500">Bấm mẫu để tự động điền cấu trúc</span>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {INDUSTRY_PRESETS.map((p) => (
                      <button
                        key={p.id}
                        type="button"
                        onClick={() => handleApplyPreset(p)}
                        className="px-2.5 py-1.5 bg-white hover:bg-orange-500 hover:text-white text-gray-700 border border-orange-200/80 rounded-xl text-[11px] font-bold shadow-sm transition-all flex items-center gap-1.5"
                      >
                        <span>{p.icon}</span>
                        <span>{p.name}</span>
                      </button>
                    ))}
                  </div>
                </div>

                {hasTierVariation && (
                  <div className="space-y-4 bg-gray-50/70 p-4 rounded-2xl border border-gray-200">
                    {/* Tier 1 Definition */}
                    <div className="bg-white p-3.5 rounded-xl border border-gray-200 space-y-2.5">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-gray-800 text-xs">Nhóm phân loại 1:</span>
                        <input
                          type="text"
                          value={tier1Name}
                          onChange={(e) => setTier1Name(e.target.value)}
                          placeholder="Ví dụ: Loại, Màu sắc, Phân loại thịt, Layout..."
                          className="px-2.5 py-1 bg-gray-50 border border-gray-200 rounded-lg text-xs font-semibold focus:bg-white focus:outline-none flex-1 max-w-xs"
                        />
                      </div>

                      <div className="flex flex-wrap items-center gap-1.5">
                        {tier1Options.map((opt, idx) => (
                          <span
                            key={idx}
                            className="px-2.5 py-1 bg-orange-50 text-chopee-orange border border-orange-200 rounded-lg text-xs font-bold flex items-center gap-1"
                          >
                            <span>{opt}</span>
                            <button
                              type="button"
                              onClick={() => handleRemoveTier1Option(idx)}
                              className="hover:text-red-600 transition-colors"
                            >
                              <X className="w-3 h-3" />
                            </button>
                          </span>
                        ))}

                        <div className="flex items-center gap-1">
                          <input
                            type="text"
                            value={tier1Input}
                            onChange={(e) => setTier1Input(e.target.value)}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter') {
                                e.preventDefault();
                                handleAddTier1Option();
                              }
                            }}
                            placeholder="Thêm tùy chọn (Enter)..."
                            className="px-2.5 py-1 bg-gray-50 border border-gray-200 rounded-lg text-xs focus:bg-white focus:outline-none w-36"
                          />
                          <button
                            type="button"
                            onClick={handleAddTier1Option}
                            className="px-2 py-1 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg text-xs font-bold"
                          >
                            +
                          </button>
                        </div>
                      </div>
                    </div>

                    {/* Tier 2 Definition */}
                    <div className="bg-white p-3.5 rounded-xl border border-gray-200 space-y-2.5">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2 flex-1">
                          <span className="font-bold text-gray-800 text-xs">Nhóm phân loại 2:</span>
                          {hasTier2 ? (
                            <input
                              type="text"
                              value={tier2Name}
                              onChange={(e) => setTier2Name(e.target.value)}
                              placeholder="Ví dụ: Quy cách đóng gói, Cấu hình, Switch, Size..."
                              className="px-2.5 py-1 bg-gray-50 border border-gray-200 rounded-lg text-xs font-semibold focus:bg-white focus:outline-none flex-1 max-w-xs"
                            />
                          ) : (
                            <button
                              type="button"
                              onClick={() => {
                                setHasTier2(true);
                                setTier2Name('Quy cách đóng gói');
                              }}
                              className="px-3 py-1 bg-orange-100 hover:bg-orange-200 text-chopee-orange font-bold text-xs rounded-lg transition-colors flex items-center gap-1"
                            >
                              <Plus className="w-3 h-3" /> Thêm nhóm phân loại 2 (Chuẩn 2 cấp)
                            </button>
                          )}
                        </div>
                        {hasTier2 && (
                          <button
                            type="button"
                            onClick={() => {
                              setHasTier2(false);
                              setTier2Options([]);
                              const curP = bulkPrice !== '' ? Number(bulkPrice) : sellingPrice;
                              const curS = bulkStock !== '' ? Number(bulkStock) : 50;
                              setMatrixVariants(
                                computeMatrix(tier1Name, tier1Options, false, '', [], matrixVariants, curP, curS)
                              );
                            }}
                            className="text-xs text-red-500 hover:underline font-semibold"
                          >
                            Xóa nhóm 2
                          </button>
                        )}
                      </div>

                      {hasTier2 && (
                        <div className="flex flex-wrap items-center gap-1.5">
                          {tier2Options.map((opt, idx) => (
                            <span
                              key={idx}
                              className="px-2.5 py-1 bg-orange-50 text-chopee-orange border border-orange-200 rounded-lg text-xs font-bold flex items-center gap-1"
                            >
                              <span>{opt}</span>
                              <button
                                type="button"
                                onClick={() => handleRemoveTier2Option(idx)}
                                className="hover:text-red-600 transition-colors"
                              >
                                <X className="w-3 h-3" />
                              </button>
                            </span>
                          ))}

                          <div className="flex items-center gap-1">
                            <input
                              type="text"
                              value={tier2Input}
                              onChange={(e) => setTier2Input(e.target.value)}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                  e.preventDefault();
                                  handleAddTier2Option();
                                }
                              }}
                              placeholder="Thêm tùy chọn (Enter)..."
                              className="px-2.5 py-1 bg-gray-50 border border-gray-200 rounded-lg text-xs focus:bg-white focus:outline-none w-36"
                            />
                            <button
                              type="button"
                              onClick={handleAddTier2Option}
                              className="px-2 py-1 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg text-xs font-bold"
                            >
                              +
                            </button>
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Bulk Apply Bar */}
                    {matrixVariants.length > 0 && (
                      <div className="bg-orange-50 p-3 rounded-xl border border-orange-200 flex flex-wrap items-center justify-between gap-3">
                        <div className="flex items-center gap-2">
                          <span className="font-extrabold text-orange-900 text-xs flex items-center gap-1">
                            <Check className="w-4 h-4 text-emerald-600" /> Áp dụng hàng loạt:
                          </span>
                          <input
                            type="number"
                            placeholder="Giá bán chung"
                            value={bulkPrice}
                            onChange={(e) => setBulkPrice(e.target.value === '' ? '' : Number(e.target.value))}
                            className="px-2.5 py-1 bg-white border border-gray-300 rounded-lg text-xs w-28 focus:outline-none focus:ring-1 focus:ring-orange-500"
                          />
                          <input
                            type="number"
                            placeholder="Kho chung"
                            value={bulkStock}
                            onChange={(e) => setBulkStock(e.target.value === '' ? '' : Number(e.target.value))}
                            className="px-2.5 py-1 bg-white border border-gray-300 rounded-lg text-xs w-24 focus:outline-none focus:ring-1 focus:ring-orange-500"
                          />
                        </div>
                        <button
                          type="button"
                          onClick={handleBulkApply}
                          className="px-4 py-1.5 bg-chopee-orange hover:bg-orange-600 text-white font-bold text-xs rounded-xl shadow-sm transition-colors"
                        >
                          Áp dụng cho tất cả ({matrixVariants.length} phân loại)
                        </button>
                      </div>
                    )}

                    {/* Matrix Variants Table */}
                    {matrixVariants.length > 0 && (
                      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
                        <div className="p-2.5 bg-gray-50 border-b border-gray-200 flex items-center justify-between">
                          <span className="font-bold text-gray-800 text-xs">
                            Danh sách ma trận biến thể ({matrixVariants.length} dòng):
                          </span>
                          <span className="text-[11px] text-gray-500">
                            Tổng tồn kho: {matrixVariants.reduce((s, r) => s + (Number(r.stockQuantity) || 0), 0)} {unit}
                          </span>
                        </div>
                        <div className="max-h-60 overflow-y-auto">
                          <table className="w-full text-left text-xs">
                            <thead className="bg-gray-50 border-b border-gray-200 text-gray-500 uppercase text-[10px]">
                              <tr>
                                <th className="py-2 px-3">Tên Phân Loại</th>
                                <th className="py-2 px-3">Mã SKU</th>
                                <th className="py-2 px-3">Giá Bán (VNĐ)</th>
                                <th className="py-2 px-3">Tồn Kho</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                              {matrixVariants.map((row, idx) => (
                                <tr key={idx} className="hover:bg-gray-50">
                                  <td className="py-2 px-3 font-semibold text-gray-900">
                                    {row.variantName}
                                  </td>
                                  <td className="py-2 px-3">
                                    <input
                                      type="text"
                                      value={row.sku}
                                      onChange={(e) => handleUpdateMatrixRow(idx, 'sku', e.target.value)}
                                      placeholder="Mã SKU (tùy chọn)"
                                      className="px-2 py-1 bg-gray-50 border border-gray-200 rounded-md text-xs w-36 focus:bg-white focus:outline-none"
                                    />
                                  </td>
                                  <td className="py-2 px-3">
                                    <input
                                      type="number"
                                      value={row.price}
                                      onChange={(e) => handleUpdateMatrixRow(idx, 'price', Number(e.target.value))}
                                      className="px-2 py-1 bg-gray-50 border border-gray-200 rounded-md text-xs w-28 focus:bg-white focus:outline-none font-bold text-chopee-orange"
                                    />
                                  </td>
                                  <td className="py-2 px-3">
                                    <input
                                      type="number"
                                      value={row.stockQuantity}
                                      onChange={(e) =>
                                        handleUpdateMatrixRow(idx, 'stockQuantity', Math.round(Number(e.target.value)))
                                      }
                                      className="px-2 py-1 bg-gray-50 border border-gray-200 rounded-md text-xs w-20 focus:bg-white focus:outline-none"
                                    />
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Action Buttons */}
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
