-- 租户注册人数上限（容量），平台总后台创建/编辑企业时使用
-- Run against luohuo_dev

USE luohuo_dev;

ALTER TABLE `def_tenant`
    ADD COLUMN `account_limit` int NULL DEFAULT 500 COMMENT '注册人数上限（容量）' AFTER `account_count`;

UPDATE `def_tenant` SET `account_limit` = 500 WHERE `account_limit` IS NULL;
