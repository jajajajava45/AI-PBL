package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pbl.selection.entity.Interview;
import com.pbl.selection.vo.InterviewVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InterviewMapper extends BaseMapper<Interview> {

    /** 面试记录（带学生/老师信息），支持动态过滤 */
    @Select("""
            <script>
            SELECT i.id, i.round_id AS roundId, i.student_id AS studentId, s.student_no AS studentNo,
                   u.real_name AS studentName, s.major, s.class_name AS className,
                   i.teacher_id AS teacherId, tu.real_name AS teacherName,
                   i.interview_time AS interviewTime, i.location, i.status, i.score, i.feedback
            FROM interview i
            JOIN student s ON s.id = i.student_id AND s.deleted = 0
            JOIN sys_user u ON u.id = s.user_id
            JOIN teacher t ON t.id = i.teacher_id AND t.deleted = 0
            JOIN sys_user tu ON tu.id = t.user_id
            WHERE i.deleted = 0
            <if test="roundId != null"> AND i.round_id = #{roundId}</if>
            <if test="teacherId != null"> AND i.teacher_id = #{teacherId}</if>
            <if test="studentId != null"> AND i.student_id = #{studentId}</if>
            <if test="status != null"> AND i.status = #{status}</if>
            ORDER BY i.interview_time, i.id
            </script>
            """)
    List<InterviewVO> selectInterviewVOs(@Param("roundId") Long roundId,
                                         @Param("teacherId") Long teacherId,
                                         @Param("studentId") Long studentId,
                                         @Param("status") Integer status);
}
