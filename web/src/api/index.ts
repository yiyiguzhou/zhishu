import client from "./client";
import type {
  ArticleDTO,
  BloggerDTO,
  CategoryDTO,
  HistoryDTO,
  HotResponse,
  LoginResponse,
  UserDTO,
  VideoDTO,
  VideoDetailDTO
} from "./types";

export const api = {
  // 认证
  sendSms: (phone: string) =>
    client.post("/api/auth/sms-code", null, { params: { phone } }),
  register: (data: { phone: string; code: string; nickname?: string }) =>
    client.post("/api/auth/register", data) as Promise<LoginResponse>,
  login: (data: { phone: string; code: string }) =>
    client.post("/api/auth/login", data) as Promise<LoginResponse>,

  // 热点 / 分类
  hot: (limit = 10) => client.get("/api/hot", { params: { limit } }) as Promise<HotResponse>,
  categories: (type = "video_tech") =>
    client.get("/api/categories", { params: { type } }) as Promise<CategoryDTO[]>,
  videosByCategory: (key: string) =>
    client.get(`/api/categories/${key}/videos`) as Promise<VideoDTO[]>,
  bloggers: () => client.get("/api/bloggers") as Promise<BloggerDTO[]>,
  videosByBlogger: (id: number) =>
    client.get(`/api/bloggers/${id}/videos`) as Promise<VideoDTO[]>,

  // 视频
  videoDetail: (id: number) => client.get(`/api/videos/${id}`) as Promise<VideoDetailDTO>,

  // 用户中心
  profile: () => client.get("/api/user/profile") as Promise<UserDTO>,
  favorites: () => client.get("/api/user/favorites") as Promise<HistoryDTO[]>,
  addFavorite: (targetType: "video" | "article", targetId: number) =>
    client.post("/api/user/favorites", { targetType, targetId }),
  removeFavorite: (targetType: string, targetId: number) =>
    client.delete(`/api/user/favorites/${targetType}/${targetId}`),
  history: () => client.get("/api/user/history") as Promise<HistoryDTO[]>,
  addHistory: (targetType: string, targetId: number, watchedProgress?: number) =>
    client.post("/api/user/history", { targetType, targetId, watchedProgress })
};

export type { ArticleDTO }; // re-export convenience