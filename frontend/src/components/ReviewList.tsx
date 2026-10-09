import React, { useEffect, useState, useCallback } from 'react';
import { Star, MessageSquareQuote, ChevronLeft, ChevronRight } from 'lucide-react';
import { Review, PageResponse } from '../types';
import { reviewApi } from '../services/api';

interface ReviewListProps {
  productId: number;
  ratingAvg: number;
  reviewCount: number;
}

export const ReviewList: React.FC<ReviewListProps> = ({
  productId,
  ratingAvg,
  reviewCount,
}) => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [loading, setLoading] = useState<boolean>(true);

  const fetchReviews = useCallback(async () => {
    setLoading(true);
    try {
      const res = await reviewApi.getProductReviews(productId, { page, size: 5 });
      if (res.success && res.data) {
        const data = res.data as PageResponse<Review>;
        setReviews(data.content || []);
        setTotalPages(data.totalPages || 1);
      }
    } catch (err) {
      console.error('Lỗi tải đánh giá:', err);
    } finally {
      setLoading(false);
    }
  }, [productId, page]);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  return (
    <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm mt-8">
      <h3 className="text-base font-bold text-gray-900 mb-6 uppercase tracking-wider">
        Đánh Giá & Nhận Xét Từ Khách Hàng
      </h3>

      {/* Rating Overview Box */}
      <div className="p-5 rounded-2xl bg-orange-50/60 border border-orange-100 flex flex-col md:flex-row items-center gap-6 mb-8">
        <div className="text-center md:text-left flex flex-col items-center md:items-start">
          <div className="flex items-baseline gap-1 text-chopee-orange">
            <span className="text-4xl font-black">{ratingAvg ? ratingAvg.toFixed(1) : '5.0'}</span>
            <span className="text-base font-bold text-gray-500">trên 5</span>
          </div>
          <div className="flex items-center gap-1 my-1.5 text-amber-400">
            {[1, 2, 3, 4, 5].map((s) => (
              <Star
                key={s}
                className={`w-5 h-5 ${
                  s <= Math.round(ratingAvg || 5)
                    ? 'fill-amber-400 text-amber-400'
                    : 'text-gray-300'
                }`}
              />
            ))}
          </div>
          <p className="text-xs text-gray-500 font-medium">{reviewCount} lượt đánh giá thực tế</p>
        </div>

        <div className="border-t md:border-t-0 md:border-l border-orange-200/60 pt-4 md:pt-0 md:pl-6 text-xs text-gray-600 space-y-1">
          <p className="font-semibold text-gray-800">✅ 100% Đánh giá từ người mua đã nhận hàng</p>
          <p className="text-gray-500">
            Hệ thống chỉ cho phép khách hàng đã mua và nhận hàng thành công viết đánh giá để đảm bảo tính khách quan và uy tín của gian hàng.
          </p>
        </div>
      </div>

      {/* Reviews Content */}
      {loading ? (
        <p className="text-center py-8 text-xs text-gray-400">Đang tải nhận xét...</p>
      ) : reviews.length === 0 ? (
        <div className="text-center py-10 text-gray-400">
          <p className="text-sm font-semibold">Chưa có đánh giá nào cho sản phẩm này.</p>
          <p className="text-xs mt-1">Hãy là người đầu tiên mua và trải nghiệm!</p>
        </div>
      ) : (
        <div className="divide-y divide-gray-100">
          {reviews.map((r) => (
            <div key={r.id} className="py-5 first:pt-0 last:pb-0">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-orange-100 text-chopee-orange font-bold text-xs flex items-center justify-center">
                    U
                  </div>
                  <div>
                    <span className="text-xs font-bold text-gray-800 block">Khách hàng ẩn danh</span>
                    <span className="text-[10px] text-gray-400">
                      {new Date(r.createdAt).toLocaleDateString('vi-VN')}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-0.5 text-amber-400">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <Star
                      key={s}
                      className={`w-3.5 h-3.5 ${
                        s <= r.rating ? 'fill-amber-400 text-amber-400' : 'text-gray-200'
                      }`}
                    />
                  ))}
                </div>
              </div>

              {/* Review Comment */}
              <p className="text-xs text-gray-700 leading-relaxed mt-2 font-normal">
                {r.comment || 'Khách hàng không để lại nhận xét bằng lời.'}
              </p>

              {/* Shop Reply Box */}
              {r.shopReply && (
                <div className="mt-3.5 p-3 rounded-xl bg-gray-50 border border-gray-100 text-xs text-gray-600">
                  <div className="flex items-center gap-1.5 font-bold text-chopee-orange mb-1 text-[11px]">
                    <MessageSquareQuote className="w-3.5 h-3.5" />
                    <span>Phản Hồi Của Người Bán:</span>
                  </div>
                  <p className="text-gray-600 text-[11px] leading-relaxed">{r.shopReply}</p>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-6 border-t border-gray-100 mt-6">
          <button
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="p-1.5 rounded-lg border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-40"
          >
            <ChevronLeft className="w-4 h-4 text-gray-600" />
          </button>
          <span className="text-xs text-gray-600">
            {page + 1} / {totalPages}
          </span>
          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            className="p-1.5 rounded-lg border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-40"
          >
            <ChevronRight className="w-4 h-4 text-gray-600" />
          </button>
        </div>
      )}
    </div>
  );
};
export default ReviewList;

