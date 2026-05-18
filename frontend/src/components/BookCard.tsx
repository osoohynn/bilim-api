import Link from "next/link";

interface BookCardProps {
  id: number;
  userBookId?: number;
  title: string;
  author: string;
  price?: number;
  isOwned?: boolean;
  onRequestRental?: (userBookId: number) => void;
}

export default function BookCard({ id, userBookId, title, author, price, isOwned, onRequestRental }: BookCardProps) {
  return (
    <div className="bg-white rounded-xl shadow p-4 flex flex-col gap-2">
      <div className="bg-blue-100 rounded-lg h-32 flex items-center justify-center text-blue-400 text-4xl">
        📖
      </div>
      <p className="font-semibold text-sm leading-tight">{title}</p>
      <p className="text-xs text-gray-500">{author}</p>
      {price !== undefined && price > 0 && (
        <p className="text-xs text-gray-400">{price.toLocaleString()}원</p>
      )}
      <div className="mt-auto flex gap-2">
        {isOwned && (
          <Link
            href={`/read/${id}`}
            className="flex-1 text-center text-xs bg-blue-600 text-white py-1.5 rounded-lg hover:bg-blue-700 transition"
          >
            읽기
          </Link>
        )}
        {!isOwned && userBookId && onRequestRental && (
          <button
            onClick={() => onRequestRental(userBookId)}
            className="flex-1 text-xs bg-green-600 text-white py-1.5 rounded-lg hover:bg-green-700 transition"
          >
            대여 요청
          </button>
        )}
      </div>
    </div>
  );
}
