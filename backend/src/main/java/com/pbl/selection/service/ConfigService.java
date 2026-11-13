package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.entity.SystemConfig;
import com.pbl.selection.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统配置服务（system_config 表）
 */
@Service
@RequiredArgsConstructor
public class ConfigService {

    public static final String KEY_MAX_SUPPLEMENT_ROUNDS = "max_supplement_rounds";
    public static final String KEY_MAX_CHOICES = "max_choices_per_round";
    public static final String KEY_TEACHER_DEFAULT_QUOTA = "teacher_default_quota";
    public static final String KEY_FINAL_TIMEOUT_MIN = "final_choice_timeout_min";
    public static final String KEY_CHOICE_MODIFY_ENABLED = "choice_modify_enabled";

    private final SystemConfigMapper configMapper;

    public String get(String key) {
        SystemConfig c = configMapper.selectOne(Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getConfigKey, key).last("LIMIT 1"));
        return c == null ? null : c.getConfigValue();
    }

    public String get(String key, String defaultValue) {
        String v = get(key);
        return v == null ? defaultValue : v;
    }

    public int getInt(String key, int defaultValue) {
        try {
            String v = get(key);
            return v == null ? defaultValue : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBool(String key, boolean defaultValue) {
        String v = get(key);
        return v == null ? defaultValue : Boolean.parseBoolean(v.trim());
    }

    public List<SystemConfig> listAll() {
        return configMapper.selectList(Wrappers.<SystemConfig>lambdaQuery().orderByAsc(SystemConfig::getId));
    }

    public void update(String key, String value) {
        SystemConfig c = configMapper.selectOne(Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getConfigKey, key).last("LIMIT 1"));
        if (c == null) {
            c = new SystemConfig();
            c.setConfigKey(key);
            c.setConfigValue(value);
            configMapper.insert(c);
        } else {
            c.setConfigValue(value);
            configMapper.updateById(c);
        }
    }
}
