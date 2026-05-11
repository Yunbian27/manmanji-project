package com.yunbian27.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticlePlusService {
    public String improve(String article) {
        //调用ai，获取生成后的文章
        log.info("ai润色后的文章：");
        return "生成后的文章";
    }
}
