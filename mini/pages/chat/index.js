const api = require("../../utils/api");

const GREETING = "你好，我是纸书学习助手，大模型学习中的问题都可以问我～";

Page({
  data: {
    messages: [{ role: "assistant", content: GREETING }],
    input: "",
    sending: false,
    scrollTop: 0
  },

  onInput(e) {
    this.setData({ input: e.detail.value });
  },

  scrollBottom() {
    this.setData({ scrollTop: 99999 + Math.random() });
  },

  async send() {
    const question = this.data.input.trim();
    if (!question || this.data.sending) return;

    if (!api.getUser()) {
      wx.navigateTo({ url: "/pages/login/index" });
      return;
    }

    const history = this.data.messages
      .filter((m) => m.content !== GREETING)
      .concat({ role: "user", content: question });

    const messages = this.data.messages.concat([
      { role: "user", content: question },
      { role: "assistant", content: "" }
    ]);
    this.setData({ messages, input: "", sending: true });
    this.scrollBottom();

    try {
      const data = await api.assistantChat({
        messages: history
      });
      const list = this.data.messages;
      list[list.length - 1] = { role: "assistant", content: data.content || "（无回复）" };
      this.setData({ messages: list });
    } catch (e) {
      const list = this.data.messages;
      list[list.length - 1] = { role: "assistant", content: "答疑失败：" + e.message };
      this.setData({ messages: list });
    } finally {
      this.setData({ sending: false });
      this.scrollBottom();
    }
  }
});
