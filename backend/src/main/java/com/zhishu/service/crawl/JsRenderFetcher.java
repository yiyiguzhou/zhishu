package com.zhishu.service.crawl;

import com.zhishu.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * JS 渲染抓取：复用系统 Chrome `--headless=new --dump-dom`，经 ProcessBuilder 执行。
 * 仅 render-enabled=true 时使用；线上默认关闭。失败抛异常由编排层回退静态。
 *
 * 注意：Chrome dump 完 DOM 后往往不主动退出（受代理/后台请求拖累），
 * 故读到完整 HTML 结束标记即判定成功，随后主动 destroyForcibly。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsRenderFetcher {

    private final CrawlerProperties props;

    public boolean available() {
        return props.isRenderEnabled();
    }

    /** 用无头 Chrome dump DOM，返回渲染后的完整 HTML。 */
    public String render(String url) {
        if (!props.isRenderEnabled()) {
            throw new IllegalStateException("JS 渲染未启用");
        }
        ProcessBuilder pb = new ProcessBuilder(
                props.getChromePath(),
                "--headless=new",
                "--disable-gpu",
                "--no-sandbox",
                "--no-proxy-server",
                "--disable-background-networking",
                "--user-data-dir=/tmp/zhishu-chrome-" + System.currentTimeMillis(),
                "--dump-dom",
                "--virtual-time-budget=8000",
                url
        );
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CountDownLatch done = new CountDownLatch(1);
            // 后台线程读 stdout：读到 HTML 结束标记即完成，不再等待 Chrome 退出。
            Thread reader = new Thread(() -> {
                try (InputStream in = p.getInputStream()) {
                    byte[] buf = new byte[8192];
                    int n;
                    String s;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                        s = out.toString(StandardCharsets.UTF_8);
                        if (s.contains("</html>") || out.size() > props.getMaxBytes()) {
                            done.countDown();
                            return;
                        }
                    }
                    done.countDown();
                } catch (Exception ignored) {
                    done.countDown();
                }
            });
            reader.start();

            boolean finished = done.await(props.getTimeoutMs(), TimeUnit.MILLISECONDS);
            p.destroyForcibly();
            if (!finished) {
                throw new IllegalStateException("Chrome 渲染超时");
            }
            String html = out.toString(StandardCharsets.UTF_8);
            if (html.length() < 100) {
                throw new IllegalStateException("Chrome 渲染结果为空");
            }
            return html;
        } catch (Exception e) {
            throw new RuntimeException("JS 渲染失败：" + e.getMessage(), e);
        }
    }
}