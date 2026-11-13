package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.MatchResult;
import com.pbl.selection.entity.WaitPool;
import com.pbl.selection.mapper.MatchResultMapper;
import com.pbl.selection.mapper.StudentMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.WaitPoolMapper;
import com.pbl.selection.vo.FlowVO;
import com.pbl.selection.vo.MatchVO;
import com.pbl.selection.vo.TeacherCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务（US-40/41）
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudentMapper studentMapper;
    private final MatchResultMapper matchResultMapper;
    private final WaitPoolMapper waitPoolMapper;
    private final TeacherMapper teacherMapper;
    private final FlowService flowService;
    private final QuotaService quotaService;

    /** 总览统计 */
    public Map<String, Object> summary() {
        Map<String, Object> s = new LinkedHashMap<>();
        long totalStudents = studentMapper.selectCount(Wrappers.emptyWrapper());
        long matched = matchResultMapper.selectCount(Wrappers.<MatchResult>lambdaQuery()
                .eq(MatchResult::getMatchStatus, Enums.MatchStatus.MATCHED.code));
        s.put("totalStudents", totalStudents);
        s.put("matchedCount", matched);
        s.put("unmatchedCount", totalStudents - matched);

        Map<Integer, Long> bySource = new LinkedHashMap<>();
        for (Enums.MatchSource ms : Enums.MatchSource.values()) {
            bySource.put(ms.code, 0L);
        }
        for (Map<String, Object> row : matchResultMapper.countBySource()) {
            Integer code = ((Number) row.get("matchSource")).intValue();
            bySource.put(code, ((Number) row.get("cnt")).longValue());
        }
        Map<String, Long> bySourceText = new LinkedHashMap<>();
        bySource.forEach((code, cnt) -> {
            Enums.MatchSource ms = Enums.MatchSource.of(code);
            bySourceText.put(ms == null ? String.valueOf(code) : ms.text, cnt);
        });
        s.put("matchedBySource", bySourceText);

        s.put("waitPoolTotal", waitPoolMapper.selectCount(Wrappers.emptyWrapper()));
        s.put("waitPoolPending", waitPoolMapper.selectCount(Wrappers.<WaitPool>lambdaQuery()
                .ne(WaitPool::getStatus, Enums.WaitPoolStatus.SETTLED.code)));

        FlowVO.Overview flow = flowService.overview();
        s.put("currentRound", flow.getCurrentRound());
        s.put("currentStage", flow.getCurrentStage());
        return s;
    }

    /** 各实训室统计 */
    public List<Map<String, Object>> byRoom() {
        return matchResultMapper.statsByRoom();
    }

    /** 各轮次统计 */
    public List<Map<String, Object>> byRound() {
        return matchResultMapper.statsByRound();
    }

    /** 各老师名额使用情况 */
    public List<Map<String, Object>> byTeacher() {
        Long currentRoundId = flowService.getCurrentRound() == null
                ? null : flowService.getCurrentRound().getId();
        List<TeacherCardVO> cards = teacherMapper.selectTeacherCards(null, currentRoundId, false);
        return cards.stream().map(c -> {
            int matched = matchResultMapper.countMatched(c.getId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("teacherId", c.getId());
            m.put("teacherNo", c.getTeacherNo());
            m.put("teacherName", c.getRealName());
            m.put("roomName", c.getRoomName());
            m.put("quota", c.getQuota());
            m.put("matched", matched);
            m.put("pendingPicks", Math.max(0, (c.getQuota() - c.getRemaining()) - matched));
            m.put("remaining", c.getRemaining());
            return m;
        }).toList();
    }

    /** 匹配结果 CSV 导出（含 BOM，Excel 直接打开不乱码） */
    public String exportMatchCsv() {
        List<MatchVO> rows = matchResultMapper.selectMatchVOs(null, null, null, Enums.MatchStatus.MATCHED.code);
        StringBuilder sb = new StringBuilder();
        sb.append("﻿");
        sb.append("学号,姓名,专业,班级,录取老师,所属实训室,录取来源,确定轮次,录取时间\n");
        for (MatchVO r : rows) {
            Enums.MatchSource ms = Enums.MatchSource.of(r.getMatchSource());
            String source = ms == null ? String.valueOf(r.getMatchSource()) : ms.text;
            sb.append(csv(r.getStudentNo())).append(',')
                    .append(csv(r.getStudentName())).append(',')
                    .append(csv(r.getMajor())).append(',')
                    .append(csv(r.getClassName())).append(',')
                    .append(csv(r.getTeacherName())).append(',')
                    .append(csv(r.getRoomName())).append(',')
                    .append(csv(source)).append(',')
                    .append(csv(r.getRoundName())).append(',')
                    .append(csv(r.getCreatedAt() == null ? "" : r.getCreatedAt().toString()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String csv(String v) {
        if (v == null) {
            return "";
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
