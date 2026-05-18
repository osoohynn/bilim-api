"use client";
import { usePathname } from "next/navigation";
import Navbar from "./Navbar";

const AUTH_PATHS = ["/login", "/signup"];

export default function NavbarWrapper() {
  const pathname = usePathname();
  if (AUTH_PATHS.includes(pathname)) return null;
  return <Navbar />;
}
