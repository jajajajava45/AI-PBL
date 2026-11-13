package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pbl.selection.entity.MatchResult;
import com.pbl.selection.vo.MatchVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface MatchResultMapper extends BaseMapper<MatchResult> {

    /** 匹配结果（带学生/老师/实训室/轮次信息），支持动态过滤 */
    @Select("""
            <script>
            SELECT m.id, m.student_id AS studentId, s.student_no AS studentNo, u.real_name AS studentName,
                   s.major, s.class_name AS className,
                   m.teacher_id AS teacherId, tu.real_name AS teacherName, r.name AS roomName,
                   m.final_round_id AS finalRoundId, sr.round_no AS roundNo, sr.name AS roundName,
                   m.match_source AS matchSource, m.match_status AS matchStatus, m.created_at AS createdAt
            FROM match_result m
            JOIN student s ON s.id = m.student_id AND s.deleted = 0
            JOIN sys_user u ON u.id = s.user_id
            JOIN teacher t ON t.id = m.teacher_id AND t.deleted = 0
            JOIN sys_user tu ON tu.id = t.user_id
            JOIN training_room r ON r.id = t.training_room_id
            JOIN selection_round sr ON sr.id = m.final_round_id
            WHERE m.deleted = 0
            <if test="studentId != null"> AND m.student_id = #{studentId}</if>
            <if test="teacherId != null"> AND m.teacher_id = #{teacherId}</if>
            <if test="finalRoundId != null"> AND m.final_round_id = #{finalRoundId}</if>
            <if test="matchStatus != null"> AND m.match_status = #{matchStatus}</if>
            ORDER BY m.created_at, m.id
            </script>
            """)
    List<MatchVO> selectMatchVOs(@Param("studentId") Long studentId,
                                 @Param("teacherId") Long teacherId,
                                 @Param("finalRoundId") Long finalRoundId,
                                 @Param("matchStatus") Integer matchStatus);

    /** 老师已录取学生数 */
    @Select("""
            SELECT COUNT(*) FROM match_result
            WHERE teacher_id = #{teacherId} AND match_status = 1 AND deleted = 0
            """)
    int countMatched(@Param("teacherId") Long teacherId);

    /** 按匹配来源统计 */
    @Select("""
            SELECT m.match_source AS matchSource, COUNT(*) AS cnt
            FROM match_result m
            WHERE m.match_status = 1 AND m.deleted = 0
            GROUP BY m.match_source
            """)
    List<Map<String, Object>> countBySource();

    /** 各轮次统计 */
    @Select("""
            SELECT sr.id AS roundId, sr.round_no AS roundNo, sr.name AS roundName, sr.status AS roundStatus,
              (SELECT COUNT(DISTINCT sc.student_id) FROM student_choice sc
               WHERE sc.round_id = sr.id AND sc.deleted = 0) AS participantCount,
              (SELECT COUNT(*) FROM match_result m
               WHERE m.final_round_id = sr.id AND m.match_status = 1 AND m.deleted = 0) AS matchCount
            FROM selection_round sr
            WHERE sr.deleted = 0
            ORDER BY sr.round_no
            """)
    List<Map<String, Object>> statsByRound();

    /** 各实训室统计 */
    @Select("""
            SELECT r.id AS roomId, r.name AS roomName, r.code AS roomCode,
              COUNT(t.id) AS teacherCount,
              COALESCE(SUM(t.quota), 0) AS totalQuota,
              (SELECT COUNT(*) FROM match_result m JOIN teacher t2 ON t2.id = m.teacher_id
               WHERE t2.training_room_id = r.id AND m.match_status = 1 AND m.deleted = 0) AS matchedCount
            FROM training_room r
            LEFT JOIN teacher t ON t.training_room_id = r.id AND t.deleted = 0
            WHERE r.deleted = 0
            GROUP BY r.id, r.name, r.code
            ORDER BY r.id
            """)
    List<Map<String, Object>> statsByRoom();
}
