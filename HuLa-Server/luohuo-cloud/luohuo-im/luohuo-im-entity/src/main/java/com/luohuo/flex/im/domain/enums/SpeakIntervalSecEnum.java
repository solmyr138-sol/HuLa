package com.luohuo.flex.im.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 群发言间隔（秒）
 */
@AllArgsConstructor
@Getter
public enum SpeakIntervalSecEnum {
    UNLIMITED(0, "不限"),
    SEC_5(5, "5秒"),
    SEC_10(10, "10秒"),
    SEC_30(30, "30秒"),
    SEC_60(60, "1分钟"),
    SEC_300(300, "5分钟"),
    SEC_900(900, "15分钟"),
    SEC_1800(1800, "30分钟"),
    SEC_3600(3600, "1小时"),
    ;

    private final Integer seconds;
    private final String label;

    private static final Map<Integer, SpeakIntervalSecEnum> CACHE = Arrays.stream(values())
            .collect(Collectors.toMap(SpeakIntervalSecEnum::getSeconds, Function.identity()));

    public static SpeakIntervalSecEnum of(Integer seconds) {
        return CACHE.get(seconds);
    }

    public static boolean isValid(Integer seconds) {
        return seconds != null && CACHE.containsKey(seconds);
    }
}
