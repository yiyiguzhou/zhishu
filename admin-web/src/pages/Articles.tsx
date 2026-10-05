import { useEffect, useState } from "react";
import {
  Table, Button, Modal, Form, Input, Select, InputNumber, Switch, message, Popconfirm, Space, Tabs
} from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { api } from "../api";

export default function Articles() {
  const [data, setData] = useState<any>({ records: [], total: 0 });
  const [bloggers, setBloggers] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();
  const [mode, setMode] = useState("manual");

  const load = async (p = page) => {
    setLoading(true);
    try {
      const res: any = await api.articles(p, 20);
      setData({ records: res.records ?? [], total: res.total ?? 0 });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(1);
    api.bloggers().then(setBloggers);
  }, []);

  const onCreate = async () => {
    const values = await form.validateFields();
    const payload: any = { ...values, mode };
    try {
      await api.createArticle(payload);
      message.success("创建成功");
      setOpen(false);
      form.resetFields();
      load(1);
    } catch (e: any) {
      message.error(e.message);
    }
  };

  const columns = [
    { title: "ID", dataIndex: "id", width: 70 },
    { title: "标题", dataIndex: "title", ellipsis: true },
    { title: "作者ID", dataIndex: "bloggerId", width: 90 },
    { title: "分类", dataIndex: "categoryKey", width: 110 },
    { title: "热度", dataIndex: "hotScore", width: 80 },
    {
      title: "上线",
      dataIndex: "valid",
      width: 90,
      render: (v: number, row: any) => (
        <Switch
          checked={v === 1}
          onChange={async (checked) => {
            await api.setArticleValid(row.id, checked ? 1 : 0);
            message.success(checked ? "已上线" : "已下线");
            load();
          }}
        />
      )
    },
    {
      title: "操作",
      width: 90,
      render: (_: any, row: any) => (
        <Popconfirm title="确认删除？" onConfirm={async () => { await api.deleteArticle(row.id); load(); }}>
          <Button size="small" danger>删除</Button>
        </Popconfirm>
      )
    }
  ];

  const commonItems = [
    { name: "title", label: "标题", required: true, el: <Input /> },
    { name: "bloggerId", label: "作者（可选）", el: <Select allowClear options={bloggers.map((b) => ({ value: b.id, label: b.name }))} /> },
    { name: "categoryKey", label: "分类Key", el: <Input /> },
    { name: "hotScore", label: "热度", el: <InputNumber style={{ width: "100%" }} /> },
    { name: "authorName", label: "外部作者名", el: <Input /> }
  ];

  return (
    <div>
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }} onClick={() => { form.resetFields(); setMode("manual"); setOpen(true); }}>
        创建文章
      </Button>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data.records}
        loading={loading}
        pagination={{ current: page, total: data.total, pageSize: 20, onChange: (p) => { setPage(p); load(p); } }}
      />
      <Modal title="创建文章" open={open} onOk={onCreate} onCancel={() => setOpen(false)} width={640}>
        <Tabs
          activeKey={mode}
          onChange={(k) => setMode(k)}
          items={[
            { key: "manual", label: "手动 Markdown" },
            { key: "raw", label: "贴原文 AI" },
            { key: "fetch", label: "抓 URL AI" }
          ]}
        />
        <Form form={form} layout="vertical">
          {commonItems.map((it) => (
            <Form.Item key={it.name} name={it.name} label={it.label} rules={it.required ? [{ required: true, message: `请填写${it.label}` }] : []}>
              {it.el}
            </Form.Item>
          ))}
          {mode === "manual" && (
            <Form.Item name="contentMd" label="Markdown 正文" rules={[{ required: true, message: "请输入正文" }]}>
              <Input.TextArea rows={8} />
            </Form.Item>
          )}
          {mode === "raw" && (
            <Form.Item name="rawText" label="原文正文" rules={[{ required: true, message: "请输入原文" }]}>
              <Input.TextArea rows={8} />
            </Form.Item>
          )}
          {mode === "fetch" && (
            <Form.Item name="sourceUrl" label="原文 URL" rules={[{ required: true, message: "请输入 URL" }]}>
              <Input />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
}