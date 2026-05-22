-- Tenant policy whitelist (exempt users from global restrictions)
USE luohuo_im_01;

DROP TABLE IF EXISTS `im_tenant_policy_whitelist`;
CREATE TABLE `im_tenant_policy_whitelist` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `im_uid` bigint NOT NULL COMMENT 'IM用户uid',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `create_by` bigint NOT NULL DEFAULT 0,
  `update_by` bigint NULL DEFAULT NULL,
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_uid` (`tenant_id`, `im_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户策略白名单';
