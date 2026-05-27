-- 企业官方频道：def_tenant 记录 roomId / groupId（luohuo_dev）
USE luohuo_dev;

ALTER TABLE `def_tenant`
    ADD COLUMN `official_room_id` bigint NULL COMMENT '企业官方频道房间ID' AFTER `invite_code`,
    ADD COLUMN `official_group_id` bigint NULL COMMENT '企业官方频道群ID(im_room_group.id)' AFTER `official_room_id`;
