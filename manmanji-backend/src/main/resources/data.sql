-- ============================================
-- 慢慢记 (ManManJi) 初始数据
-- ============================================

-- 默认分类
INSERT INTO categories (id, name, slug, description, sort_order) VALUES
(1, '后端开发', 'backend', 'Java/Spring/数据库等后端技术', 1),
(2, '前端开发', 'frontend', 'Vue/React/TypeScript 等前端技术', 2),
(3, 'DevOps', 'devops', 'CI/CD/Docker/K8s 等运维技术', 3),
(4, 'AI & 机器学习', 'ai-ml', 'LLM/RAG/Embedding 等 AI 技术', 4),
(5, '架构设计', 'architecture', '系统设计/微服务/分布式等架构话题', 5),
(6, '工具推荐', 'tools', '开发工具/效率工具推荐', 6)
ON CONFLICT (slug) DO NOTHING;

-- 默认标签
INSERT INTO tags (id, name, slug, color) VALUES
(1, 'Java', 'java', '#f89820'),
(2, 'Spring Boot', 'spring-boot', '#6db33f'),
(3, 'Vue', 'vue', '#4fc08d'),
(4, 'PostgreSQL', 'postgresql', '#336791'),
(5, 'Redis', 'redis', '#dc382c'),
(6, 'Docker', 'docker', '#2496ed'),
(7, 'AI', 'ai', '#8b5cf6'),
(8, '微服务', 'microservices', '#ef4444'),
(9, 'Linux', 'linux', '#fcc624'),
(10, 'Git', 'git', '#f05032')
ON CONFLICT (slug) DO NOTHING;

-- 内置 LLM Provider 配置（API Key 仍通过环境变量注入，加密字段留空）
INSERT INTO llm_provider_config (id, base_url, model, embedding_model, embedding_dimensions, supports_embedding, temperature, enabled, builtin) VALUES
('dashscope', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen3.5-flash', 'text-embedding-v3', 1024, TRUE,  NULL, TRUE, TRUE),
('lmstudio',  'http://localhost:1234',                          'qwen2.5-7b-instruct', NULL,                NULL,   FALSE, NULL, TRUE, TRUE),
('kimi',      'https://api.moonshot.cn/v1',                     'kimi-latest',          NULL,                NULL,   FALSE, 1.0,   TRUE, TRUE),
('deepseek',  'https://api.deepseek.com',                       'deepseek-v4-flash',    NULL,                NULL,   FALSE, NULL, TRUE, TRUE),
('glm',       'https://open.bigmodel.cn/api/coding/paas/v4',   'glm-5',                'embedding-3',       1024,   TRUE,  NULL, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;
