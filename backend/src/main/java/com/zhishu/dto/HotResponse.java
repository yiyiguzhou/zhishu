package com.zhishu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 热点模块：视频 + 文章。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotResponse {
    private List<VideoDTO> videos;
    private List<ArticleDTO> articles;
}