-- 审核功能迁移（已有数据库执行）
-- review_reason 不存在时才添加，已存在的 review_status 列可手动删除

ALTER TABLE articles ADD COLUMN IF NOT EXISTS review_reason TEXT;
-- ALTER TABLE articles DROP COLUMN IF EXISTS review_status;  -- 确认后执行
