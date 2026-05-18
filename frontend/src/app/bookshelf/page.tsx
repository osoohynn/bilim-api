"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import BookCard from "@/components/BookCard";
import RentalRequestModal from "@/components/RentalRequestModal";

interface BookItem {
  id: number;
  title: string;
  author: string;
  price: number;
}

interface BookshelfData {
  owned: BookItem[];
  renting: BookItem[];
  wishlist: BookItem[];
}

type Tab = "owned" | "renting" | "wishlist";

export default function BookshelfPage() {
  const router = useRouter();
  const [data, setData] = useState<BookshelfData>({ owned: [], renting: [], wishlist: [] });
  const [tab, setTab] = useState<Tab>("owned");
  const [modal, setModal] = useState<{ userBookId: number } | null>(null);

  const fetchBookshelf = async () => {
    try {
      const { data: res } = await api.get("/api/bookshelf");
      setData(res);
    } catch {
      router.replace("/login");
    }
  };

  useEffect(() => { fetchBookshelf(); }, []);

  const books = data[tab];
  const tabLabel: Record<Tab, string> = { owned: "소유", renting: "대여중", wishlist: "찜" };

  return (
    <div className="max-w-3xl mx-auto px-4 py-6">
      <h1 className="text-xl font-bold mb-4">내 책장</h1>

      <div className="flex gap-1 mb-4 bg-gray-100 p-1 rounded-lg">
        {(["owned", "renting", "wishlist"] as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`flex-1 py-1.5 rounded-md text-sm font-medium transition ${
              tab === t ? "bg-white shadow text-blue-600" : "text-gray-500 hover:text-gray-700"
            }`}
          >
            {tabLabel[t]} ({data[t].length})
          </button>
        ))}
      </div>

      {books.length === 0 ? (
        <p className="text-center text-gray-400 py-16">
          {tab === "owned" ? "구매한 책이 없습니다." : tab === "renting" ? "대여 중인 책이 없습니다." : "찜한 책이 없습니다."}
        </p>
      ) : (
        <div className="grid grid-cols-2 gap-4">
          {books.map((book) => (
            <BookCard
              key={book.id}
              id={book.id}
              title={book.title}
              author={book.author}
              price={book.price}
              isOwned
            />
          ))}
        </div>
      )}

      {modal && (
        <RentalRequestModal
          userBookId={modal.userBookId}
          onClose={() => setModal(null)}
          onSuccess={() => { setModal(null); fetchBookshelf(); }}
        />
      )}
    </div>
  );
}
