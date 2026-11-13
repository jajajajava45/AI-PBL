package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pbl.selection.entity.TeacherPick;
import com.pbl.selection.vo.PickVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeacherPickMapper extends BaseMapper<TeacherPick> {

    /** 选人记录（带学生信息与本轮面试平均分） */
    @Select("""
            <script>
            SELECT p.id, p.round_id AS roundId, p.teacher_id AS teacherId, p.student_id AS studentId,
                   s.student_no AS studentNo, u.real_name AS studentName, s.major, s.class_name AS className,
                   p.pick_source AS pickSource, p.created_at AS createdAt,
                   (SELECT AVG(i.score) FROM interview i
                    WHERE i.student_id = s.id AND i.round_id = p.round_id
                      AND i.deleted = 0 AND i.score IS NOT NULL) AS avgScore
            FROM teacher_pick p
            JOIN student s ON s.id = p.student_id AND s.deleted = 0
            JOIN sys_user u ON u.id = s.user_id
            WHERE p.deleted = 0
            <if test="roundId != null"> AND p.round_id = #{roundId}</if>
            <if test="teacherId != null"> AND p.teacher_id = #{teacherId}</if>
            <if test="studentId != null"> AND p.student_id = #{studentId}</if>
            <if test="pickSource != null"> AND p.pick_source = #{pickSource}</if>
            ORDER BY p.created_at, p.id
            </script>
            """)
    List<PickVO> selectPickVOs(@Param("roundId") Long roundId,
                               @Param("teacherId") Long teacherId,
                               @Param("studentId") Long studentId,
                               @Param("pickSource") Integer pickSource);

    /** 某老师本轮"暂选未落地"的学生数（该生尚未被任何老师录取） — 用于名额计算 */
    @Select("""
            SELECT COUNT(*) FROM teacher_pick p
            WHERE p.teacher_id = #{teacherId} AND p.deleted = 0
              AND p.round_id = COALESCE(#{roundId}, 0)
              AND NOT EXISTS (SELECT 1 FROM match_result m
                              WHERE m.student_id = p.student_id
                                AND m.match_status = 1 AND m.deleted = 0)
            """)
    int countUnmatchedPicks(@Param("teacherId") Long teacherId, @Param("roundId") Long roundId);
}
