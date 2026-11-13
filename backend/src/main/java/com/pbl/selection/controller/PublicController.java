package com.pbl.selection.controller;

import com.pbl.selection.common.Result;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.entity.TrainingRoom;
import com.pbl.selection.mapper.TrainingRoomMapper;
import com.pbl.selection.service.ConfigService;
import com.pbl.selection.service.FlowService;
import com.pbl.selection.vo.FlowVO;
import com.pbl.selection.vo.TeacherCardVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共接口：实训室 / 老师（含剩余名额）/ 流程总览
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final TrainingRoomMapper roomMapper;
    private final com.pbl.selection.mapper.TeacherMapper teacherMapper;
    private final FlowService flowService;
    private final ConfigService configService;

    /** 实训室列表（含启用老师数） */
    @GetMapping("/rooms")
    public Result<List<Map<String, Object>>> rooms() {
        List<TrainingRoom> rooms = roomMapper.selectList(Wrappers.<TrainingRoom>lambdaQuery()
                .orderByAsc(TrainingRoom::getId));
        List<Map<String, Object>> data = rooms.stream().map(r -> {
            long teacherCount = teacherMapper.selectCount(Wrappers.<Teacher>lambdaQuery()
                    .eq(Teacher::getTrainingRoomId, r.getId())
                    .eq(Teacher::getStatus, 1)
                    .eq(Teacher::getDeleted, 0));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("code", r.getCode() == null ? "" : r.getCode());
            m.put("description", r.getDescription() == null ? "" : r.getDescription());
            m.put("teacherCount", teacherCount);
            return m;
        }).toList();
        return Result.ok(data);
    }

    /** 老师列表（实时剩余名额；roomId 可选过滤；仅返回参与招生的老师） */
    @GetMapping("/teachers")
    public Result<List<TeacherCardVO>> teachers(@RequestParam(required = false) Long roomId) {
        Long currentRoundId = flowService.getCurrentRound() == null
                ? null : flowService.getCurrentRound().getId();
        return Result.ok(teacherMapper.selectTeacherCards(roomId, currentRoundId, true));
    }

    /** 流程总览（当前轮次 + 阶段） */
    @GetMapping("/flow")
    public Result<FlowVO.Overview> flow() {
        return Result.ok(flowService.overview());
    }

    /** 公开配置（每轮最多志愿数等，登录用户均可读取） */
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.ok(Map.of(
                "maxChoicesPerRound", configService.getInt(ConfigService.KEY_MAX_CHOICES, 3),
                "maxSupplementRounds", configService.getInt(ConfigService.KEY_MAX_SUPPLEMENT_ROUNDS, 2),
                "finalChoiceTimeoutMin", configService.getInt(ConfigService.KEY_FINAL_TIMEOUT_MIN, 60)));
    }
}
