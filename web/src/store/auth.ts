import { create } from "zustand";
import type { UserDTO } from "../api/types";

interface AuthState {
  token: string | null;
  user: UserDTO | null;
  setLogin: (token: string, user: UserDTO) => void;
  setUser: (user: UserDTO) => void;
  logout: () => void;
}

export const useAuth = create<AuthState>((set) => ({
  token: localStorage.getItem("zhishu_token"),
  user: null,
  setLogin: (token, user) => {
    localStorage.setItem("zhishu_token", token);
    set({ token, user });
  },
  setUser: (user) => set({ user }),
  logout: () => {
    localStorage.removeItem("zhishu_token");
    set({ token: null, user: null });
  }
}));