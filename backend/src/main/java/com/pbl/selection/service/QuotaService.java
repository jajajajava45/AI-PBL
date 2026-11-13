package com.pbl.selection.service;

import com.pbl.selection.common.BizException;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.mapper.MatchResultMapper;
import com.pbl.selection.mapper.TeacherPickMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 名额服务：已用名额 / 剩余名额计算与校验
 *
 * 已用名额 = 已录取学生数（全轮次）+ 本轮"暂选未落地"的学生数
 * （暂选未落地 = 该老师本轮选了、但该生尚未被任何老师录取的记录；
 *   一旦该生被录取（无论录取给谁），该暂选记录不再占用名额）
 */
@Service
@RequiredArgsConstructor
public class QuotaService {

    private final MatchResultMapper matchResultMapper;
    private final TeacherPickMapper teacherPickMapper;

    public int usedQuota(Long teacherId, Long currentRoundId) {
        int matched = matchResultMapper.countMatched(teacherId);
        int pendingPicks = teacherPickMapper.countUnmatchedPicks(teacherId, currentRoundId);
        return matched + pendingPicks;
    }

    public int remaining(Teacher teacher, Long currentRoundId) {
        return teacher.getQuota() - usedQuota(teacher.getId(), currentRoundId);
    }

    public void checkRemainingOrThrow(Teacher teacher, Long currentRoundId) {
        if (remaining(teacher, currentRoundId) <= 0) {
            throw BizException.conflict("老师【" + teacher.getTeacherNo() + "】名额已满");
        }
    }
}
