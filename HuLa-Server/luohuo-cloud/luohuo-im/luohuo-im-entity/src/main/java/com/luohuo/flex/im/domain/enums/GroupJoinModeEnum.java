package com.luohuo.flex.im.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 群加入模式
 */
@AllArgsConstructor
@Getter
public enum GroupJoinModeEnum {
    ADMIN_ONLY(1, "仅管理员邀请"),
    MEMBER_INVITE(2, "成员可邀请"),
    OPEN(3, "不限制"),
    ;

    private final Integer type;
    private final String desc;

    private static final Map<Integer, GroupJoinModeEnum> CACHE = Arrays.stream(values())
            .collect(Collectors.toMap(GroupJoinModeEnum::getType, Function.identity()));

    public static GroupJoinModeEnum of(Integer type) {
        return CACHE.get(type);
    }
}
