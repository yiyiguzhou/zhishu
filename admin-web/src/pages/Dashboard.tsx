import { useEffect, useState } from "react";
import { Card, Row, Col, Statistic } from "antd";
import { UserOutlined, VideoCameraOutlined, FileTextOutlined, TagsOutlined } from "@ant-design/icons";
import { api } from "../api";

export default function Dashboard() {
  const [stats, setStats] = useState({ bloggers: 0, videos: 0, articles: 0, tags: 0 });

  useEffect(() => {
    (async () => {
      const [bloggers, videos, articles, tags] = await Promise.all([
        api.bloggers(),
        api.videos(1, 1),
        api.articles(1, 1),
        api.tags()
      ]);
      setStats({
        bloggers: bloggers.length,
        videos: videos?.total ?? 0,
        articles: articles?.total ?? 0,
        tags: tags.length
      });
    })();
  }, []);

  return (
    <Row gutter={16}>
      <Col span={6}><Card><Statistic title="作者" value={stats.bloggers} prefix={<UserOutlined />} /></Card></Col>
      <Col span={6}><Card><Statistic title="视频" value={stats.videos} prefix={<VideoCameraOutlined />} /></Card></Col>
      <Col span={6}><Card><Statistic title="文章" value={stats.articles} prefix={<FileTextOutlined />} /></Card></Col>
      <Col span={6}><Card><Statistic title="标签" value={stats.tags} prefix={<TagsOutlined />} /></Card></Col>
    </Row>
  );
}