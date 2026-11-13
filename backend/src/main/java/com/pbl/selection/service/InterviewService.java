package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.dto.InterviewScheduleRequest;
import com.pbl.selection.dto.InterviewScoreRequest;
import com.pbl.selection.entity.FlowStage;
import com.pbl.selection.entity.Interview;
import com.pbl.selection.entity.SelectionRound;
import com.pbl.selection.entity.StudentChoice;
import com.pbl.selection.mapper.InterviewMapper;
import com.pbl.selection.mapper.StudentChoiceMapper;
import com.pbl.selection.vo.InterviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 面试服务：安排面试 / 录入成绩 / 查询
 */
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final FlowService flowService;
    private final InterviewMapper interviewMapper;
    private final StudentChoiceMapper studentChoiceMapper;

    /**
     * 安排面试（已存在记录则更新时间地点）
     */
    @Transactional
    public void schedule(Long teacherId, InterviewScheduleRequest req) {
        SelectionRound round = requireStageForInterview();

        boolean choseMe = studentChoiceMapper.selectCount(Wrappers.<StudentChoice>lambdaQuery()
                .eq(StudentChoice::getRoundId, round.getId())
                .eq(StudentChoice::getStudentId, req.getStudentId())
                .eq(StudentChoice::getTeacherId, teacherId)) > 0;
        if (!choseMe) {
            throw BizException.conflict("该学生本轮未选择您为志愿老师");
        }

        Interview existing = interviewMapper.selectOne(Wrappers.<Interview>lambdaQuery()
                .eq(Interview::getRoundId, round.getId())
                .eq(Interview::getStudentId, req.getStudentId())
                .eq(Interview::getTeacherId, teacherId).last("LIMIT 1"));
        if (existing == null) {
            existing = new Interview();
            existing.setRoundId(round.getId());
            existing.setStudentId(req.getStudentId());
            existing.setTeacherId(teacherId);
            existing.setStatus(Enums.InterviewStatus.SCHEDULED.code);
            existing.setDeleted(0);
            existing.setInterviewTime(req.getInterviewTime());
            existing.setLocation(req.getLocation());
            interviewMapper.insert(existing);
        } else {
            if (existing.getStatus() == Enums.InterviewStatus.COMPLETED.code) {
                throw BizException.conflict("该面试已完成，不能修改");
            }
            existing.setStatus(Enums.InterviewStatus.SCHEDULED.code);
            existing.setInterviewTime(req.getInterviewTime());
            existing.setLocation(req.getLocation());
            interviewMapper.updateById(existing);
        }
    }

    /**
     * 录入面试成绩 / 评价
     */
    @Transactional
    public void recordScore(Long teacherId, Long interviewId, InterviewScoreRequest req) {
        Interview it = interviewMapper.selectById(interviewId);
        if (it == null) {
            throw BizException.notFound("面试记录不存在");
        }
        if (!it.getTeacherId().equals(teacherId)) {
            throw BizException.forbidden("只能操作自己的面试记录");
        }
        if (req.getScore() != null) {
            if (req.getScore().doubleValue() < 0 || req.getScore().doubleValue() > 100) {
                throw BizException.conflict("面试成绩应在 0~100 之间");
            }
            it.setScore(req.getScore());
        }
        if (req.getFeedback() != null) {
            it.setFeedback(req.getFeedback());
        }
        if (req.getStatus() != null) {
            it.setStatus(req.getStatus());
        }
        interviewMapper.updateById(it);
    }

    public List<InterviewVO> listForTeacher(Long teacherId, Long roundId) {
        return interviewMapper.selectInterviewVOs(roundId, teacherId, null, null);
    }

    public List<InterviewVO> listForStudent(Long studentId, Long roundId) {
        return interviewMapper.selectInterviewVOs(roundId, null, studentId, null);
    }

    private SelectionRound requireStageForInterview() {
        // 允许在 面试 / 老师选学生 / 学生终选 阶段安排面试（宽容处理，面试可补排）
        SelectionRound round = flowService.getCurrentRound();
        if (round == null) {
            throw BizException.conflict("当前没有进行中的录取轮次");
        }
        FlowStage stage = flowService.getCurrentStage(round.getId());
        String name = stage == null ? "" : stage.getStageName();
        if (!Enums.StageName.INTERVIEW.name().equals(name)
                && !Enums.StageName.TEACHER_PICK.name().equals(name)
                && !Enums.StageName.STUDENT_FINAL.name().equals(name)) {
            throw BizException.conflict("当前阶段不可安排面试");
        }
        return round;
    }
}
