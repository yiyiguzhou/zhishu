package com.zhishu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 网页抓取结果：纯文本正文 + 页面标题回填 + 是否走了 JS 渲染。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchResult {
    private String title;
    private String content;
    private boolean rendered;
}