import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Avatar, Button, Card, FloatButton, Input, Space } from "antd";
import { RobotOutlined, UserOutlined } from "@ant-design/icons";
import { useAuth } from "../store/auth";

interface Msg {
  role: "user" | "assistant";
  content: string;
}

const GREETING = "你好，我是纸书学习助手，大模型学习中的问题都可以问我～";

export default function ChatAssistant() {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const [messages, setMessages] = useState<Msg[]>([
    { role: "assistant", content: GREETING }
  ]);
  const listRef = useRef<HTMLDivElement>(null);
  const abortRef = useRef<AbortController | null>(null);

  const scrollBottom = () => {
    requestAnimationFrame(() => {
      const el = listRef.current;
      if (el) el.scrollTop = el.scrollHeight;
    });
  };

  const send = async () => {
    const question = input.trim();
    if (!question || sending) return;

    if (!token) {
      navigate("/login");
      return;
    }

    const history: Msg[] = [...messages, { role: "user", content: question }];
    setMessages(history);
    setInput("");
    setSending(true);
    scrollBottom();

    const controller = new AbortController();
    abortRef.current = controller;

    setMessages((m) => [...m, { role: "assistant", content: "" }]);

    try {
      const resp = await fetch("/api/assistant/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          messages: messages
            .filter((m) => m.content !== GREETING)
            .concat({ role: "user", content: question })
        }),
        signal: controller.signal
      });

      if (!resp.ok || !resp.body) {
        throw new Error(`请求失败（${resp.status}）`);
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";
      let answer = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });

        // SSE 帧以空行分隔，事件为纯文本增量
        const frames = buffer.split("\n\n");
        buffer = frames.pop() ?? "";
        for (const frame of frames) {
          const line = frame.split("\n").find((l) => l.startsWith("data:"));
          if (!line) continue;
          answer += line.slice(5).replace(/^ ?/, "");
          paintAnswer(answer);
        }
      }
      if (buffer) {
        const line = buffer.split("\n").find((l) => l.startsWith("data:"));
        if (line) {
          answer += line.slice(5).replace(/^ ?/, "");
          paintAnswer(answer);
        }
      }
    } catch (e) {
      const msg = (e as Error).name === "AbortError" ? "已停止生成" : `答疑失败：${(e as Error).message}`;
      setMessages((m) => {
        const copy = [...m];
        const last = copy[copy.length - 1];
        if (last && last.role === "assistant") {
          copy[copy.length - 1] = { role: "assistant", content: last.content || msg };
        }
        return copy;
      });
    } finally {
      setSending(false);
      abortRef.current = null;
      scrollBottom();
    }
  };

  const paintAnswer = (answer: string) => {
    setMessages((m) => {
      const copy = [...m];
      copy[copy.length - 1] = { role: "assistant", content: answer };
      return copy;
    });
    scrollBottom();
  };

  // 未登录不展示入口（助手仅登录用户可用）
  if (!token) {
    return null;
  }

  return (
    <>
      <FloatButton
        icon={<RobotOutlined />}
        tooltip="大模型学习助手"
        onClick={() => setOpen((v) => !v)}
      />
      {open && (
        <Card
          size="small"
          title={
            <Space>
              <RobotOutlined style={{ color: "#1677ff" }} />
              大模型学习助手
            </Space>
          }
          style={{
            position: "fixed",
            right: 24,
            bottom: 64,
            width: 380,
            zIndex: 1000,
            boxShadow: "0 8px 24px rgba(0,0,0,0.15)"
          }}
          styles={{ body: { padding: 12 } }}
        >
          <div
            ref={listRef}
            style={{ height: 320, overflowY: "auto", paddingRight: 4 }}
          >
            {messages.map((m, i) => (
              <Space
                key={i}
                align="start"
                style={{
                  display: "flex",
                  justifyContent: m.role === "user" ? "flex-end" : "flex-start",
                  marginBottom: 12
                }}
              >
                {m.role === "assistant" && (
                  <Avatar size="small" icon={<RobotOutlined />} style={{ background: "#1677ff" }} />
                )}
                <div
                  style={{
                    maxWidth: 260,
                    padding: "6px 10px",
                    borderRadius: 8,
                    whiteSpace: "pre-wrap",
                    background: m.role === "user" ? "#1677ff" : "#f5f5f5",
                    color: m.role === "user" ? "#fff" : "inherit"
                  }}
                >
                  {m.content}
                  {sending && i === messages.length - 1 && m.role === "assistant" && !m.content && "正在输入…"}
                </div>
                {m.role === "user" && (
                  <Avatar size="small" icon={<UserOutlined />} />
                )}
              </Space>
            ))}
          </div>
          <Space.Compact style={{ width: "100%", marginTop: 8 }}>
            <Input.TextArea
              value={input}
              autoSize={{ minRows: 1, maxRows: 3 }}
              placeholder={token ? "输入你的问题…" : "登录后使用学习助手"}
              onChange={(e) => setInput(e.target.value)}
              onPressEnter={(e) => {
                if (!e.shiftKey) {
                  e.preventDefault();
                  send();
                }
              }}
            />
            {sending ? (
              <Button danger onClick={() => abortRef.current?.abort()}>
                停止
              </Button>
            ) : (
              <Button type="primary" onClick={send}>
                发送
              </Button>
            )}
          </Space.Compact>
        </Card>
      )}
    </>
  );
}
