import React, { useState, useEffect } from 'react';
import { X, MapPin, Plus } from 'lucide-react';
import { useAddressStore } from '../stores/useAddressStore';
import { UserAddress } from '../types';

interface AddressModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (address: UserAddress) => void;
  selectedId?: number;
}

export const AddressModal: React.FC<AddressModalProps> = ({
  isOpen,
  onClose,
  onSelect,
  selectedId,
}) => {
  const { addresses, fetchAddresses, addAddress, setDefault } = useAddressStore();
  const [isAddingNew, setIsAddingNew] = useState(false);

  // Form State
  const [receiverName, setReceiverName] = useState('');
  const [phone, setPhone] = useState('');
  const [province, setProvince] = useState('');
  const [district, setDistrict] = useState('');
  const [ward, setWard] = useState('');
  const [detailAddress, setDetailAddress] = useState('');
  const [isDefault, setIsDefault] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      fetchAddresses();
      setIsAddingNew(false);
      setFormError(null);
    }
  }, [isOpen, fetchAddresses]);

  if (!isOpen) return null;

  const handleAddNewSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!receiverName.trim() || !phone.trim() || !province.trim() || !district.trim() || !ward.trim() || !detailAddress.trim()) {
      setFormError('Vui lòng điền đầy đủ các thông tin địa chỉ');
      return;
    }

    setLoading(true);
    try {
      await addAddress({
        receiverName: receiverName.trim(),
        phone: phone.trim(),
        province: province.trim(),
        district: district.trim(),
        ward: ward.trim(),
        detailAddress: detailAddress.trim(),
        isDefault,
      });
      setIsAddingNew(false);
      // reset form
      setReceiverName('');
      setPhone('');
      setProvince('');
      setDistrict('');
      setWard('');
      setDetailAddress('');
      setIsDefault(false);
    } catch (err: any) {
      setFormError(err.message || 'Không thể thêm địa chỉ mới');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white rounded-2xl shadow-2xl border border-gray-100 max-w-lg w-full max-h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <MapPin className="w-5 h-5 text-chopee-orange" />
            <h3 className="font-bold text-gray-900 text-sm">
              {isAddingNew ? 'Thêm Địa Chỉ Nhận Hàng Mới' : 'Địa Chỉ Nhận Hàng Của Tôi'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-5 overflow-y-auto flex-1">
          {formError && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl">
              {formError}
            </div>
          )}

          {!isAddingNew ? (
            <div className="space-y-3">
              {addresses.length === 0 ? (
                <div className="text-center py-8 text-gray-400">
                  <p className="text-xs">Bạn chưa lưu địa chỉ nhận hàng nào.</p>
                </div>
              ) : (
                addresses.map((addr) => {
                  const isCurrent = addr.id === selectedId;

                  return (
                    <div
                      key={addr.id}
                      className={`p-4 rounded-xl border transition-all ${
                        isCurrent
                          ? 'border-orange-500 bg-orange-50/30 shadow-sm'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-gray-900 text-xs">
                              {addr.receiverName}
                            </span>
                            <span className="text-gray-300">|</span>
                            <span className="text-xs text-gray-600 font-medium">
                              {addr.phone}
                            </span>
                            {addr.isDefault && (
                              <span className="text-[10px] font-bold px-1.5 py-0.2 bg-orange-100 text-chopee-orange rounded">
                                Mặc định
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-gray-600">
                            {addr.detailAddress}, {addr.ward}, {addr.district}, {addr.province}
                          </p>
                        </div>

                        <div className="flex flex-col items-end gap-2">
                          <button
                            type="button"
                            onClick={() => {
                              onSelect(addr);
                              onClose();
                            }}
                            className={`px-3 py-1.5 text-xs font-bold rounded-lg transition-colors ${
                              isCurrent
                                ? 'bg-chopee-orange text-white'
                                : 'bg-gray-100 text-gray-700 hover:bg-orange-50 hover:text-chopee-orange'
                            }`}
                          >
                            {isCurrent ? 'Đang chọn' : 'Chọn'}
                          </button>
                          {!addr.isDefault && (
                            <button
                              type="button"
                              onClick={() => setDefault(addr.id)}
                              className="text-[11px] text-gray-400 hover:text-chopee-orange hover:underline"
                            >
                              Đặt làm mặc định
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}

              <button
                type="button"
                onClick={() => setIsAddingNew(true)}
                className="w-full py-3 border-2 border-dashed border-gray-200 hover:border-orange-400 text-gray-600 hover:text-chopee-orange rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition-all mt-4"
              >
                <Plus className="w-4 h-4" />
                <span>Thêm Địa Chỉ Mới</span>
              </button>
            </div>
          ) : (
            /* Add New Address Form */
            <form onSubmit={handleAddNewSubmit} className="space-y-3 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">
                    Tên người nhận <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={receiverName}
                    onChange={(e) => setReceiverName(e.target.value)}
                    placeholder="Nguyễn Văn A"
                    className="w-full p-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">
                    Số điện thoại <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="0912345678"
                    className="w-full p-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Tỉnh / Thành phố *</label>
                  <input
                    type="text"
                    value={province}
                    onChange={(e) => setProvince(e.target.value)}
                    placeholder="TP. Hồ Chí Minh"
                    className="w-full p-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Quận / Huyện *</label>
                  <input
                    type="text"
                    value={district}
                    onChange={(e) => setDistrict(e.target.value)}
                    placeholder="Quận 1"
                    className="w-full p-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Phường / Xã *</label>
                  <input
                    type="text"
                    value={ward}
                    onChange={(e) => setWard(e.target.value)}
                    placeholder="Phường Bến Nghé"
                    className="w-full p-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-gray-700 mb-1">
                  Địa chỉ cụ thể (Số nhà, tên đường, tòa nhà) <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={detailAddress}
                  onChange={(e) => setDetailAddress(e.target.value)}
                  placeholder="123 Lê Lợi, Căn hộ A12..."
                  className="w-full p-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-orange-500"
                  required
                />
              </div>

              <label className="flex items-center gap-2 pt-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={isDefault}
                  onChange={(e) => setIsDefault(e.target.checked)}
                  className="w-4 h-4 text-chopee-orange rounded focus:ring-orange-500"
                />
                <span className="text-gray-700 font-medium">Đặt làm địa chỉ nhận hàng mặc định</span>
              </label>

              <div className="flex items-center justify-end gap-2 pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsAddingNew(false)}
                  className="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg text-gray-700 font-bold"
                >
                  Trở lại
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-5 py-2 bg-chopee-orange hover:bg-orange-600 text-white font-bold rounded-lg shadow-sm disabled:opacity-60"
                >
                  {loading ? 'Đang lưu...' : 'Hoàn thành'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
export default AddressModal;
