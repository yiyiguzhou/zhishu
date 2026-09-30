import { create } from "zustand";
import type { UserDTO } from "../api/types";

const TOKEN_KEY = "zhishu_token";
const USER_KEY = "zhishu_user";

/** 从 localStorage 安全读取缓存的用户信息。 */
function loadCachedUser(): UserDTO | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as UserDTO) : null;
  } catch {
    return null;
  }
}

interface AuthState {
  token: string | null;
  user: UserDTO | null;
  setLogin: (token: string, user: UserDTO) => void;
  setUser: (user: UserDTO) => void;
  logout: () => void;
}

export const useAuth = create<AuthState>((set) => ({
  // 刷新时 token 与用户信息都立即从本地恢复，无需等待 profile 请求
  token: localStorage.getItem(TOKEN_KEY),
  user: loadCachedUser(),
  setLogin: (token, user) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    set({ token, user });
  },
  setUser: (user) => {
    // profile 后台刷新到新信息时同步更新缓存
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    set({ user });
  },
  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    set({ token: null, user: null });
  }
}));
