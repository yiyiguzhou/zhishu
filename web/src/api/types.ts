export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface UserDTO {
  id: number;
  phone?: string;
  nickname: string;
  avatar?: string;
  createdAt: string;
}

export interface LoginResponse {
  token: string;
  user: UserDTO;
}

export interface VideoDTO {
  id: number;
  title: string;
  cover?: string;
  duration?: number;
  categoryKey?: string;
  hotScore?: number;
  authorName?: string;
  createdAt: string;
}

export interface ArticleDTO {
  id: number;
  title: string;
  cover?: string;
  categoryKey?: string;
  hotScore?: number;
  authorName?: string;
  createdAt: string;
}

export interface HotResponse {
  videos: VideoDTO[];
  articles: ArticleDTO[];
}

export interface CategoryDTO {
  id: number;
  name: string;
  catKey: string;
  catType: string;
}

export interface BloggerDTO {
  id: number;
  name: string;
  avatar?: string;
  introduction?: string;
}

export interface VideoDetailDTO {
  id: number;
  title: string;
  cover?: string;
  duration?: number;
  categoryId?: number;
  categoryName?: string;
  tags?: string[];
  bloggerId?: number;
  authorName?: string;
  authorAvatar?: string;
  favorited: boolean;
  playUrl?: string;
  favoriteCount?: number;
}

export interface HistoryDTO {
  id: number;
  targetType: string;
  targetId: number;
  watchedProgress?: number;
  lastWatchedAt: string;
  title?: string;
  cover?: string;
}

export interface HotResult {
  hot: HotResponse;
  techCategories: CategoryDTO[];
  bloggers: BloggerDTO[];
}