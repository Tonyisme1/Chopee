import React, { useState, useRef, useEffect } from 'react';
import {
  Bot,
  Send,
  Sparkles,
  Plus,
  RefreshCw,
  Store,
  ChevronDown,
} from 'lucide-react';
import { aiApi } from '../services/api';
import { ProductSummary } from '../types';
import { useCartStore } from '../stores/useCartStore';
import { useAuthStore } from '../stores/useAuthStore';

interface ChatMessage {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  products?: ProductSummary[];
  suggestedQuestions?: string[];
}

export const AIChatWidget: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [addedProductId, setAddedProductId] = useState<number | null>(null);

  const { addToCart } = useCartStore();
  const { isAuthenticated } = useAuthStore();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const initialMessage: ChatMessage = {
    id: 'msg-welcome',
    sender: 'bot',
    text: 'Xin chào! Tôi là Trợ lý Mua Sắm AI của Chopee 🤖. Tôi có thể tư vấn nguyên liệu tươi ngon theo công thức nấu ăn, gợi ý mâm cơm gia đình, hoặc tư vấn thông số kỹ thuật công nghệ chính hãng. Bạn cần tìm gì hôm nay?',
    suggestedQuestions: [
      'Gợi ý nguyên liệu nấu canh chua 4 người',
      'Tìm sạc nhanh 65W cho laptop',
      'Lên thực đơn 150k mâm cơm gia đình',
      'Thùng bia & nước ngọt tiệc 8 người',
    ],
  };

  const [messages, setMessages] = useState<ChatMessage[]>([initialMessage]);

  useEffect(() => {
    if (isOpen) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isOpen]);

  const handleSendMessage = async (textToSend: string) => {
    const trimmed = textToSend.trim();
    if (!trimmed || loading) return;

    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: trimmed,
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const res = await aiApi.chat({
        message: trimmed,
      });

      if (res.success && res.data) {
        const botMsg: ChatMessage = {
          id: `bot-${Date.now()}`,
          sender: 'bot',
          text: res.data.reply,
          products: res.data.recommendedProducts,
          suggestedQuestions: res.data.suggestedQuestions,
        };
        setMessages((prev) => [...prev, botMsg]);
      }
    } catch (err: any) {
      const errorMsg: ChatMessage = {
        id: `bot-err-${Date.now()}`,
        sender: 'bot',
        text: 'Xin lỗi, tôi đang gặp sự cố khi kết nối tới hệ thống AI. Vui lòng thử lại sau ít phút!',
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickAdd = async (product: ProductSummary) => {
    if (!isAuthenticated) {
      alert('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!');
      return;
    }
    try {
      await addToCart(product.id, product.minOrderQuantity || 1);
      setAddedProductId(product.id);
      setTimeout(() => setAddedProductId(null), 2000);
    } catch (err: any) {
      alert(err.message || 'Không thể thêm sản phẩm');
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

  return (
    <div className="fixed bottom-6 right-6 z-50">
      {/* Floating Launcher Button */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="group relative flex items-center gap-2.5 px-4 py-3 bg-gradient-to-r from-orange-500 to-amber-500 text-white rounded-full shadow-2xl hover:shadow-orange-500/40 hover:scale-105 active:scale-95 transition-all duration-200"
          title="Mở Trợ Lý Mua Sắm AI"
        >
          <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
            <Bot className="w-5 h-5 text-white animate-bounce" />
          </div>
          <div className="text-left">
            <span className="block text-xs font-black tracking-wide leading-tight">AI Copilot</span>
            <span className="block text-[10px] text-orange-100 font-medium">Tư vấn mua sắm</span>
          </div>
          <span className="absolute -top-1 -right-1 w-3.5 h-3.5 bg-yellow-400 rounded-full border-2 border-white animate-ping" />
        </button>
      )}

      {/* Expandable Chat Window */}
      {isOpen && (
        <div className="w-[360px] sm:w-[420px] h-[580px] bg-white rounded-3xl shadow-2xl border border-gray-100 flex flex-col overflow-hidden animate-in zoom-in-95 duration-200">
          {/* Header */}
          <div className="p-4 bg-gradient-to-r from-orange-500 to-amber-500 text-white flex items-center justify-between shadow-md">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center font-bold">
                <Bot className="w-5 h-5 text-white" />
              </div>
              <div>
                <h3 className="font-bold text-sm tracking-tight flex items-center gap-1.5">
                  <span>Chopee AI Shopping Copilot</span>
                  <Sparkles className="w-3.5 h-3.5 text-yellow-300" />
                </h3>
                <span className="text-[10px] text-orange-100 flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                  Trực tuyến 24/7 (Gemini Flash)
                </span>
              </div>
            </div>

            <button
              onClick={() => setIsOpen(false)}
              className="p-1.5 rounded-lg text-white/80 hover:text-white hover:bg-white/10 transition-colors"
            >
              <ChevronDown className="w-5 h-5" />
            </button>
          </div>

          {/* Messages Body */}
          <div className="flex-1 p-4 overflow-y-auto space-y-4 bg-gray-50/50">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex flex-col ${
                  msg.sender === 'user' ? 'items-end' : 'items-start'
                }`}
              >
                {/* Bubble */}
                <div
                  className={`max-w-[85%] p-3.5 rounded-2xl text-xs leading-relaxed ${
                    msg.sender === 'user'
                      ? 'bg-chopee-orange text-white rounded-tr-none shadow-sm font-medium'
                      : 'bg-white text-gray-800 rounded-tl-none border border-gray-100 shadow-sm'
                  }`}
                >
                  <p className="whitespace-pre-wrap">{msg.text}</p>
                </div>

                {/* Recommended Product Cards inside Bot Message */}
                {msg.products && msg.products.length > 0 && (
                  <div className="w-full mt-2.5 space-y-2">
                    <p className="text-[11px] font-bold text-gray-500 uppercase tracking-wider pl-1">
                      Sản phẩm gợi ý mua ngay:
                    </p>
                    <div className="space-y-2">
                      {msg.products.map((prod) => (
                        <div
                          key={prod.id}
                          className="p-2.5 bg-white rounded-xl border border-gray-100 shadow-xs flex items-center justify-between gap-3 hover:border-orange-200 transition-colors"
                        >
                          <div className="flex items-center gap-2.5 min-w-0">
                            <img
                              src={prod.thumbnailUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e'}
                              alt={prod.name}
                              className="w-11 h-11 object-cover rounded-lg border border-gray-100 flex-shrink-0"
                            />
                            <div className="min-w-0 space-y-0.5">
                              <p className="text-xs font-bold text-gray-800 truncate" title={prod.name}>
                                {prod.name}
                              </p>
                              <div className="flex items-center gap-1.5 text-[10px] text-gray-400">
                                <Store className="w-3 h-3 text-chopee-orange flex-shrink-0" />
                                <span className="truncate">{prod.shopName}</span>
                              </div>
                              <span className="text-xs font-extrabold text-chopee-orange block">
                                {formatCurrency(prod.sellingPrice)}
                              </span>
                            </div>
                          </div>

                          <button
                            type="button"
                            onClick={() => handleQuickAdd(prod)}
                            className={`p-2 rounded-lg text-xs font-bold flex items-center gap-1 flex-shrink-0 transition-colors ${
                              addedProductId === prod.id
                                ? 'bg-emerald-600 text-white'
                                : 'bg-orange-50 text-chopee-orange hover:bg-chopee-orange hover:text-white'
                            }`}
                            title="Thêm vào giỏ"
                          >
                            <Plus className="w-3.5 h-3.5" />
                            <span>{addedProductId === prod.id ? 'Đã thêm' : 'Chọn mua'}</span>
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Suggested Questions Chips */}
                {msg.suggestedQuestions && msg.suggestedQuestions.length > 0 && (
                  <div className="w-full mt-2.5 flex flex-wrap gap-1.5">
                    {msg.suggestedQuestions.map((q, idx) => (
                      <button
                        key={idx}
                        onClick={() => handleSendMessage(q)}
                        className="text-[11px] font-medium px-3 py-1 bg-white hover:bg-orange-50 hover:text-chopee-orange border border-gray-200 rounded-full text-gray-600 transition-all text-left"
                      >
                        💡 {q}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}

            {loading && (
              <div className="flex items-center gap-2 p-3 bg-white rounded-2xl border border-gray-100 text-xs text-gray-500 w-fit">
                <RefreshCw className="w-3.5 h-3.5 animate-spin text-chopee-orange" />
                <span>AI đang tìm sản phẩm phù hợp trong kho...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Footer */}
          <div className="p-3 bg-white border-t border-gray-100">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSendMessage(input);
              }}
              className="flex items-center gap-2"
            >
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Hỏi AI: Canh chua cá lóc, nồi chiên 5L..."
                className="flex-1 px-3.5 py-2.5 text-xs bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-1 focus:ring-orange-500"
              />
              <button
                type="submit"
                disabled={loading || !input.trim()}
                className="p-2.5 bg-chopee-orange hover:bg-orange-600 disabled:opacity-40 text-white rounded-xl shadow-md transition-colors"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default AIChatWidget;
