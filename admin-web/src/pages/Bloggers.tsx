import { useEffect, useState } from "react";
import { Table, Button, Modal, Form, Input, message, Popconfirm, Space } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { api } from "../api";

export default function Bloggers() {
  const [list, setList] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<any>(null);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      setList(await api.bloggers());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const onSave = async () => {
    const values = await form.validateFields();
    try {
      if (editing) {
        await api.updateBlogger(editing.id, values);
      } else {
        await api.createBlogger(values);
      }
      message.success("已保存");
      setOpen(false);
      form.resetFields();
      load();
    } catch (e: any) {
      message.error(e.message);
    }
  };

  const columns = [
    { title: "ID", dataIndex: "id", width: 70 },
    { title: "名称", dataIndex: "name" },
    { title: "简介", dataIndex: "introduction", ellipsis: true },
    {
      title: "操作",
      width: 160,
      render: (_: any, row: any) => (
        <Space>
          <Button size="small" onClick={() => { setEditing(row); form.setFieldsValue(row); setOpen(true); }}>编辑</Button>
          <Popconfirm title="确认删除？" onConfirm={async () => { await api.deleteBlogger(row.id); load(); }}>
            <Button size="small" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <div>
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }} onClick={() => { setEditing(null); form.resetFields(); setOpen(true); }}>
        新增作者
      </Button>
      <Table rowKey="id" columns={columns} dataSource={list} loading={loading} />
      <Modal title={editing ? "编辑作者" : "新增作者"} open={open} onOk={onSave} onCancel={() => setOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="avatar" label="头像URL"><Input /></Form.Item>
          <Form.Item name="introduction" label="简介"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}