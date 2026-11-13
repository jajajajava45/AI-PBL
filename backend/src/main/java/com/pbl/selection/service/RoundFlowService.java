package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.FlowStage;
import com.pbl.selection.entity.SelectionRound;
import com.pbl.selection.mapper.FlowStageMapper;
import com.pbl.selection.mapper.SelectionRoundMapper;
import com.pbl.selection.vo.StageAdvanceResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程推进服务：启动轮次 / 阶段推进（含自动处理钩子）
 *
 * 阶段顺序：CHOICE_OPEN → CHOICE_CLOSED → INTERVIEW → TEACHER_PICK
 *           → STUDENT_FINAL → DIVERSION → COMPLETED
 */
@Service
@RequiredArgsConstructor
public class RoundFlowService {

    private final SelectionRoundMapper roundMapper;
    private final FlowStageMapper stageMapper;
    private final FlowService flowService;
    private final FinalChoiceService finalChoiceService;
    private final DiversionService diversionService;

    /**
     * 启动轮次：创建阶段记录、开放志愿填报
     */
    @Transactional
    public void startRound(Long roundId) {
        SelectionRound round = roundMapper.selectById(roundId);
        if (round == null) {
            throw BizException.notFound("轮次不存在");
        }
        if (round.getStatus() == null || round.getStatus() != Enums.RoundStatus.NOT_STARTED.code) {
            throw BizException.conflict("该轮次已开始或已结束");
        }
        // 前序轮次必须全部结束
        List<SelectionRound> prev = roundMapper.selectList(Wrappers.<SelectionRound>lambdaQuery()
                .lt(SelectionRound::getRoundNo, round.getRoundNo()));
        for (SelectionRound r : prev) {
            if (r.getStatus() == null || r.getStatus() != Enums.RoundStatus.FINISHED.code) {
                throw BizException.conflict("第 " + r.getRoundNo() + " 轮尚未结束，不能启动第 " + round.getRoundNo() + " 轮");
            }
        }

        // 阶段记录：不存在则创建；存在（重放场景）则重置状态
        List<FlowStage> stages = stageMapper.selectList(Wrappers.<FlowStage>lambdaQuery()
                .eq(FlowStage::getRoundId, roundId));
        if (stages.isEmpty()) {
            for (Enums.StageName sn : Enums.StageName.values()) {
                FlowStage fs = new FlowStage();
                fs.setRoundId(roundId);
                fs.setStageName(sn.name());
                fs.setStageOrder(sn.order);
                fs.setStatus(Enums.StageStatus.NOT_STARTED.code);
                fs.setDeleted(0);
                stageMapper.insert(fs);
            }
        } else {
            for (FlowStage fs : stages) {
                fs.setStatus(Enums.StageStatus.NOT_STARTED.code);
                fs.setStartAt(null);
                fs.setEndAt(null);
                stageMapper.updateById(fs);
            }
        }

        // 开放志愿填报
        FlowStage first = flowService.getStage(roundId, Enums.StageName.CHOICE_OPEN);
        first.setStatus(Enums.StageStatus.IN_PROGRESS.code);
        first.setStartAt(LocalDateTime.now());
        stageMapper.updateById(first);

        round.setStatus(Enums.RoundStatus.IN_PROGRESS.code);
        round.setChoiceStartAt(LocalDateTime.now());
        roundMapper.updateById(round);
    }

    /**
     * 进入下一阶段（自动处理钩子）：
     *  - 进入 STUDENT_FINAL：=1 位选中的学生直接匹配
     *  - 离开 STUDENT_FINAL：≥2 位选中未终选 → 超时自动分配
     *  - 离开 DIVERSION：未安置学生流转下一轮 / 最后一轮进待选池，轮次结束
     */
    @Transactional
    public StageAdvanceResultVO advanceStage(Long roundId) {
        SelectionRound round = roundMapper.selectById(roundId);
        if (round == null) {
            throw BizException.notFound("轮次不存在");
        }
        if (round.getStatus() == null || round.getStatus() != Enums.RoundStatus.IN_PROGRESS.code) {
            throw BizException.conflict("该轮次未在进行中");
        }
        FlowStage current = flowService.getCurrentStage(roundId);
        if (current == null) {
            throw BizException.conflict("该轮次没有进行中的阶段");
        }
        Enums.StageName currentName = Enums.StageName.of(current.getStageName());
        if (currentName == null || currentName == Enums.StageName.COMPLETED) {
            throw BizException.conflict("本轮已全部完成");
        }
        Enums.StageName next = findNext(currentName);

        StageAdvanceResultVO vo = new StageAdvanceResultVO();
        vo.setFromStage(currentName.name());
        vo.setToStage(next.name());

        // 离开阶段钩子
        if (currentName == Enums.StageName.STUDENT_FINAL) {
            var r = finalChoiceService.resolveFinal(roundId, true);
            vo.setAutoMatched(r.autoMatched());
            vo.setAutoTimeout(r.autoTimeout());
        }
        if (currentName == Enums.StageName.DIVERSION) {
            var r = diversionService.finalizeRound(round);
            vo.setToWaitPool(r.get("toWaitPool"));
            vo.setToNextRound(r.get("toNextRound"));
        }

        // 关闭当前阶段
        current.setStatus(Enums.StageStatus.FINISHED.code);
        current.setEndAt(LocalDateTime.now());
        stageMapper.updateById(current);

        // 开启下一阶段
        FlowStage nextStage = flowService.getStage(roundId, next);
        nextStage.setStatus(Enums.StageStatus.IN_PROGRESS.code);
        nextStage.setStartAt(LocalDateTime.now());
        stageMapper.updateById(nextStage);

        // 进入阶段钩子
        if (next == Enums.StageName.STUDENT_FINAL) {
            var r = finalChoiceService.resolveFinal(roundId, false);
            vo.setAutoMatched(vo.getAutoMatched() + r.autoMatched());
            vo.setAutoTimeout(vo.getAutoTimeout() + r.autoTimeout());
        }
        return vo;
    }

    private Enums.StageName findNext(Enums.StageName cur) {
        for (Enums.StageName s : Enums.StageName.values()) {
            if (s.order == cur.order + 1) {
                return s;
            }
        }
        throw BizException.conflict("没有下一阶段");
    }
}
