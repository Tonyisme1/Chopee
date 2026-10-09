import React, { useState } from 'react';
import { X, Star, AlertCircle } from 'lucide-react';
import { OrderItem } from '../types';
import { reviewApi } from '../services/api';

interface ReviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  orderItem: OrderItem | null;
  onSuccess: () => void;
}

export const ReviewModal: React.FC<ReviewModalProps> = ({
  isOpen,
  onClose,
  orderItem,
  onSuccess,
}) => {
  const [rating, setRating] = useState<number>(5);
  const [comment, setComment] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (!isOpen || !orderItem) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setLoading(true);

    try {
      const res = await reviewApi.createReview({
        productId: orderItem.productId,
        orderItemId: orderItem.id,
        rating,
        comment: comment.trim() || undefined,
      });

      if (res.success) {
        onSuccess();
        onClose();
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Không thể gửi đánh giá');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white rounded-2xl shadow-2xl border border-gray-100 max-w-md w-full overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="font-bold text-gray-900 text-sm">Đánh Giá Sản Phẩm Đã Mua</h3>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Product Preview */}
        <div className="p-4 bg-gray-50 flex items-center gap-3 border-b border-gray-100">
          <img
            src={orderItem.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e'}
            alt={orderItem.productName}
            className="w-12 h-12 rounded-lg object-cover border border-gray-200"
          />
          <div>
            <h4 className="text-xs font-bold text-gray-800 line-clamp-1">{orderItem.productName}</h4>
            <span className="text-[11px] text-gray-500">
              {orderItem.quantity} {orderItem.unit}
            </span>
          </div>
        </div>

        {/* Review Form */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {errorMsg && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* Star Selection */}
          <div className="text-center space-y-2">
            <p className="text-xs font-semibold text-gray-700">Chất lượng sản phẩm:</p>
            <div className="flex items-center justify-center gap-2">
              {[1, 2, 3, 4, 5].map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => setRating(s)}
                  className="p-1 hover:scale-125 transition-transform"
                >
                  <Star
                    className={`w-7 h-7 ${
                      s <= rating
                        ? 'fill-amber-400 text-amber-400'
                        : 'text-gray-300'
                    }`}
                  />
                </button>
              ))}
            </div>
            <p className="text-xs font-bold text-amber-600">
              {rating === 5 && 'Tuyệt vời'}
              {rating === 4 && 'Hài lòng'}
              {rating === 3 && 'Bình thường'}
              {rating === 2 && 'Không hài lòng'}
              {rating === 1 && 'Rất tệ'}
            </p>
          </div>

          {/* Comment */}
          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">
              Chia sẻ cảm nhận của bạn về sản phẩm:
            </label>
            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Sản phẩm rất tươi ngon, giao nhanh trong 2h, đóng gói cẩn thận..."
              rows={4}
              className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-orange-500"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-xl text-xs font-bold text-gray-700"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2 bg-chopee-orange hover:bg-orange-600 text-white rounded-xl text-xs font-bold shadow-md shadow-orange-500/20 disabled:opacity-60"
            >
              {loading ? 'Đang gửi...' : 'Gửi Đánh Giá'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
export default ReviewModal;
