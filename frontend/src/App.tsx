import { useEffect, useState } from 'react'
import { ShoppingBag, CheckCircle, Store, Shield, Sparkles, Layers } from 'lucide-react'

export default function App() {
  const [healthStatus, setHealthStatus] = useState<string>('Checking backend...')
  const [loading, setLoading] = useState<boolean>(true)

  useEffect(() => {
    fetch('/api/v1/health')
      .then(res => res.json())
      .then(data => {
        if (data.success) {
          setHealthStatus(data.message)
        } else {
          setHealthStatus('Backend reachable, but returned status issue')
        }
      })
      .catch(() => {
        setHealthStatus('Backend offline or starting up on :8080')
      })
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-between">
      {/* Shopee Style Top Header */}
      <header className="bg-chopee-orange text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center text-chopee-orange shadow-sm font-bold text-xl">
              <ShoppingBag className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-2xl font-black tracking-tight">Chopee</h1>
              <p className="text-xs text-orange-100">Sàn Chợ Đa Ngành Hàng & Thực Phẩm Tươi Sống</p>
            </div>
          </div>
          <div className="flex items-center space-x-4 text-sm font-medium">
            <span className="hover:text-orange-200 cursor-pointer flex items-center gap-1">
              <Store className="w-4 h-4" /> Kênh Người Bán
            </span>
            <span className="text-orange-300">|</span>
            <span className="hover:text-orange-200 cursor-pointer flex items-center gap-1">
              <Shield className="w-4 h-4" /> Quản Trị Sàn
            </span>
          </div>
        </div>
      </header>

      {/* Main Showcase Hero */}
      <main className="max-w-7xl mx-auto px-4 py-12 flex-1 w-full">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 md:p-12 text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-orange-50 text-chopee-orange text-sm font-semibold mb-6">
            <Sparkles className="w-4 h-4" /> Kiến Trúc Modular Monolith Đã Sẵn Sàng
          </div>

          <h2 className="text-3xl md:text-4xl font-extrabold text-gray-900 mb-4 tracking-tight">
            Chào mừng bạn đến với Chopee Marketplace
          </h2>
          <p className="text-gray-600 mb-8 leading-relaxed">
            Hệ thống sàn thương mại điện tử đa người bán tích hợp Chợ thực phẩm tươi sống, Nước giải khát, Thiết bị gia dụng, Phụ kiện công nghệ và Trợ lý AI mua sắm thông minh.
          </p>

          {/* Backend Status Badge */}
          <div className="inline-flex items-center gap-3 p-4 rounded-xl bg-gray-50 border border-gray-200 text-sm">
            <span className="font-semibold text-gray-700">Trạng thái Backend:</span>
            <div className="flex items-center gap-1.5 font-medium text-emerald-600">
              <CheckCircle className="w-4 h-4 text-emerald-500" />
              <span>{loading ? 'Đang kết nối...' : healthStatus}</span>
            </div>
          </div>

          {/* Core Features Grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12 text-left">
            <div className="p-5 rounded-xl border border-gray-100 bg-orange-50/50">
              <div className="w-8 h-8 rounded-lg bg-orange-100 text-chopee-orange flex items-center justify-center mb-3">
                <Store className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 mb-1">Multi-Vendor Cart</h3>
              <p className="text-xs text-gray-600">Gom sản phẩm từ nhiều shop, tự động phân tách đơn hàng độc lập.</p>
            </div>

            <div className="p-5 rounded-xl border border-gray-100 bg-orange-50/50">
              <div className="w-8 h-8 rounded-lg bg-orange-100 text-chopee-orange flex items-center justify-center mb-3">
                <Layers className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 mb-1">Chợ Thực Phẩm Tươi Sống</h3>
              <p className="text-xs text-gray-600">Hỗ trợ bán theo kg, bảo quản lạnh, giao hỏa tốc 2h.</p>
            </div>

            <div className="p-5 rounded-xl border border-gray-100 bg-orange-50/50">
              <div className="w-8 h-8 rounded-lg bg-orange-100 text-chopee-orange flex items-center justify-center mb-3">
                <Sparkles className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 mb-1">AI Shopping Copilot</h3>
              <p className="text-xs text-gray-600">Lên thực đơn, tư vấn đồ công nghệ & hiển thị thẻ sản phẩm mua tức thì.</p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 py-6 text-center text-xs text-gray-500">
        <p>© 2026 Chopee Platform. All rights reserved. Spring Boot 3 + React + MySQL 8.</p>
      </footer>
    </div>
  )
}
