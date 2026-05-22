-- 企业码手机号注册未写入 system_type 时，库默认 1，IM 客户端登录使用 system_type=2 会查不到用户。
-- 将仅有 IM 账号、且 mobile 在 def_user 与 im_user 均存在、system_type=1 的记录修正为 2。
-- im 库名以实际环境为准（常见为 im / luohuo_im）；若无跨库权限可仅按 mobile 修正：
-- UPDATE luohuo_dev.def_user SET system_type = 2 WHERE system_type = 1 AND mobile IS NOT NULL AND mobile <> '';

UPDATE luohuo_dev.def_user u
INNER JOIN luohuo_im_01.im_user i ON i.user_id = u.id
SET u.system_type = 2
WHERE u.system_type = 1
  AND u.mobile IS NOT NULL
  AND u.mobile <> '';

-- 兜底：所有有手机号的 IM 注册账号
UPDATE luohuo_dev.def_user SET system_type = 2
WHERE system_type = 1 AND mobile IS NOT NULL AND mobile <> '';
