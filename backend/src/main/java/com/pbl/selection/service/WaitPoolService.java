package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.entity.WaitPool;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.WaitPoolMapper;
import com.pbl.selection.vo.WaitPoolVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待选池服务：最后一轮后未安置学生，学校约谈后人工安置
 */
@Service
@RequiredArgsConstructor
public class WaitPoolService {

    private final WaitPoolMapper waitPoolMapper;
    private final TeacherMapper teacherMapper;
    private final MatchService matchService;
    private final QuotaService quotaService;
    private final FlowService flowService;

    public List<WaitPoolVO> list(Integer status) {
        return waitPoolMapper.selectWaitPoolVOs(status);
    }

    public WaitPool getByStudent(Long studentId) {
        return waitPoolMapper.selectOne(Wrappers.<WaitPool>lambdaQuery()
                .eq(WaitPool::getStudentId, studentId).last("LIMIT 1"));
    }

    /** 标记已约谈 */
    @Transactional
    public void markInterview(Long studentId, LocalDateTime interviewAt) {
        WaitPool wp = requirePool(studentId);
        if (wp.getStatus() != null && wp.getStatus() == Enums.WaitPoolStatus.SETTLED.code) {
            throw BizException.conflict("该学生已安置，无需约谈");
        }
        wp.setInterviewAt(interviewAt);
        wp.setStatus(Enums.WaitPoolStatus.INTERVIEWED.code);
        waitPoolMapper.updateById(wp);
    }

    /** 人工安置（US-39）→ match_source=4 */
    @Transactional
    public void settle(Long studentId, Long teacherId, String remark, Long adminUserId) {
        WaitPool wp = requirePool(studentId);
        if (wp.getStatus() != null && wp.getStatus() == Enums.WaitPoolStatus.SETTLED.code) {
            throw BizException.conflict("该学生已安置，请勿重复操作");
        }
        if (matchService.isMatched(studentId)) {
            throw BizException.conflict("该学生已被录取");
        }
        Teacher t = teacherMapper.selectById(teacherId);
        if (t == null || t.getDeleted() == 1 || t.getStatus() == null || t.getStatus() != 1) {
            throw BizException.conflict("老师不存在或未参与招生");
        }
        Long currentRoundId = flowService.getCurrentRound() == null
                ? null : flowService.getCurrentRound().getId();
        quotaService.checkRemainingOrThrow(t, currentRoundId);

        matchService.createMatch(studentId, teacherId, wp.getEnterRound(), Enums.MatchSource.MANUAL_SETTLE.code);

        wp.setStatus(Enums.WaitPoolStatus.SETTLED.code);
        wp.setSettleTeacherId(teacherId);
        wp.setSettleBy(adminUserId);
        wp.setSettleRemark(remark);
        wp.setSettleAt(LocalDateTime.now());
        waitPoolMapper.updateById(wp);
    }

    private WaitPool requirePool(Long studentId) {
        WaitPool wp = getByStudent(studentId);
        if (wp == null) {
            throw BizException.notFound("该学生不在待选池中");
        }
        return wp;
    }
}
