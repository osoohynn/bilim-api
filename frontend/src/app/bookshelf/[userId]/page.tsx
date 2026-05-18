"use client";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import api from "@/lib/api";
import BookCard from "@/components/BookCard";
import RentalRequestModal from "@/components/RentalRequestModal";
import Link from "next/link";

interface BookItem {
  id: number;
  userBookId?: number;
  title: string;
  author: string;
  price: number;
}

export default function FriendBookshelfPage() {
  const { userId } = useParams<{ userId: string }>();
  const [books, setBooks] = useState<BookItem[]>([]);
  const [modal, setModal] = useState<{ userBookId: number } | null>(null);

  useEffect(() => {
    api.get(`/api/bookshelf/${userId}`)
      .then(({ data }) => setBooks(data))
      .catch(() => {});
  }, [userId]);

  return (
    <div className="max-w-2xl mx-auto px-4 py-6">
      <div className="flex items-center gap-3 mb-6">
        <Link href="/bookshelf" className="text-gray-400 hover:text-gray-600">← 내 책장</Link>
        <h1 className="text-xl font-bold">친구 책장</h1>
      </div>

      {books.length === 0 ? (
        <p className="text-center text-gray-400 py-16">공개된 책이 없습니다.</p>
      ) : (
        <div className="grid grid-cols-2 gap-4">
          {books.map((book) => (
            <BookCard
              key={book.id}
              id={book.id}
              userBookId={book.userBookId}
              title={book.title}
              author={book.author}
              price={book.price}
              onRequestRental={(userBookId) => setModal({ userBookId })}
            />
          ))}
        </div>
      )}

      {modal && (
        <RentalRequestModal
          userBookId={modal.userBookId}
          onClose={() => setModal(null)}
          onSuccess={() => setModal(null)}
        />
      )}
    </div>
  );
}
