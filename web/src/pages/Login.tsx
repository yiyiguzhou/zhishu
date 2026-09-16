import { useNavigate } from "react-router-dom";
import { Card, Form, Input, Button, message, Typography } from "antd";
import { api } from "../api";
import { useAuth } from "../store/auth";

const { Title, Text } = Typography;

export default function Login() {
  const navigate = useNavigate();
  const { setLogin } = useAuth();

  const onFinish = async (values: { phone: string; code: string }) => {
    try {
      const res = await api.login({ phone: values.phone, code: values.code });
      setLogin(res.token, res.user);
      message.success("登录成功");
      navigate("/");
    } catch (e: any) {
      message.error(e.message);
    }
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", paddingTop: 48 }}>
      <Card style={{ width: 380 }}>
        <Title level={4} style={{ textAlign: "center" }}>
          手机号登录 / 注册
        </Title>
        <Text type="secondary" style={{ display: "block", textAlign: "center", marginBottom: 16 }}>
          未注册的手机号验证通过后将自动注册（开发期验证码为 123456）
        </Text>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item
            name="phone"
            label="手机号"
            rules={[{ required: true, message: "请输入手机号" }]}
          >
            <Input maxLength={11} placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item
            name="code"
            label="验证码"
            rules={[{ required: true, message: "请输入验证码" }]}
          >
            <Input placeholder="开发期固定为 123456" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              登录 / 注册
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}