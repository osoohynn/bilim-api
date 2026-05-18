"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { getUserId } from "@/lib/auth";

interface Friend {
  id: number;
  nickname: string;
  lastSeenAt: string;
}

interface FriendRequest {
  id: number;
  nickname: string;
  createdAt: string;
}

export default function FriendsPage() {
  const myId = getUserId();
  const [friends, setFriends] = useState<Friend[]>([]);
  const [requests, setRequests] = useState<FriendRequest[]>([]);
  const [recipientId, setRecipientId] = useState("");
  const [msg, setMsg] = useState<{ text: string; ok: boolean } | null>(null);

  const fetchAll = async () => {
    const [f, r] = await Promise.all([
      api.get("/api/friends").catch(() => ({ data: [] })),
      api.get("/api/friends/requests").catch(() => ({ data: [] })),
    ]);
    setFriends(f.data);
    setRequests(r.data);
  };

  useEffect(() => { fetchAll(); }, []);

  const sendRequest = async () => {
    const id = Number(recipientId.trim());
    if (!id) return;
    try {
      await api.post(`/api/friends/request/${id}`);
      setMsg({ text: "친구 요청을 보냈습니다.", ok: true });
      setRecipientId("");
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      const text = status === 409 ? "이미 친구이거나 요청 중입니다."
        : status === 400 ? "자기 자신에게는 요청할 수 없습니다."
        : "요청에 실패했습니다.";
      setMsg({ text, ok: false });
    }
  };

  const accept = async (requesterId: number) => {
    await api.post(`/api/friends/accept/${requesterId}`);
    fetchAll();
  };

  const reject = async (requesterId: number) => {
    await api.post(`/api/friends/reject/${requesterId}`);
    fetchAll();
  };

  const remove = async (friendId: number) => {
    await api.delete(`/api/friends/${friendId}`);
    fetchAll();
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-6 space-y-8">
      <div>
        <h1 className="text-xl font-bold mb-1">친구</h1>
        {myId && (
          <p className="text-sm text-gray-400">
            내 ID: <span className="font-mono font-semibold text-gray-700">{myId}</span>
            <span className="ml-2 text-xs">(친구에게 알려주세요)</span>
          </p>
        )}
      </div>

      {/* 친구 요청 보내기 */}
      <section>
        <h2 className="text-base font-semibold mb-2">친구 추가</h2>
        <div className="flex gap-2">
          <input
            type="number"
            placeholder="상대방 ID 입력"
            value={recipientId}
            onChange={(e) => setRecipientId(e.target.value)}
            className="flex-1 border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={sendRequest}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
          >
            요청 보내기
          </button>
        </div>
        {msg && (
          <p className={`mt-2 text-sm ${msg.ok ? "text-green-600" : "text-red-500"}`}>{msg.text}</p>
        )}
      </section>

      {/* 받은 친구 요청 */}
      {requests.length > 0 && (
        <section>
          <h2 className="text-base font-semibold mb-2">받은 요청 ({requests.length})</h2>
          <div className="space-y-2">
            {requests.map((r) => (
              <div key={r.id} className="bg-white rounded-xl shadow px-4 py-3 flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">{r.nickname}</p>
                  <p className="text-xs text-gray-400">ID: {r.id}</p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => accept(r.id)}
                    className="px-3 py-1.5 bg-blue-600 text-white text-xs rounded-lg hover:bg-blue-700"
                  >
                    수락
                  </button>
                  <button
                    onClick={() => reject(r.id)}
                    className="px-3 py-1.5 border text-xs rounded-lg hover:bg-gray-50"
                  >
                    거절
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* 친구 목록 */}
      <section>
        <h2 className="text-base font-semibold mb-2">친구 목록 ({friends.length})</h2>
        {friends.length === 0 ? (
          <p className="text-gray-400 text-sm py-8 text-center">아직 친구가 없습니다.</p>
        ) : (
          <div className="space-y-2">
            {friends.map((f) => (
              <div key={f.id} className="bg-white rounded-xl shadow px-4 py-3 flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">{f.nickname}</p>
                  <p className="text-xs text-gray-400">ID: {f.id}</p>
                </div>
                <div className="flex gap-2">
                  <Link
                    href={`/bookshelf/${f.id}`}
                    className="px-3 py-1.5 bg-green-600 text-white text-xs rounded-lg hover:bg-green-700"
                  >
                    책장 보기
                  </Link>
                  <button
                    onClick={() => remove(f.id)}
                    className="px-3 py-1.5 border text-xs rounded-lg text-gray-500 hover:bg-gray-50"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
