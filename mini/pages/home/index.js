const api = require("../../utils/api");

function fmtDuration(sec) {
  if (!sec) return "";
  return Math.floor(sec / 60) + ":" + String(sec % 60).padStart(2, "0");
}

Page({
  data: {
    tag: "tech", // tech | blogger
    techCategories: [],
    bloggers: [],
    hotVideos: [],
    hotArticles: [],
    list: [],
    loading: false
  },
  onLoad() {
    this.load();
  },
  onPullDownRefresh() {
    this.load().finally(() => wx.stopPullDownRefresh());
  },
  async load() {
    this.setData({ loading: true });
    try {
      const [hot, techCategories, bloggers] = await Promise.all([
        api.hot(),
        api.categories("video_tech"),
        api.bloggers()
      ]);
      this.setData({
        hotVideos: (hot.videos || []).map((v) => ({ ...v, durationText: fmtDuration(v.duration) })),
        hotArticles: hot.articles,
        techCategories,
        bloggers
      });
      if (this.data.tag === "tech" && techCategories.length) {
        await this.loadList(techCategories[0].catKey);
      } else if (this.data.tag === "blogger" && bloggers.length) {
        await this.loadBloggerList(bloggers[0].id);
      }
    } catch (e) {
      wx.showToast({ title: e.message, icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },
  async loadList(key) {
    const list = (await api.videosByCategory(key)).map((v) => ({ ...v, durationText: fmtDuration(v.duration) }));
    this.setData({ list });
  },
  async loadBloggerList(id) {
    const list = (await api.videosByBlogger(id)).map((v) => ({ ...v, durationText: fmtDuration(v.duration) }));
    this.setData({ list });
  },
  switchTag(e) {
    const tag = e.currentTarget.dataset.tag;
    this.setData({ tag });
    if (tag === "tech" && this.data.techCategories.length) {
      this.loadList(this.data.techCategories[0].catKey);
    } else if (tag === "blogger" && this.data.bloggers.length) {
      this.loadBloggerList(this.data.bloggers[0].id);
    }
  },
  onSelectCategory(e) {
    this.loadList(e.currentTarget.dataset.key);
  },
  onSelectBlogger(e) {
    this.loadBloggerList(e.currentTarget.dataset.id);
  },
  goDetail(e) {
    wx.navigateTo({ url: "/pages/detail/index?id=" + e.currentTarget.dataset.id });
  },
  fmtDuration
});