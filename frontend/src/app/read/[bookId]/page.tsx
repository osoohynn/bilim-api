"use client";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import dynamic from "next/dynamic";
import api from "@/lib/api";
import Link from "next/link";

const EpubReader = dynamic(() => import("@/components/EpubReader"), { ssr: false });

export default function ReadPage() {
  const { bookId } = useParams<{ bookId: string }>();
  const router = useRouter();
  const [url, setUrl] = useState<string | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get(`/api/books/${bookId}/read`)
      .then(({ data }) => setUrl(data))
      .catch((err) => {
        if (err.response?.status === 403) {
          setError("이 책에 접근할 권한이 없습니다.");
        } else {
          router.replace("/bookshelf");
        }
      });
  }, [bookId, router]);

  if (error) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4">
        <p className="text-red-500">{error}</p>
        <Link href="/bookshelf" className="text-blue-600 hover:underline">책장으로 돌아가기</Link>
      </div>
    );
  }

  if (!url) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-400">불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col">
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b">
        <Link href="/bookshelf" className="text-gray-400 hover:text-gray-600 text-sm">← 책장</Link>
      </div>
      <div className="flex-1 overflow-hidden">
        <EpubReader url={url} />
      </div>
    </div>
  );
}
