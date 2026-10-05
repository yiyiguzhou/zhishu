import client from "./client";

export const api = {
  // 登录
  login: (data: { username: string; password: string }) =>
    client.post("/api/admin/auth/login", data) as Promise<{ token: string; username: string }>,

  // 作者
  bloggers: () => client.get("/api/admin/bloggers") as Promise<any[]>,
  createBlogger: (data: any) => client.post("/api/admin/bloggers", data),
  updateBlogger: (id: number, data: any) => client.put(`/api/admin/bloggers/${id}`, data),
  deleteBlogger: (id: number) => client.delete(`/api/admin/bloggers/${id}`),

  // 分类
  categories: () => client.get("/api/admin/categories") as Promise<any[]>,
  createCategory: (data: any) => client.post("/api/admin/categories", data),
  updateCategory: (id: number, data: any) => client.put(`/api/admin/categories/${id}`, data),
  deleteCategory: (id: number) => client.delete(`/api/admin/categories/${id}`),

  // 标签
  tags: () => client.get("/api/admin/tags") as Promise<any[]>,
  createTag: (data: any) => client.post("/api/admin/tags", data),
  updateTag: (id: number, data: any) => client.put(`/api/admin/tags/${id}`, data),
  deleteTag: (id: number) => client.delete(`/api/admin/tags/${id}`),

  // 视频
  videos: (page = 1, size = 20) =>
    client.get("/api/admin/videos", { params: { page, size } }) as Promise<any>,
  createVideo: (data: any) => client.post("/api/admin/videos", data),
  deleteVideo: (id: number) => client.delete(`/api/admin/videos/${id}`),
  importVideos: (formData: FormData) =>
    client.post("/api/admin/videos/import", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    }) as Promise<number[]>,

  // 文章
  articles: (page = 1, size = 20) =>
    client.get("/api/admin/articles", { params: { page, size } }) as Promise<any>,
  createArticle: (data: any) => client.post("/api/admin/articles", data) as Promise<number>,
  setArticleValid: (id: number, valid: number) =>
    client.put(`/api/admin/articles/${id}/valid`, null, { params: { valid } }),
  deleteArticle: (id: number) => client.delete(`/api/admin/articles/${id}`)
};