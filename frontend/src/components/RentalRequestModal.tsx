"use client";
import { useState } from "react";
import api from "@/lib/api";

interface Props {
  userBookId: number;
  onClose: () => void;
  onSuccess: () => void;
}

export default function RentalRequestModal({ userBookId, onClose, onSuccess }: Props) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleRequest = async () => {
    setLoading(true);
    setError("");
    try {
      await api.post(`/api/rentals/request/${userBookId}`);
      onSuccess();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? "대여 요청에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl p-6 w-80 shadow-xl">
        <h2 className="text-lg font-bold mb-2">대여 요청</h2>
        <p className="text-sm text-gray-600 mb-4">이 책의 대여를 요청하시겠습니까?</p>
        {error && <p className="text-red-500 text-sm mb-3">{error}</p>}
        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 py-2 border rounded-lg text-sm hover:bg-gray-50"
          >
            취소
          </button>
          <button
            onClick={handleRequest}
            disabled={loading}
            className="flex-1 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? "요청 중..." : "요청"}
          </button>
        </div>
      </div>
    </div>
  );
}
