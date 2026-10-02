import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

/**
 * 简约美的 Markdown 排版：延续项目 antd 视觉语言——#1677ff 强调、
 * #f0f0f0 分隔、灰底代码块，无独立 CSS 文件（纯内联 style）。
 */
export default function Markdown({ content }: { content?: string }) {
  if (!content) return null;
  return (
    <div style={{ fontSize: 15, lineHeight: 1.8, color: "rgba(0,0,0,0.88)" }}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          h1: ({ node: _n, ...p }: any) => (
            <h1 style={{ fontSize: 24, fontWeight: 700, margin: "22px 0 12px", paddingBottom: 8, borderBottom: "2px solid #f0f0f0" }} {...p} />
          ),
          h2: ({ node: _n, ...p }: any) => (
            <h2 style={{ fontSize: 20, fontWeight: 600, margin: "18px 0 10px", paddingBottom: 6, borderBottom: "1px solid #f0f0f0" }} {...p} />
          ),
          h3: ({ node: _n, ...p }: any) => (
            <h3 style={{ fontSize: 17, fontWeight: 600, margin: "16px 0 8px" }} {...p} />
          ),
          p: ({ node: _n, ...p }: any) => <p style={{ margin: "8px 0" }} {...p} />,
          ul: ({ node: _n, ...p }: any) => <ul style={{ paddingLeft: 20, margin: "8px 0" }} {...p} />,
          ol: ({ node: _n, ...p }: any) => <ol style={{ paddingLeft: 20, margin: "8px 0" }} {...p} />,
          li: ({ node: _n, ...p }: any) => <li style={{ margin: "4px 0" }} {...p} />,
          blockquote: ({ node: _n, ...p }: any) => (
            <blockquote style={{ margin: "8px 0", padding: "8px 16px", borderLeft: "4px solid #1677ff", background: "#f5f7fa", borderRadius: 4, color: "rgba(0,0,0,0.65)" }} {...p} />
          ),
          code: ({ node: _n, inline: _i, ...p }: any) => (
            <code style={{ background: "#f5f5f5", padding: "1px 6px", borderRadius: 4, fontFamily: "SFMono-Regular, Consolas, monospace", fontSize: 13, color: "#c7254e" }} {...p} />
          ),
          pre: ({ node: _n, ...p }: any) => (
            <pre style={{ background: "#282c34", color: "#abb2bf", padding: 16, borderRadius: 8, overflow: "auto", lineHeight: 1.5 }} {...p} />
          ),
          a: ({ node: _n, ...p }: any) => (
            <a style={{ color: "#1677ff" }} target="_blank" rel="noreferrer" {...p} />
          ),
          strong: ({ node: _n, ...p }: any) => <strong style={{ fontWeight: 600 }} {...p} />,
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}