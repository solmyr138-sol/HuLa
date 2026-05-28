package com.luohuo.flex.im.core.tenant.service;

import com.luohuo.flex.im.api.vo.OfficialChannelResp;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 企业官方频道补齐/清理结果
 */
@Data
public class OfficialChannelCleanupResult implements Serializable {
    private Long tenantId;
    private OfficialChannelResp officialChannel;
    private int ensuredUsers;
    private int deletedDuplicateChannels;
    private List<Long> deletedRoomIds;
}

