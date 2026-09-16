import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, Row, Col, Tabs, Spin, Tag, Typography, Image, Empty, Space } from "antd";
import { FireOutlined } from "@ant-design/icons";
import { api } from "../api";
import type { ArticleDTO, BloggerDTO, CategoryDTO, HotResponse, VideoDTO } from "../api/types";

const { Title, Text } = Typography;

export function formatDuration(sec?: number) {
  if (!sec) return "";
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}:${String(s).padStart(2, "0")}`;
}

export function VideoCard({ video, onClick }: { video: VideoDTO; onClick: () => void }) {
  return (
    <Card
      hoverable
      cover={
        <Image
          src={video.cover || "https://placehold.co/400x225"}
          alt={video.title}
          preview={false}
          style={{ aspectRatio: "16/9", objectFit: "cover" }}
          fallback="https://placehold.co/400x225"
        />
      }
      onClick={onClick}
      style={{ marginBottom: 8 }}
    >
      <Card.Meta
        title={<Text ellipsis>{video.title}</Text>}
        description={
          <Space>
            <Text type="secondary">{video.authorName || "未知作者"}</Text>
            <Tag color="blue">{formatDuration(video.duration)}</Tag>
          </Space>
        }
      />
    </Card>
  );
}

export function VideoGrid({ videos }: { videos: VideoDTO[] }) {
  const navigate = useNavigate();
  if (!videos.length) return <Empty description="暂无视频" style={{ padding: 24 }} />;
  return (
    <Row gutter={[16, 16]}>
    {videos.map((v) => (
      <Col xs={24} sm={12} md={8} key={v.id}>
        <VideoCard video={v} onClick={() => navigate(`/video/${v.id}`)} />
      </Col>
    ))}
    </Row>
  );
}

interface Data {
  hot: HotResponse;
  techCategories: CategoryDTO[];
  bloggers: BloggerDTO[];
}

export default function Home() {
  const [data, setData] = useState<Data | null>(null);
  const [techVideos, setTechVideos] = useState<VideoDTO[]>([]);
  const [bloggerVideos, setBloggerVideos] = useState<VideoDTO[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [hot, techCategories, bloggers] = await Promise.all([
          api.hot(8),
          api.categories("video_tech"),
          api.bloggers()
        ]);
        setData({ hot, techCategories, bloggers });
        if (techCategories.length) {
          onSelectTech(techCategories[0].catKey);
        }
        if (bloggers.length) {
          onSelectBlogger(bloggers[0].id);
        }
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const onSelectTech = async (key: string) => {
    setTechVideos(await api.videosByCategory(key));
  };
  const onSelectBlogger = async (id: number) => {
    setBloggerVideos(await api.videosByBlogger(id));
  };

  if (loading && !data) return <Spin style={{ display: "block", margin: "60px auto" }} />;

  const techTabs = (data?.techCategories || []).map((c) => ({ key: c.catKey, label: c.name }));
  const bloggerTabs = (data?.bloggers || []).map((b) => ({
    key: String(b.id),
    label: b.name,
    avatar: b.avatar
  }));
  const bloggerMap = new Map(data?.bloggers.map((b) => [String(b.id), b] as [string, BloggerDTO]));

  return (
    <div>
      <Title level={4}>
        <FireOutlined style={{ color: "#f5222d" }} /> 热点内容
      </Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={16}>
          <VideoGrid videos={data?.hot.videos || []} />
        </Col>
        <Col xs={24} md={8}>
          <Card title="热点文章" size="small">
            {(data?.hot.articles || []).map((a: ArticleDTO) => (
              <div key={a.id} style={{ padding: "6px 0", borderBottom: "1px solid #f0f0f0" }}>
                <Text ellipsis>{a.title}</Text>
                <Text type="secondary" style={{ float: "right" }}>
                  {a.authorName}
                </Text>
              </div>
            ))}
          </Card>
        </Col>
      </Row>

      <Title level={4} style={{ marginTop: 32 }}>
        按技术分类
      </Title>
      <Tabs
        items={techTabs.map((t) => ({
          key: t.key,
          label: t.label,
          children: <VideoGrid videos={techVideos} />
        }))}
        onChange={(k) => onSelectTech(k)}
      />

      <Title level={4} style={{ marginTop: 32 }}>
        按博主分类
      </Title>
      <Tabs
        items={bloggerTabs.map((t) => ({
          key: t.key,
          label: (
            <Space>
              <img
                width={28}
                height={28}
                style={{ borderRadius: 14 }}
                src={bloggerMap.get(t.key)?.avatar || "https://placehold.co/28"}
                alt=""
              />
              {t.label}
            </Space>
          ),
          children: <VideoGrid videos={bloggerVideos} />
        }))}
        onChange={(k) => onSelectBlogger(Number(k))}
      />
    </div>
  );
}