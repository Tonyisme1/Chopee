import React from 'react';
import { ShieldCheck, Truck, Headphones, RotateCcw } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-white border-t border-gray-200 mt-16 text-gray-600 text-xs">
      {/* Service Guarantees */}
      <div className="border-b border-gray-100 py-6 bg-gray-50/50">
        <div className="max-w-7xl mx-auto px-4 grid grid-cols-2 md:grid-cols-4 gap-6">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-orange-100 text-chopee-orange flex items-center justify-center flex-shrink-0">
              <Truck className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-gray-800 text-sm">Giao Hỏa Tốc 2H</p>
              <p className="text-gray-500 text-xs">Thực phẩm tươi sống & nước uống</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-orange-100 text-chopee-orange flex items-center justify-center flex-shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-gray-800 text-sm">100% Chính Hãng</p>
              <p className="text-gray-500 text-xs">Nông sản VietGAP & đồ công nghệ</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-orange-100 text-chopee-orange flex items-center justify-center flex-shrink-0">
              <RotateCcw className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-gray-800 text-sm">Đổi Trả Dễ Dàng</p>
              <p className="text-gray-500 text-xs">Miễn phí đổi trả trong 7 ngày</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-orange-100 text-chopee-orange flex items-center justify-center flex-shrink-0">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-gray-800 text-sm">Hỗ Trợ 24/7</p>
              <p className="text-gray-500 text-xs">AI Copilot & Tổng đài viên</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Links */}
      <div className="max-w-7xl mx-auto px-4 py-10 grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <h4 className="font-bold text-gray-900 text-sm mb-3 uppercase tracking-wider">CHĂM SÓC KHÁCH HÀNG</h4>
          <ul className="space-y-2">
            <li><a href="#" className="hover:text-chopee-orange">Trung Tâm Trợ Giúp</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Hướng Dẫn Mua Hàng</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Hướng Dẫn Bán Hàng</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Thanh Toán & Vận Chuyển</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Trả Hàng & Hoàn Tiền</a></li>
          </ul>
        </div>

        <div>
          <h4 className="font-bold text-gray-900 text-sm mb-3 uppercase tracking-wider">VỀ CHOPEE VIỆT NAM</h4>
          <ul className="space-y-2">
            <li><a href="#" className="hover:text-chopee-orange">Giới Thiệu Về Chopee</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Tuyển Dụng Nhân Tài</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Điều Khoản Sàn Thương Mại</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Chính Sách Bảo Mật</a></li>
            <li><a href="#" className="hover:text-chopee-orange">Kênh Người Bán Chính Hãng</a></li>
          </ul>
        </div>

        <div>
          <h4 className="font-bold text-gray-900 text-sm mb-3 uppercase tracking-wider">THANH TOÁN & ĐỐI TÁC</h4>
          <div className="flex flex-wrap gap-2 text-xs">
            <span className="px-2.5 py-1 bg-gray-100 rounded text-gray-700 font-semibold">COD</span>
            <span className="px-2.5 py-1 bg-blue-50 text-blue-700 rounded font-semibold border border-blue-200">VNPay QR</span>
            <span className="px-2.5 py-1 bg-gray-100 rounded text-gray-700 font-semibold">Visa / Mastercard</span>
          </div>
          <h4 className="font-bold text-gray-900 text-sm mt-5 mb-2 uppercase tracking-wider">ĐƠN VỊ VẬN CHUYỂN</h4>
          <div className="flex flex-wrap gap-2 text-xs">
            <span className="px-2.5 py-1 bg-emerald-50 text-emerald-700 rounded font-semibold border border-emerald-200">Chopee Express Fresh</span>
            <span className="px-2.5 py-1 bg-gray-100 rounded text-gray-700 font-semibold">Giao Hàng Tiêu Chuẩn</span>
          </div>
        </div>

        <div>
          <h4 className="font-bold text-gray-900 text-sm mb-3 uppercase tracking-wider">THEO DÕI CHOPEE</h4>
          <p className="text-gray-500 mb-3">Tải ứng dụng Chopee hoặc trải nghiệm Trợ lý Mua Sắm AI Copilot trực tiếp trên nền tảng web.</p>
          <div className="p-3 bg-orange-50 border border-orange-100 rounded-xl">
            <p className="text-chopee-orange font-bold text-xs">🤖 Trợ Lý Mua Sắm AI</p>
            <p className="text-gray-600 text-[11px] mt-0.5">Nhấp vào biểu tượng robot ở góc phải để nhận tư vấn nguyên liệu món ăn và đồ công nghệ!</p>
          </div>
        </div>
      </div>

      {/* Copyright Notice */}
      <div className="bg-gray-100 py-4 text-center text-gray-500 text-[11px] border-t border-gray-200">
        <p>© 2026 Chopee Marketplace Platform. Bản quyền thuộc về Đội ngũ Kỹ sư Chopee.</p>
        <p className="mt-1">Kiến trúc: Spring Boot 3 + React TypeScript + MySQL 8 InnoDB (Hot/Warm/Cold Storage Tiering).</p>
      </div>
    </footer>
  );
};

