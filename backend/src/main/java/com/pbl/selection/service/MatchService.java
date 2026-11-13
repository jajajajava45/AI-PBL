package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.entity.MatchResult;
import com.pbl.selection.mapper.MatchResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 匹配结果服务：统一写入 match_result
 */
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchResultMapper matchResultMapper;

    public boolean isMatched(Long studentId) {
        return getMatched(studentId) != null;
    }

    public MatchResult getMatched(Long studentId) {
        return matchResultMapper.selectOne(Wrappers.<MatchResult>lambdaQuery()
                .eq(MatchResult::getStudentId, studentId)
                .eq(MatchResult::getMatchStatus, 1)
                .last("LIMIT 1"));
    }

    /**
     * 写入匹配结果（一人一条，重复录取会抛冲突）
     */
    @Transactional
    public MatchResult createMatch(Long studentId, Long teacherId, Long roundId, int matchSource) {
        if (isMatched(studentId)) {
            throw BizException.conflict("该学生已被录取，请勿重复操作");
        }
        MatchResult m = new MatchResult();
        m.setStudentId(studentId);
        m.setTeacherId(teacherId);
        m.setFinalRoundId(roundId);
        m.setMatchSource(matchSource);
        m.setMatchStatus(1);
        m.setDeleted(0);
        matchResultMapper.insert(m);
        return m;
    }
}
