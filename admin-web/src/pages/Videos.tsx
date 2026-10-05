import { useEffect, useState } from "react";
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm, Upload, Space } from "antd";
import { PlusOutlined, UploadOutlined } from "@ant-design/icons";
import { api } from "../api";

export default function Videos() {
  const [data, setData] = useState<any>({ records: [], total: 0 });
  const [bloggers, setBloggers] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [importOpen, setImportOpen] = useState(false);
  const [importForm] = Form.useForm();
  const [fileList, setFileList] = useState<any[]>([]);

  const load = async (p = page) => {
    setLoading(true);
    try {
      const res: any = await api.videos(p, 20);
      setData({ records: res.records ?? [], total: res.total ?? 0 });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(1);
    api.bloggers().then(setBloggers);
    api.categories().then(setCategories);
  }, []);

  const doImport = async () => {
    const values = await importForm.validateFields();
    const formData = new FormData();
    formData.append("bloggerId", values.bloggerId);
    if (values.categoryId) formData.append("categoryId", values.categoryId);
    fileList.forEach((f) => formData.append("files", f.originFileObj));
    try {
      const ids = await api.importVideos(formData);
      message.success(`导入成功 ${ids.length} 个视频`);
      setImportOpen(false);
      importForm.resetFields();
      setFileList([]);
      load(1);
    } catch (e: any) {
      message.error(e.message);
    }
  };

  const columns = [
    { title: "ID", dataIndex: "id", width: 70 },
    { title: "标题", dataIndex: "title", ellipsis: true },
    { title: "作者ID", dataIndex: "bloggerId", width: 90 },
    { title: "分类ID", dataIndex: "categoryId", width: 90 },
    { title: "时长", dataIndex: "duration", width: 80 },
    { title: "source_type", dataIndex: "sourceType", width: 110 },
    {
      title: "操作",
      width: 90,
      render: (_: any, row: any) => (
        <Popconfirm title="确认删除？" onConfirm={async () => { await api.deleteVideo(row.id); load(); }}>
          <Button size="small" danger>删除</Button>
        </Popconfirm>
      )
    }
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<UploadOutlined />} onClick={() => setImportOpen(true)}>
          批量导入视频
        </Button>
      </Space>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data.records}
        loading={loading}
        pagination={{ current: page, total: data.total, pageSize: 20, onChange: (p) => { setPage(p); load(p); } }}
      />
      <Modal title="批量导入视频" open={importOpen} onOk={doImport} onCancel={() => setImportOpen(false)}>
        <Form form={importForm} layout="vertical">
          <Form.Item name="bloggerId" label="作者" rules={[{ required: true, message: "请选择作者" }]}>
            <Select options={bloggers.map((b) => ({ value: b.id, label: b.name }))} />
          </Form.Item>
          <Form.Item name="categoryId" label="分类（可选）">
            <Select allowClear options={categories.filter((c) => c.catType === "video_tech").map((c) => ({ value: c.id, label: c.name }))} />
          </Form.Item>
          <Form.Item label="视频文件（可多选 mp4）">
            <Upload
              multiple
              beforeUpload={() => false}
              fileList={fileList}
              onChange={({ fileList }) => setFileList(fileList)}
              accept=".mp4"
            >
              <Button icon={<UploadOutlined />}>选择文件</Button>
            </Upload>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}