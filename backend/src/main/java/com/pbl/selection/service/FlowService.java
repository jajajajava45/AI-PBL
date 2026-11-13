package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.FlowStage;
import com.pbl.selection.entity.SelectionRound;
import com.pbl.selection.mapper.FlowStageMapper;
import com.pbl.selection.mapper.SelectionRoundMapper;
import com.pbl.selection.vo.FlowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程上下文服务：当前轮次 / 当前阶段 / 流程总览
 */
@Service
@RequiredArgsConstructor
public class FlowService {

    private final SelectionRoundMapper roundMapper;
    private final FlowStageMapper stageMapper;

    /** 当前进行中的轮次（selection_round.status=1），无则返回 null */
    public SelectionRound getCurrentRound() {
        return roundMapper.selectOne(Wrappers.<SelectionRound>lambdaQuery()
                .eq(SelectionRound::getStatus, Enums.RoundStatus.IN_PROGRESS.code)
                .orderByAsc(SelectionRound::getRoundNo)
                .last("LIMIT 1"));
    }

    /** 某轮当前进行中的阶段，无则返回 null */
    public FlowStage getCurrentStage(Long roundId) {
        return stageMapper.selectOne(Wrappers.<FlowStage>lambdaQuery()
                .eq(FlowStage::getRoundId, roundId)
                .eq(FlowStage::getStatus, Enums.StageStatus.IN_PROGRESS.code)
                .last("LIMIT 1"));
    }

    public FlowStage getStage(Long roundId, Enums.StageName stageName) {
        return stageMapper.selectOne(Wrappers.<FlowStage>lambdaQuery()
                .eq(FlowStage::getRoundId, roundId)
                .eq(FlowStage::getStageName, stageName.name())
                .last("LIMIT 1"));
    }

    /** 要求当前处于进行中轮次的指定阶段，否则抛冲突 */
    public SelectionRound requireCurrentStage(Enums.StageName stageName) {
        SelectionRound round = getCurrentRound();
        if (round == null) {
            throw BizException.conflict("当前没有进行中的录取轮次");
        }
        FlowStage stage = getCurrentStage(round.getId());
        if (stage == null || !stageName.name().equals(stage.getStageName())) {
            String now = stage == null ? "无进行中阶段" : stage.getStageName();
            throw BizException.conflict("当前阶段为 " + now + "，此操作需在 " + stageName + "（" + stageName.text + "）阶段进行");
        }
        return round;
    }

    /** 流程总览（管理端 + 学生/老师公共视图共用） */
    public FlowVO.Overview overview() {
        List<FlowVO.Round> rounds = roundMapper.selectList(Wrappers.<SelectionRound>lambdaQuery()
                        .orderByAsc(SelectionRound::getRoundNo)).stream()
                .map(r -> {
                    FlowVO.Round vo = new FlowVO.Round();
                    vo.setId(r.getId());
                    vo.setRoundNo(r.getRoundNo());
                    vo.setRoundType(r.getRoundType());
                    vo.setName(r.getName());
                    vo.setRoundStatus(r.getStatus());
                    vo.setRoundStatusText(Enums.RoundStatus.IN_PROGRESS.code == r.getStatus()
                            ? "进行中" : (r.getStatus() == 2 ? "已结束" : "未开始"));
                    vo.setStages(stageMapper.selectList(Wrappers.<FlowStage>lambdaQuery()
                                    .eq(FlowStage::getRoundId, r.getId())
                                    .orderByAsc(FlowStage::getStageOrder)).stream()
                            .map(this::toStageVO).toList());
                    return vo;
                }).toList();

        SelectionRound currentRound = getCurrentRound();
        FlowVO.Overview overview = new FlowVO.Overview();
        overview.setRounds(rounds);
        if (currentRound != null) {
            FlowVO.Round cur = rounds.stream().filter(r -> r.getId().equals(currentRound.getId()))
                    .findFirst().orElse(null);
            overview.setCurrentRound(cur);
            FlowStage curStage = getCurrentStage(currentRound.getId());
            if (curStage != null) {
                overview.setCurrentStage(toStageVO(curStage));
            }
        }
        return overview;
    }

    private FlowVO.Stage toStageVO(FlowStage s) {
        FlowVO.Stage vo = new FlowVO.Stage();
        vo.setId(s.getId());
        vo.setStageName(s.getStageName());
        Enums.StageName name = Enums.StageName.of(s.getStageName());
        vo.setStageText(name == null ? s.getStageName() : name.text);
        vo.setStageOrder(s.getStageOrder());
        vo.setStageStatus(s.getStatus());
        vo.setStageStatusText(s.getStatus() == 1 ? "进行中" : (s.getStatus() == 2 ? "已结束" : "未开始"));
        vo.setStartAt(s.getStartAt());
        vo.setEndAt(s.getEndAt());
        vo.setRemark(s.getRemark());
        return vo;
    }
}
