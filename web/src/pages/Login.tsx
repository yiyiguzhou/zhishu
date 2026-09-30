import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, Form, Input, Button, message, Typography, Space } from "antd";
import { api } from "../api";
import { useAuth } from "../store/auth";

const { Title, Text } = Typography;

export default function Login() {
  const navigate = useNavigate();
  const { setLogin, token, user } = useAuth();
  const [phone, setPhone] = useState("");
  const [countdown, setCountdown] = useState(0);
  const [sending, setSending] = useState(false);

  // 已登录直接回首页
  useEffect(() => {
    if (token && user) navigate("/", { replace: true });
  }, [token, user, navigate]);

  // 倒计时
  useEffect(() => {
    if (countdown <= 0) return;
    const t = setTimeout(() => setCountdown((c) => c - 1), 1000);
    return () => clearTimeout(t);
  }, [countdown]);

  const validPhone = /^1\d{10}$/.test(phone);

  /** 点击获取验证码：先发码到 Redis，成功后启动倒计时。 */
  const sendCode = async () => {
    if (!validPhone) {
      message.warning("请输入正确的 11 位手机号");
      return;
    }
    setSending(true);
    try {
      await api.sendSms(phone);
      message.success("验证码已发送（当前环境固定为 123456）");
      setCountdown(60);
    } catch (e: any) {
      message.error(e.message);
    } finally {
      setSending(false);
    }
  };

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
          未注册的手机号验证通过后将自动注册
        </Text>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item
            name="phone"
            label="手机号"
            rules={[{ required: true, message: "请输入手机号" }]}
          >
            <Input
              maxLength={11}
              placeholder="请输入手机号"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />
          </Form.Item>
          <Form.Item
            name="code"
            label="验证码"
            rules={[{ required: true, message: "请输入验证码" }]}
          >
            <Space>
              <Input placeholder="请输入验证码" style={{ width: 200 }} />
              <Button
                type="default"
                disabled={!validPhone || countdown > 0 || sending}
                loading={sending}
                onClick={sendCode}
              >
                {countdown > 0 ? `${countdown}s 后重发` : "获取验证码"}
              </Button>
            </Space>
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
