package com.soundpro.sounds.utils;

public abstract class ConsumerUtils {
    
    public JSONUtilitiesV2 jsonMapper() {
        return JSONUtilitiesV2
                .create()
                .withParseDate(JSONUtilitiesV2.BuildType.LAZY)
                .withParseLocalDate(JSONUtilitiesV2.BuildType.LAZY)
                .withParseLocalDateTime(JSONUtilitiesV2.BuildType.LAZY)
                .withParseByteArray(JSONUtilitiesV2.BuildType.LAZY)
                .builder();
    }

    public JSONUtilitiesV2 jsonMapperWithEnum() {
        return JSONUtilitiesV2
                .create()
                .withParseDate(JSONUtilitiesV2.BuildType.LAZY)
                .withParseLocalDate(JSONUtilitiesV2.BuildType.LAZY)
                .withParseLocalDateTime(JSONUtilitiesV2.BuildType.LAZY)
                .withParseEnum(JSONUtilitiesV2.BuildType.LAZY)
                .withParseByteArray(JSONUtilitiesV2.BuildType.LAZY)
                .builder();
    }
}
