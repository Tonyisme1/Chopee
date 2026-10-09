import React, { useEffect, useState, useCallback } from 'react';
import {
  Star,
  MessageSquare,
  RefreshCw,
  AlertCircle,
  CornerDownRight,
  Send,
  User,
} from 'lucide-react';
import { reviewApi } from '../../services/api';
import { Review } from '../../types';

export const SellerReviewsPage: React.FC = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Replying state
  const [activeReplyId, setActiveReplyId] = useState<number | null>(null);
  const [replyText, setReplyText] = useState('');
  const [submittingReply, setSubmittingReply] = useState(false);
  const [replyError, setReplyError] = useState<string | null>(null);

  const fetchReviews = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await reviewApi.getSellerReviews({ page: 0, size: 50 });
      if (res.success && res.data) {
        setReviews(res.data.content || []);
      }
    } catch (err: any) {
      console.error('Lỗi tải đánh giá seller:', err);
      setError(err.response?.data?.message || 'Không thể tải danh sách đánh giá');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  const handleSendReply = async (reviewId: number) => {
    if (!replyText.trim()) return;
    setSubmittingReply(true);
    setReplyError(null);

    try {
      const res = await reviewApi.replyReview(reviewId, { reply: replyText.trim() });
      if (res.success) {
        setActiveReplyId(null);
        setReplyText('');
        await fetchReviews();
      }
    } catch (err: any) {
      console.error('Lỗi gửi phản hồi đánh giá:', err);
      setReplyError(err.response?.data?.message || 'Không thể gửi phản hồi');
    } finally {
      setSubmittingReply(false);
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">
            Đánh Giá Của Khách Hàng
          </h1>
          <p className="text-xs text-gray-500 mt-1">
            Lắng nghe ý kiến người mua và gửi phản hồi chăm sóc khách hàng để nâng cao uy tín gian hàng.
          </p>
        </div>
        <button
          onClick={fetchReviews}
          disabled={loading}
          className="inline-flex items-center gap-2 px-3.5 py-2 border border-gray-200 bg-white hover:bg-gray-50 rounded-xl text-xs font-bold text-gray-700 shadow-sm transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Làm mới</span>
        </button>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Review List */}
      {loading ? (
        <div className="min-h-[300px] flex items-center justify-center text-gray-400">
          <RefreshCw className="w-6 h-6 animate-spin text-chopee-orange" />
        </div>
      ) : reviews.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center shadow-sm">
          <MessageSquare className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-sm font-bold text-gray-700">Chưa có đánh giá nào</p>
          <p className="text-xs text-gray-400 mt-1">
            Đánh giá từ người mua sau khi nhận hàng thành công sẽ hiển thị ở đây.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {reviews.map((rev) => (
            <div
              key={rev.id}
              className="bg-white rounded-2xl border border-gray-100 p-5 shadow-sm space-y-4"
            >
              {/* Review Header */}
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-gray-100 pb-3">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-orange-100 text-chopee-orange flex items-center justify-center font-bold text-xs">
                    <User className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-xs font-bold text-gray-900">
                      {rev.userFullName || 'Người mua ẩn danh'}
                    </span>
                    <p className="text-[10px] text-gray-400">{formatDate(rev.createdAt)}</p>
                  </div>
                </div>

                {/* Star rating */}
                <div className="flex items-center gap-1 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200">
                  <div className="flex text-amber-400">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star
                        key={star}
                        className={`w-3.5 h-3.5 ${
                          star <= rev.rating ? 'fill-current' : 'text-gray-200'
                        }`}
                      />
                    ))}
                  </div>
                  <span className="text-xs font-bold text-amber-800 ml-1">
                    {rev.rating}.0
                  </span>
                </div>
              </div>

              {/* Review Content */}
              <div className="space-y-2">
                {rev.productName && (
                  <p className="text-[11px] font-semibold text-gray-500">
                    Sản phẩm: <span className="text-gray-900">{rev.productName}</span>
                    {rev.variantName && <span> ({rev.variantName})</span>}
                  </p>
                )}
                <p className="text-xs text-gray-800 leading-relaxed">
                  {rev.comment || 'Khách hàng không để lại nhận xét văn bản.'}
                </p>

                {/* Images if any */}
                {rev.imagesJson && (
                  <div className="flex flex-wrap gap-2 pt-1">
                    {(() => {
                      try {
                        const imgs = JSON.parse(rev.imagesJson);
                        if (Array.isArray(imgs)) {
                          return imgs.map((imgUrl, i) => (
                            <img
                              key={i}
                              src={imgUrl}
                              alt="Review"
                              className="w-16 h-16 object-cover rounded-lg border border-gray-200"
                            />
                          ));
                        }
                      } catch (e) {
                        return null;
                      }
                      return null;
                    })()}
                  </div>
                )}
              </div>

              {/* Seller Reply Box */}
              {rev.shopReply ? (
                <div className="bg-gray-50/80 rounded-xl p-3.5 border border-gray-200 space-y-1.5 text-xs">
                  <div className="flex items-center justify-between text-gray-700">
                    <span className="font-bold text-chopee-orange flex items-center gap-1.5">
                      <CornerDownRight className="w-3.5 h-3.5" /> Phản hồi từ Người Bán
                    </span>
                  </div>
                  <p className="text-gray-700 text-xs pl-5">{rev.shopReply}</p>
                </div>
              ) : (
                <div className="pt-2">
                  {activeReplyId === rev.id ? (
                    <div className="space-y-2">
                      {replyError && (
                        <div className="text-[11px] text-red-600 font-semibold">{replyError}</div>
                      )}
                      <textarea
                        rows={3}
                        placeholder="Nhập câu trả lời lịch sự, cảm ơn khách hàng hoặc hỗ trợ đổi trả nếu có vấn đề..."
                        value={replyText}
                        onChange={(e) => setReplyText(e.target.value)}
                        className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-chopee-orange"
                      />
                      <div className="flex items-center justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => {
                            setActiveReplyId(null);
                            setReplyText('');
                          }}
                          className="px-3 py-1.5 border border-gray-200 rounded-lg text-xs font-bold text-gray-600 hover:bg-gray-50"
                        >
                          Hủy
                        </button>
                        <button
                          type="button"
                          disabled={submittingReply || !replyText.trim()}
                          onClick={() => handleSendReply(rev.id)}
                          className="px-4 py-1.5 bg-chopee-orange hover:bg-orange-600 disabled:opacity-50 text-white rounded-lg text-xs font-bold shadow-sm transition-colors flex items-center gap-1"
                        >
                          {submittingReply ? (
                            <RefreshCw className="w-3 h-3 animate-spin" />
                          ) : (
                            <Send className="w-3 h-3" />
                          )}
                          <span>Gửi phản hồi</span>
                        </button>
                      </div>
                    </div>
                  ) : (
                    <button
                      onClick={() => {
                        setActiveReplyId(rev.id);
                        setReplyText('');
                      }}
                      className="inline-flex items-center gap-1.5 text-xs font-bold text-chopee-orange hover:text-orange-600 transition-colors"
                    >
                      <CornerDownRight className="w-3.5 h-3.5" />
                      <span>Trả lời đánh giá này</span>
                    </button>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
