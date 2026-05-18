import type { Metadata } from "next";
import "./globals.css";
import NavbarWrapper from "@/components/NavbarWrapper";

export const metadata: Metadata = {
  title: "bilim",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className="bg-gray-50 text-gray-900 min-h-screen">
        <NavbarWrapper />
        {children}
      </body>
    </html>
  );
}
