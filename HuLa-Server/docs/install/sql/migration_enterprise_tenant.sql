-- Enterprise invite code (one code per tenant), tenant/group policies, member ACL
-- Run against luohuo_dev (def_tenant) and luohuo_im_01 (IM tables)

-- ========== luohuo_dev ==========
USE luohuo_dev;

ALTER TABLE `def_tenant`
    ADD COLUMN `invite_code` varchar(32) NULL COMMENT '企业邀请码(一码一租户)' AFTER `name`,
    ADD COLUMN `admin_domain` varchar(256) NULL DEFAULT '' COMMENT '企业管理后台绑定域名' AFTER `website`;

CREATE UNIQUE INDEX `uk_def_tenant_invite_code` ON `def_tenant` (`invite_code`);

UPDATE `def_tenant` SET `invite_code` = 'DEFAULT' WHERE `id` = 1 AND (`invite_code` IS NULL OR `invite_code` = '');

-- ========== luohuo_im_01 ==========
USE luohuo_im_01;

DROP TABLE IF EXISTS `im_tenant_policy`;
CREATE TABLE `im_tenant_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `allow_cross_tenant_friend` tinyint(1) NOT NULL DEFAULT 0 COMMENT '允许跨租户加好友',
  `allow_cross_tenant_group_invite` tinyint(1) NOT NULL DEFAULT 0 COMMENT '允许跨租户群邀请',
  `forbid_create_group` tinyint(1) NOT NULL DEFAULT 0 COMMENT '禁止成员创建群',
  `forbid_broadcast` tinyint(1) NOT NULL DEFAULT 0 COMMENT '禁止群发(多选转发/@所有人/建群通知)',
  `forbid_member_add_friend` tinyint(1) NOT NULL DEFAULT 0 COMMENT '禁止成员互加好友',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `create_by` bigint NOT NULL DEFAULT 0,
  `update_by` bigint NULL DEFAULT NULL,
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户IM策略';

INSERT INTO `im_tenant_policy` (`tenant_id`, `create_by`) VALUES (1, 0);

DROP TABLE IF EXISTS `im_group_policy`;
CREATE TABLE `im_group_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL COMMENT '群房间ID',
  `join_mode` tinyint NOT NULL DEFAULT 3 COMMENT '1仅管理员邀请 2成员可邀请 3不限制',
  `history_visible_to_new` tinyint(1) NOT NULL DEFAULT 1 COMMENT '新成员可见历史消息',
  `group_mute_all` tinyint(1) NOT NULL DEFAULT 0 COMMENT '全员禁言',
  `allow_member_add_friend` tinyint(1) NOT NULL DEFAULT 1 COMMENT '允许群内加好友',
  `allow_member_dm` tinyint(1) NOT NULL DEFAULT 1 COMMENT '允许群内私聊',
  `allow_member_change_nickname` tinyint(1) NOT NULL DEFAULT 1 COMMENT '允许改群昵称',
  `speak_interval_sec` int NOT NULL DEFAULT 0 COMMENT '发言间隔秒,0不限',
  `tenant_id` bigint NOT NULL DEFAULT 1,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `create_by` bigint NOT NULL DEFAULT 0,
  `update_by` bigint NULL DEFAULT NULL,
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群策略';

DROP TABLE IF EXISTS `im_group_member_acl`;
CREATE TABLE `im_group_member_acl` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '群ID(room_id)',
  `uid` bigint NOT NULL COMMENT '成员uid',
  `can_edit_any_message` tinyint(1) NOT NULL DEFAULT 0,
  `can_recall_any_message` tinyint(1) NOT NULL DEFAULT 0,
  `muted_until` datetime(3) NULL DEFAULT NULL COMMENT '禁言截止时间',
  `tenant_id` bigint NOT NULL DEFAULT 1,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `create_by` bigint NOT NULL DEFAULT 0,
  `update_by` bigint NULL DEFAULT NULL,
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_uid` (`group_id`, `uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员扩展权限';

DROP TABLE IF EXISTS `im_user_deletion_log`;
CREATE TABLE `im_user_deletion_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `im_uid` bigint NOT NULL,
  `def_user_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `account` varchar(64) NOT NULL DEFAULT '',
  `reason` varchar(512) NULL DEFAULT NULL,
  `operator_id` bigint NULL DEFAULT NULL COMMENT '后台操作人',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户注销审计';

ALTER TABLE `im_message`
    ADD COLUMN `edited_at` datetime(3) NULL DEFAULT NULL COMMENT '最后编辑时间' AFTER `update_time`,
    ADD COLUMN `edit_version` int NOT NULL DEFAULT 0 COMMENT '编辑版本' AFTER `edited_at`;
