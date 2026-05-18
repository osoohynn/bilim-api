"use client";
import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import Link from "next/link";

interface Book {
  id: number;
  title: string;
  author: string;
  publisher?: string;
  description?: string;
  category?: string;
  price: number;
  contentUrl?: string;
}

const CATEGORY_LABELS: Record<string, string> = {
  NOVEL: "소설",
  NONFICTION: "논픽션",
  SCIENCE: "과학",
  HISTORY: "역사",
  TECHNOLOGY: "기술",
  SELF_HELP: "자기계발",
  CHILDREN: "어린이",
  ART: "예술",
  OTHER: "기타",
};

export default function BooksPage() {
  const router = useRouter();
  const [books, setBooks] = useState<Book[]>([]);
  const [ownedIds, setOwnedIds] = useState<Set<number>>(new Set());
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const [purchasing, setPurchasing] = useState<number | null>(null);
  const [message, setMessage] = useState<{ id: number; text: string; ok: boolean } | null>(null);

  const fetchBooks = useCallback(async () => {
    try {
      const params: Record<string, string> = {};
      if (keyword) params.keyword = keyword;
      if (category) params.category = category;
      const { data } = await api.get("/api/books", { params });
      setBooks(data);
    } catch {
      router.replace("/login");
    }
  }, [keyword, category, router]);

  const fetchOwned = useCallback(async () => {
    try {
      const { data } = await api.get("/api/bookshelf");
      const ids = new Set<number>([
        ...data.owned.map((b: Book) => b.id),
        ...data.renting.map((b: Book) => b.id),
      ]);
      setOwnedIds(ids);
    } catch {}
  }, []);

  useEffect(() => {
    fetchBooks();
    fetchOwned();
  }, [fetchBooks, fetchOwned]);

  const handlePurchase = async (book: Book) => {
    setPurchasing(book.id);
    try {
      await api.post(`/api/books/${book.id}/purchase`);
      setMessage({ id: book.id, text: "구매 완료! 내 책장에서 확인하세요.", ok: true });
      fetchOwned();
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      const text = status === 402 || status === 400
        ? "포인트가 부족합니다."
        : status === 409
        ? "이미 소유한 책입니다."
        : "구매에 실패했습니다.";
      setMessage({ id: book.id, text, ok: false });
    } finally {
      setPurchasing(null);
    }
  };

  const handleWishlist = async (bookId: number) => {
    try {
      await api.post(`/api/bookshelf/wishlist/${bookId}`);
      setMessage({ id: bookId, text: "찜 목록에 추가됐습니다.", ok: true });
    } catch {
      setMessage({ id: bookId, text: "이미 찜한 책입니다.", ok: false });
    }
  };

  const categories = Array.from(new Set(books.map((b) => b.category).filter(Boolean)));

  return (
    <div className="max-w-3xl mx-auto px-4 py-6">
      <h1 className="text-xl font-bold mb-4">책 구경</h1>

      <div className="flex gap-2 mb-3">
        <input
          type="text"
          placeholder="제목, 저자 검색..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="flex-1 border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          onClick={fetchBooks}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
        >
          검색
        </button>
      </div>

      <div className="flex gap-2 flex-wrap mb-5">
        <button
          onClick={() => setCategory("")}
          className={`px-3 py-1 rounded-full text-xs border transition ${!category ? "bg-blue-600 text-white border-blue-600" : "text-gray-600 hover:border-blue-400"}`}
        >
          전체
        </button>
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setCategory(cat === category ? "" : cat!)}
            className={`px-3 py-1 rounded-full text-xs border transition ${category === cat ? "bg-blue-600 text-white border-blue-600" : "text-gray-600 hover:border-blue-400"}`}
          >
            {CATEGORY_LABELS[cat!] ?? cat}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-2 gap-4">
        {books.map((book) => {
          const owned = ownedIds.has(book.id);
          const msg = message?.id === book.id ? message : null;
          return (
            <div key={book.id} className="bg-white rounded-xl shadow p-4 flex flex-col gap-2">
              <div className="bg-blue-50 rounded-lg h-28 flex items-center justify-center text-4xl">📖</div>
              <div className="flex items-start justify-between gap-1">
                <p className="font-semibold text-sm leading-tight">{book.title}</p>
                {owned && (
                  <span className="shrink-0 text-xs bg-green-100 text-green-700 px-1.5 py-0.5 rounded-full">소유중</span>
                )}
              </div>
              <p className="text-xs text-gray-500">{book.author}</p>
              {book.category && (
                <p className="text-xs text-blue-400">{CATEGORY_LABELS[book.category] ?? book.category}</p>
              )}
              <p className="text-sm font-medium">{book.price.toLocaleString()}원</p>
              {msg && (
                <p className={`text-xs ${msg.ok ? "text-green-600" : "text-red-500"}`}>{msg.text}</p>
              )}
              <div className="mt-auto flex gap-2">
                {owned ? (
                  <Link
                    href={`/read/${book.id}`}
                    className="flex-1 text-center text-xs bg-blue-600 text-white py-1.5 rounded-lg hover:bg-blue-700"
                  >
                    읽기
                  </Link>
                ) : (
                  <button
                    onClick={() => handlePurchase(book)}
                    disabled={purchasing === book.id}
                    className="flex-1 text-xs bg-blue-600 text-white py-1.5 rounded-lg hover:bg-blue-700 disabled:opacity-50"
                  >
                    {purchasing === book.id ? "구매 중..." : `구매`}
                  </button>
                )}
                <button
                  onClick={() => handleWishlist(book.id)}
                  className="px-2 py-1.5 border rounded-lg text-xs hover:bg-gray-50"
                  title="찜하기"
                >
                  ♡
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
