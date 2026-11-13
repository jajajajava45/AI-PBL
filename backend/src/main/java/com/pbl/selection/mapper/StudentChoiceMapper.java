package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pbl.selection.entity.StudentChoice;
import com.pbl.selection.vo.ChoiceVO;
import com.pbl.selection.vo.DiversionCandidateVO;
import com.pbl.selection.vo.PickVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentChoiceMapper extends BaseMapper<StudentChoice> {

    /** 学生志愿（带老师信息） */
    @Select("""
            <script>
            SELECT sc.id, sc.round_id AS roundId, sc.student_id AS studentId, sc.teacher_id AS teacherId,
                   sc.choice_rank AS choiceRank, tu.real_name AS teacherName, t.title,
                   t.research_direction AS researchDirection, r.name AS roomName
            FROM student_choice sc
            JOIN teacher t ON t.id = sc.teacher_id AND t.deleted = 0
            JOIN sys_user tu ON tu.id = t.user_id
            JOIN training_room r ON r.id = t.training_room_id
            WHERE sc.deleted = 0
            <if test="roundId != null"> AND sc.round_id = #{roundId}</if>
            <if test="studentId != null"> AND sc.student_id = #{studentId}</if>
            ORDER BY sc.choice_rank
            </script>
            """)
    List<ChoiceVO> selectChoiceVOs(@Param("roundId") Long roundId, @Param("studentId") Long studentId);

    /** 学生志愿老师所属的实训室 id 集合（系统统筹第一优先级范围） */
    @Select("""
            SELECT DISTINCT t.training_room_id
            FROM student_choice sc
            JOIN teacher t ON t.id = sc.teacher_id AND t.deleted = 0
            WHERE sc.student_id = #{studentId} AND sc.round_id = #{roundId} AND sc.deleted = 0
            """)
    List<Long> selectWishRoomIds(@Param("studentId") Long studentId, @Param("roundId") Long roundId);

    /**
     * 调剂候选学生：本轮已参与、未录取、不在待选池、服从调剂且范围包含指定实训室（或服从全校）
     */
    @Select("""
            SELECT DISTINCT s.id AS studentId, s.student_no AS studentNo, u.real_name AS studentName,
                   s.major, s.class_name AS className,
                   w.allow_internal AS allowInternal, w.allow_cross_room AS allowCrossRoom,
                   (SELECT AVG(i.score) FROM interview i
                    WHERE i.student_id = s.id AND i.round_id = #{roundId}
                      AND i.deleted = 0 AND i.score IS NOT NULL) AS avgScore
            FROM student s
            JOIN sys_user u ON u.id = s.user_id AND u.deleted = 0
            JOIN adjustment_wish w ON w.student_id = s.id AND w.round_id = #{roundId} AND w.deleted = 0
            WHERE s.deleted = 0
              AND EXISTS (SELECT 1 FROM student_choice sc
                          WHERE sc.student_id = s.id AND sc.round_id = #{roundId} AND sc.deleted = 0)
              AND NOT EXISTS (SELECT 1 FROM match_result m
                              WHERE m.student_id = s.id AND m.match_status = 1 AND m.deleted = 0)
              AND NOT EXISTS (SELECT 1 FROM wait_pool wp WHERE wp.student_id = s.id AND wp.deleted = 0)
              AND (w.allow_cross_room = 1
                   OR (w.allow_internal = 1 AND EXISTS (
                         SELECT 1 FROM student_choice sc2
                         JOIN teacher t2 ON t2.id = sc2.teacher_id AND t2.deleted = 0
                         WHERE sc2.student_id = s.id AND sc2.round_id = #{roundId} AND sc2.deleted = 0
                           AND t2.training_room_id = #{roomId})))
            ORDER BY avgScore DESC, s.id
            """)
    List<DiversionCandidateVO> selectDiversionCandidates(@Param("roundId") Long roundId,
                                                         @Param("roomId") Long roomId);

    /** 系统统筹候选学生（全校范围，含志愿提交时间） */
    @Select("""
            SELECT DISTINCT s.id AS studentId, s.student_no AS studentNo, u.real_name AS studentName,
                   s.major, s.class_name AS className,
                   w.allow_internal AS allowInternal, w.allow_cross_room AS allowCrossRoom,
                   (SELECT AVG(i.score) FROM interview i
                    WHERE i.student_id = s.id AND i.round_id = #{roundId}
                      AND i.deleted = 0 AND i.score IS NOT NULL) AS avgScore,
                   (SELECT MIN(sc.created_at) FROM student_choice sc
                    WHERE sc.student_id = s.id AND sc.round_id = #{roundId} AND sc.deleted = 0) AS submitAt
            FROM student s
            JOIN sys_user u ON u.id = s.user_id AND u.deleted = 0
            JOIN adjustment_wish w ON w.student_id = s.id AND w.round_id = #{roundId} AND w.deleted = 0
            WHERE s.deleted = 0
              AND (w.allow_internal = 1 OR w.allow_cross_room = 1)
              AND EXISTS (SELECT 1 FROM student_choice sc
                          WHERE sc.student_id = s.id AND sc.round_id = #{roundId} AND sc.deleted = 0)
              AND NOT EXISTS (SELECT 1 FROM match_result m
                              WHERE m.student_id = s.id AND m.match_status = 1 AND m.deleted = 0)
              AND NOT EXISTS (SELECT 1 FROM wait_pool wp WHERE wp.student_id = s.id AND wp.deleted = 0)
            """)
    List<DiversionCandidateVO> selectSystemCandidates(@Param("roundId") Long roundId);

    /** 管理端分流候选总览（含已被安置的学生，用于查看当前状态） */
    @Select("""
            SELECT DISTINCT s.id AS studentId, s.student_no AS studentNo, u.real_name AS studentName,
                   s.major, s.class_name AS className,
                   w.allow_internal AS allowInternal, w.allow_cross_room AS allowCrossRoom,
                   (SELECT AVG(i.score) FROM interview i
                    WHERE i.student_id = s.id AND i.round_id = #{roundId}
                      AND i.deleted = 0 AND i.score IS NOT NULL) AS avgScore,
                   (SELECT MIN(sc.created_at) FROM student_choice sc
                    WHERE sc.student_id = s.id AND sc.round_id = #{roundId} AND sc.deleted = 0) AS submitAt,
                   CASE WHEN EXISTS (SELECT 1 FROM match_result m
                                     WHERE m.student_id = s.id AND m.match_status = 1 AND m.deleted = 0)
                        THEN 1 ELSE 0 END AS placed
            FROM student s
            JOIN sys_user u ON u.id = s.user_id AND u.deleted = 0
            JOIN adjustment_wish w ON w.student_id = s.id AND w.round_id = #{roundId} AND w.deleted = 0
            WHERE s.deleted = 0
              AND (w.allow_internal = 1 OR w.allow_cross_room = 1)
              AND EXISTS (SELECT 1 FROM student_choice sc
                          WHERE sc.student_id = s.id AND sc.round_id = #{roundId} AND sc.deleted = 0)
            ORDER BY placed, avgScore DESC, submitAt
            """)
    List<DiversionCandidateVO> selectAdminDiversionCandidates(@Param("roundId") Long roundId);

    /** 截至指定轮次参与过志愿填报的学生（未匹配学生需跨轮次追踪至待选池） */
    @Select("""
            SELECT DISTINCT sc.student_id FROM student_choice sc
            JOIN selection_round r ON r.id = sc.round_id
            WHERE r.round_no <= #{roundNo} AND sc.deleted = 0
            """)
    List<Long> participantStudentIdsUpToRound(@Param("roundNo") Integer roundNo);

    /** 物理删除某学生本轮志愿（覆盖提交前清理，避免唯一键冲突） */
    @Delete("DELETE FROM student_choice WHERE round_id = #{roundId} AND student_id = #{studentId}")
    int purgeChoices(@Param("roundId") Long roundId, @Param("studentId") Long studentId);

    /** 老师候选学生：本轮选我当志愿的学生（含我是否已选他） */
    @Select("""
            SELECT sc.student_id AS studentId, s.student_no AS studentNo, u.real_name AS studentName,
                   s.major, s.class_name AS className,
                   p.pick_source AS pickSource, p.created_at AS createdAt,
                   (SELECT AVG(i.score) FROM interview i
                    WHERE i.student_id = s.id AND i.round_id = #{roundId}
                      AND i.deleted = 0 AND i.score IS NOT NULL) AS avgScore
            FROM student_choice sc
            JOIN student s ON s.id = sc.student_id AND s.deleted = 0
            JOIN sys_user u ON u.id = s.user_id
            LEFT JOIN teacher_pick p ON p.student_id = sc.student_id AND p.round_id = sc.round_id
                   AND p.teacher_id = #{teacherId} AND p.deleted = 0 AND p.pick_source = 1
            WHERE sc.deleted = 0 AND sc.round_id = #{roundId} AND sc.teacher_id = #{teacherId}
            ORDER BY avgScore DESC, sc.student_id
            """)
    List<PickVO> selectTeacherCandidates(@Param("roundId") Long roundId,
                                         @Param("teacherId") Long teacherId);
}
