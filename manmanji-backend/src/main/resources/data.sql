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
(1,  'Java',           'java',           '#f89820'),
(2,  '并发',            'concurrency',    '#ef4444'),
(3,  'PostgreSQL',     'postgresql',     '#336791'),
(4,  '数据库',          'database',       '#f59e0b'),
(5,  'Kubernetes',     'kubernetes',     '#326ce5'),
(6,  '分布式',          'distributed',    '#8b5cf6'),
(7,  'Spring Boot',    'spring-boot',    '#6db33f'),
(8,  '前端',            'frontend',       '#4fc08d'),
(9,  'Go',             'go',             '#00add8'),
(10, 'Redis',          'redis',          '#dc382c'),
(11, '源码',            'source-code',    '#6b7280'),
(12, '设计模式',         'design-patterns','#ec4899'),
(13, 'Linux',          'linux',          '#fcc624'),
(14, 'Python',         'python',         '#3776ab'),
(15, '安全',            'security',       '#dc2626'),
(16, 'TypeScript',     'typescript',     '#3178c6'),
(17, 'Elasticsearch',  'elasticsearch',  '#00bfb3'),
(18, '消息队列',         'message-queue',  '#f97316'),
(19, 'Nginx',          'nginx',          '#009639'),
(20, 'Docker',         'docker',         '#2496ed'),
(21, '微服务',           'microservices',  '#ef4444'),
(22, '性能优化',         'performance',    '#eab308'),
(23, '网络',            'network',        '#06b6d4'),
(24, '运维',            'devops',         '#a855f7'),
(25, 'AI',             'ai',             '#8b5cf6'),
(26, '机器学习',         'machine-learning','#6366f1'),
(27, 'Rust',           'rust',           '#f74c00'),
(28, '工程实践',         'engineering',    '#475569'),
(29, '搜索',            'search',         '#0891b2')
ON CONFLICT (slug) DO NOTHING;

