package com.zhishu.service.crawl;

import com.zhishu.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * JS 渲染抓取：复用系统 Chrome `--headless=new --dump-dom`，经 ProcessBuilder 执行。
 * 仅 render-enabled=true 时使用；线上默认关闭。失败抛异常由编排层回退静态。
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
                "--dump-dom",
                "--virtual-time-budget=8000",
                url
        );
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = p.getInputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
            }
            if (!p.waitFor(props.getTimeoutMs(), TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                throw new IllegalStateException("Chrome 渲染超时");
            }
            int exit = p.exitValue();
            if (exit != 0) {
                throw new IllegalStateException("Chrome 退出码 " + exit);
            }
            return out.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("JS 渲染失败：" + e.getMessage(), e);
        }
    }
}