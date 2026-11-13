package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.dto.ChoiceSubmitRequest;
import com.pbl.selection.entity.AdjustmentWish;
import com.pbl.selection.entity.FlowStage;
import com.pbl.selection.entity.SelectionRound;
import com.pbl.selection.entity.StudentChoice;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.entity.TrainingRoom;
import com.pbl.selection.mapper.AdjustmentWishMapper;
import com.pbl.selection.mapper.StudentChoiceMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.TrainingRoomMapper;
import com.pbl.selection.vo.ChoiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/**
 * 学生志愿服务：填报 / 修改 / 查询（平行志愿，不分先后）
 */
@Service
@RequiredArgsConstructor
public class ChoiceService {

    private final FlowService flowService;
    private final StudentChoiceMapper choiceMapper;
    private final AdjustmentWishMapper wishMapper;
    private final TeacherMapper teacherMapper;
    private final TrainingRoomMapper roomMapper;
    private final MatchService matchService;
    private final ConfigService configService;

    /**
     * 提交/修改志愿（全量覆盖），同时保存调剂意愿
     */
    @Transactional
    public void submit(Long studentId, ChoiceSubmitRequest req) {
        SelectionRound round = flowService.requireCurrentStage(Enums.StageName.CHOICE_OPEN);

        if (matchService.isMatched(studentId)) {
            throw BizException.conflict("您已被录取，无需填报志愿");
        }

        List<Long> ids = req.getTeacherIds();
        int max = configService.getInt(ConfigService.KEY_MAX_CHOICES, 3);
        if (ids.size() > max) {
            throw BizException.conflict("每轮最多选择 " + max + " 名老师");
        }
        if (ids.size() != new HashSet<>(ids).size()) {
            throw BizException.conflict("志愿老师不能重复");
        }
        for (Long tid : ids) {
            Teacher t = teacherMapper.selectById(tid);
            if (t == null || t.getDeleted() == 1 || t.getStatus() == null || t.getStatus() != 1) {
                throw BizException.conflict("老师不存在或未参与招生: id=" + tid);
            }
            TrainingRoom room = roomMapper.selectById(t.getTrainingRoomId());
            if (room == null || room.getStatus() == null || room.getStatus() != 1) {
                throw BizException.conflict("该老师所属实训室已停用: id=" + tid);
            }
        }

        // 覆盖旧志愿（物理删除，避免与唯一键 uk_round_student_teacher 冲突）
        choiceMapper.purgeChoices(round.getId(), studentId);
        for (int i = 0; i < ids.size(); i++) {
            StudentChoice c = new StudentChoice();
            c.setRoundId(round.getId());
            c.setStudentId(studentId);
            c.setTeacherId(ids.get(i));
            c.setChoiceRank(i + 1);   // 仅展示用，业务上平行志愿
            c.setDeleted(0);
            choiceMapper.insert(c);
        }

        // 调剂意愿 upsert（可多选/单选/不选）
        AdjustmentWish wish = wishMapper.selectOne(Wrappers.<AdjustmentWish>lambdaQuery()
                .eq(AdjustmentWish::getRoundId, round.getId())
                .eq(AdjustmentWish::getStudentId, studentId).last("LIMIT 1"));
        int allowInternal = Boolean.TRUE.equals(req.getAllowInternal()) ? 1 : 0;
        int allowCross = Boolean.TRUE.equals(req.getAllowCrossRoom()) ? 1 : 0;
        if (wish == null) {
            wish = new AdjustmentWish();
            wish.setRoundId(round.getId());
            wish.setStudentId(studentId);
            wish.setAllowInternal(allowInternal);
            wish.setAllowCrossRoom(allowCross);
            wish.setDeleted(0);
            wishMapper.insert(wish);
        } else {
            wish.setAllowInternal(allowInternal);
            wish.setAllowCrossRoom(allowCross);
            wishMapper.updateById(wish);
        }
    }

    /** 本轮（或指定轮）我的志愿 */
    public List<ChoiceVO> myChoices(Long studentId, Long roundId) {
        return choiceMapper.selectChoiceVOs(roundId, studentId);
    }

    /** 我的调剂意愿 */
    public AdjustmentWish myWish(Long studentId, Long roundId) {
        return wishMapper.selectOne(Wrappers.<AdjustmentWish>lambdaQuery()
                .eq(AdjustmentWish::getRoundId, roundId)
                .eq(AdjustmentWish::getStudentId, studentId).last("LIMIT 1"));
    }

    /** 学生是否参与了某轮填报 */
    public boolean participated(Long studentId, Long roundId) {
        return choiceMapper.selectCount(Wrappers.<StudentChoice>lambdaQuery()
                .eq(StudentChoice::getRoundId, roundId)
                .eq(StudentChoice::getStudentId, studentId)) > 0;
    }

    /** 本轮参与填报的学生 id（去重） */
    public List<Long> participantStudentIds(Long roundId) {
        return choiceMapper.selectList(Wrappers.<StudentChoice>lambdaQuery()
                        .eq(StudentChoice::getRoundId, roundId)
                        .select(StudentChoice::getStudentId)).stream()
                .map(StudentChoice::getStudentId).distinct().toList();
    }
}
