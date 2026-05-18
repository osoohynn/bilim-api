"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import api from "@/lib/api";
import { clearTokens, getAccessToken } from "@/lib/auth";

export default function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const [balance, setBalance] = useState<number | null>(null);

  useEffect(() => {
    if (!getAccessToken()) return;
    api.get("/api/points")
      .then(({ data }) => setBalance(data.balance))
      .catch(() => {});
  }, [pathname]);

  const handleLogout = async () => {
    try { await api.post("/api/auth/logout"); } finally {
      clearTokens();
      router.replace("/login");
    }
  };

  const navLink = (href: string, label: string) => (
    <Link
      href={href}
      className={`text-sm font-medium transition ${
        pathname.startsWith(href)
          ? "text-blue-600"
          : "text-gray-600 hover:text-gray-900"
      }`}
    >
      {label}
    </Link>
  );

  return (
    <nav className="sticky top-0 z-40 bg-white border-b">
      <div className="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <Link href="/books" className="font-bold text-blue-600 text-lg">bilim</Link>
          {navLink("/books", "책 구경")}
          {navLink("/bookshelf", "내 책장")}
          {navLink("/friends", "친구")}
          {navLink("/rentals", "대여 관리")}
        </div>
        <div className="flex items-center gap-4">
          {balance !== null && (
            <span className="text-sm text-gray-500">
              💰 {balance.toLocaleString()}원
            </span>
          )}
          <button
            onClick={handleLogout}
            className="text-sm text-gray-400 hover:text-gray-600"
          >
            로그아웃
          </button>
        </div>
      </div>
    </nav>
  );
}
