package com.luohuo.flex.im.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户注销审计
 */
@Data
@TableName("im_user_deletion_log")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDeletionLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("im_uid")
    private Long imUid;

    @TableField("def_user_id")
    private Long defUserId;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("account")
    private String account;

    @TableField("reason")
    private String reason;

    @TableField("operator_id")
    private Long operatorId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
