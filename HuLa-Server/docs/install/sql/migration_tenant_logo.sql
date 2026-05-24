-- 企业 LOGO + 默认租户名称
USE luohuo_dev;

ALTER TABLE `def_tenant`
    ADD COLUMN `logo` varchar(512) NULL DEFAULT NULL COMMENT '企业LOGO URL' AFTER `name`;

UPDATE `def_tenant`
SET `name` = '淘东美科技有限公司'
WHERE `id` = 1;
