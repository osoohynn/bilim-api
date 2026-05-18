"use client";
import { useEffect, useRef, useState } from "react";
import ePub from "epubjs";

interface Props {
  url: string;
}

export default function EpubReader({ url }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const renditionRef = useRef<any>(null);
  const [error, setError] = useState("");
  const [location, setLocation] = useState("");

  useEffect(() => {
    if (!containerRef.current) return;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let book: any;
    const container = containerRef.current;
    let blobUrl: string | null = null;
    let cancelled = false;

    const onResize = () => {
      renditionRef.current?.resize(container.clientWidth, container.clientHeight);
    };
    window.addEventListener("resize", onResize);

    const waitForSize = () =>
      new Promise<void>((resolve) => {
        if (container.clientWidth > 0 && container.clientHeight > 0) { resolve(); return; }
        const ro = new ResizeObserver(() => {
          if (container.clientWidth > 0 && container.clientHeight > 0) { ro.disconnect(); resolve(); }
        });
        ro.observe(container);
      });

    (async () => {
      try {
        const res = await fetch(`/epub-proxy?url=${encodeURIComponent(url)}`);
        if (!res.ok) throw new Error();
        const blob = await res.blob();
        if (cancelled) return;
        blobUrl = URL.createObjectURL(blob);
        book = ePub(blobUrl, { openAs: "epub", replacements: "blobUrl" });
        await Promise.all([book.ready, waitForSize()]);
        if (cancelled) { book.destroy(); return; }
        const rendition = book.renderTo(container, {
          width: container.clientWidth,
          height: container.clientHeight,
          allowScriptedContent: true,
          spread: "none",
        });
        rendition.on("relocated", (loc: any) => {
          const pct = Math.round((loc.start.percentage ?? 0) * 100);
          setLocation(`${pct}% · ${loc.start.displayed.page} / ${loc.start.displayed.total}`);
        });
        await rendition.display();
        if (cancelled) { book.destroy(); return; }
        renditionRef.current = rendition;
      } catch (e) {
        if (!cancelled) {
          console.error("epub load error", e);
          setError("epub 파일을 열 수 없습니다.");
        }
      }
    })();

    return () => {
      cancelled = true;
      window.removeEventListener("resize", onResize);
      renditionRef.current = null;
      book?.destroy();
      if (blobUrl) URL.revokeObjectURL(blobUrl);
    };
  }, [url]);

  const prev = () => { console.log("prev"); renditionRef.current?.prev(); };
  const next = () => { console.log("next"); renditionRef.current?.next(); };

  if (error) {
    return (
      <div className="flex items-center justify-center h-full text-red-500">{error}</div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <div ref={containerRef} className="flex-1 overflow-hidden" />
      <div className="flex justify-between items-center px-6 py-3 bg-white border-t">
        <button
          onClick={prev}
          className="px-8 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 transition text-sm font-medium"
        >
          ← 이전
        </button>
        <span className="text-xs text-gray-400">{location}</span>
        <button
          onClick={next}
          className="px-8 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 transition text-sm font-medium"
        >
          다음 →
        </button>
      </div>
    </div>
  );
}
