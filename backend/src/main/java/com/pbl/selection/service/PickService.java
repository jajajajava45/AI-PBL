package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.Student;
import com.pbl.selection.entity.StudentChoice;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.entity.TeacherPick;
import com.pbl.selection.entity.WaitPool;
import com.pbl.selection.mapper.StudentChoiceMapper;
import com.pbl.selection.mapper.StudentMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.TeacherPickMapper;
import com.pbl.selection.mapper.WaitPoolMapper;
import com.pbl.selection.vo.PickVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 老师选学生服务（阶段③ 暂选 + 分流阶段选人）
 */
@Service
@RequiredArgsConstructor
public class PickService {

    private final FlowService flowService;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final StudentChoiceMapper studentChoiceMapper;
    private final TeacherPickMapper teacherPickMapper;
    private final WaitPoolMapper waitPoolMapper;
    private final QuotaService quotaService;
    private final MatchService matchService;

    /** 老师候选学生：本轮选我当志愿的学生（含我是否已勾选） */
    public List<PickVO> candidates(Long teacherId, Long roundId) {
        return studentChoiceMapper.selectTeacherCandidates(roundId, teacherId);
    }

    /** 我本轮已选的学生 */
    public List<PickVO> myPicks(Long teacherId, Long roundId) {
        return teacherPickMapper.selectPickVOs(roundId, teacherId, null, null);
    }

    /**
     * 阶段③ 老师选学生（暂选，等待学生终选/阶段收尾自动匹配）
     */
    @Transactional
    public void pick(Long teacherId, Long studentId) {
        Long roundId = flowService.requireCurrentStage(Enums.StageName.TEACHER_PICK).getId();
        Teacher teacher = requireTeacher(teacherId);
        requirePickableStudent(studentId);

        boolean choseMe = studentChoiceMapper.selectCount(Wrappers.<StudentChoice>lambdaQuery()
                .eq(StudentChoice::getRoundId, roundId)
                .eq(StudentChoice::getStudentId, studentId)
                .eq(StudentChoice::getTeacherId, teacherId)) > 0;
        if (!choseMe) {
            throw BizException.conflict("该学生本轮未选择您为志愿老师，不能选择");
        }

        quotaService.checkRemainingOrThrow(teacher, roundId);

        Long dup = teacherPickMapper.selectCount(Wrappers.<TeacherPick>lambdaQuery()
                .eq(TeacherPick::getRoundId, roundId)
                .eq(TeacherPick::getTeacherId, teacherId)
                .eq(TeacherPick::getStudentId, studentId)
                .eq(TeacherPick::getPickSource, Enums.PickSource.CHOICE.code));
        if (dup != null && dup > 0) {
            throw BizException.conflict("已选择过该学生");
        }

        TeacherPick pick = new TeacherPick();
        pick.setRoundId(roundId);
        pick.setTeacherId(teacherId);
        pick.setStudentId(studentId);
        pick.setPickSource(Enums.PickSource.CHOICE.code);
        pick.setDeleted(0);
        teacherPickMapper.insert(pick);
    }

    /**
     * 撤销阶段③ 的暂选（学生已被录取后不可撤销）
     */
    @Transactional
    public void unpick(Long teacherId, Long studentId) {
        Long roundId = flowService.requireCurrentStage(Enums.StageName.TEACHER_PICK).getId();
        TeacherPick pick = teacherPickMapper.selectOne(Wrappers.<TeacherPick>lambdaQuery()
                .eq(TeacherPick::getRoundId, roundId)
                .eq(TeacherPick::getTeacherId, teacherId)
                .eq(TeacherPick::getStudentId, studentId)
                .eq(TeacherPick::getPickSource, Enums.PickSource.CHOICE.code)
                .last("LIMIT 1"));
        if (pick == null) {
            throw BizException.notFound("未找到该选择记录");
        }
        if (matchService.isMatched(studentId)) {
            throw BizException.conflict("该学生已被录取，不可撤销");
        }
        teacherPickMapper.deleteById(pick.getId());
    }

    /** 公共校验：老师有效 & 学生有效（未录取、不在待选池） */
    public Teacher requireTeacher(Long teacherId) {
        Teacher t = teacherMapper.selectById(teacherId);
        if (t == null || t.getDeleted() == 1 || t.getStatus() == null || t.getStatus() != 1) {
            throw BizException.conflict("老师不存在或未参与招生");
        }
        return t;
    }

    public void requirePickableStudent(Long studentId) {
        Student s = studentMapper.selectById(studentId);
        if (s == null || s.getDeleted() == 1) {
            throw BizException.notFound("学生不存在");
        }
        if (matchService.isMatched(studentId)) {
            throw BizException.conflict("该学生已被录取");
        }
        boolean inPool = waitPoolMapper.selectCount(Wrappers.<WaitPool>lambdaQuery()
                .eq(WaitPool::getStudentId, studentId)) > 0;
        if (inPool) {
            throw BizException.conflict("该学生已进入待选池");
        }
    }
}
