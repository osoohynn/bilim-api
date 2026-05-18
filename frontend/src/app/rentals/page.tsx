"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

interface Rental {
  id: number;
  userBookId: number;
  borrowerId: number;
  lenderId: number;
  status: "PENDING" | "ACTIVE" | "RETURNED" | "REJECTED" | "EXPIRED";
  startedAt?: string;
  dueDate?: string;
}

const STATUS_LABEL: Record<Rental["status"], string> = {
  PENDING: "대기중",
  ACTIVE: "대여중",
  RETURNED: "반납됨",
  REJECTED: "거절됨",
  EXPIRED: "만료됨",
};

const STATUS_COLOR: Record<Rental["status"], string> = {
  PENDING: "bg-yellow-100 text-yellow-700",
  ACTIVE: "bg-green-100 text-green-700",
  RETURNED: "bg-gray-100 text-gray-600",
  REJECTED: "bg-red-100 text-red-600",
  EXPIRED: "bg-red-100 text-red-600",
};

export default function RentalsPage() {
  const [rentals, setRentals] = useState<Rental[]>([]);

  const fetchRentals = () => {
    api.get("/api/rentals").then(({ data }) => setRentals(data)).catch(() => {});
  };

  useEffect(() => { fetchRentals(); }, []);

  const accept = async (id: number) => {
    await api.post(`/api/rentals/${id}/accept`, { days: 7 });
    fetchRentals();
  };

  const reject = async (id: number) => {
    await api.post(`/api/rentals/${id}/reject`);
    fetchRentals();
  };

  const returnBook = async (id: number) => {
    await api.post(`/api/rentals/${id}/return`);
    fetchRentals();
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-6">
      <h1 className="text-xl font-bold mb-4">대여 관리</h1>

      {rentals.length === 0 ? (
        <p className="text-center text-gray-400 py-16">대여 내역이 없습니다.</p>
      ) : (
        <div className="space-y-3">
          {rentals.map((rental) => (
            <div key={rental.id} className="bg-white rounded-xl shadow p-4">
              <div className="flex justify-between items-start mb-2">
                <span className="text-sm font-medium">대여 #{rental.id}</span>
                <span className={`text-xs px-2 py-0.5 rounded-full ${STATUS_COLOR[rental.status]}`}>
                  {STATUS_LABEL[rental.status]}
                </span>
              </div>
              {rental.dueDate && (
                <p className="text-xs text-gray-500 mb-3">반납 기한: {rental.dueDate}</p>
              )}
              <div className="flex gap-2">
                {rental.status === "PENDING" && (
                  <>
                    <button
                      onClick={() => accept(rental.id)}
                      className="flex-1 py-1.5 bg-blue-600 text-white text-xs rounded-lg hover:bg-blue-700"
                    >
                      수락
                    </button>
                    <button
                      onClick={() => reject(rental.id)}
                      className="flex-1 py-1.5 border text-xs rounded-lg hover:bg-gray-50"
                    >
                      거절
                    </button>
                  </>
                )}
                {rental.status === "ACTIVE" && (
                  <button
                    onClick={() => returnBook(rental.id)}
                    className="flex-1 py-1.5 bg-gray-600 text-white text-xs rounded-lg hover:bg-gray-700"
                  >
                    반납
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
