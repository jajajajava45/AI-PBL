package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pbl.selection.entity.WaitPool;
import com.pbl.selection.vo.WaitPoolVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WaitPoolMapper extends BaseMapper<WaitPool> {

    /** 待选池列表（带学生信息与安置老师姓名） */
    @Select("""
            <script>
            SELECT wp.id, wp.student_id AS studentId, s.student_no AS studentNo, u.real_name AS studentName,
                   s.major, s.class_name AS className, u.phone, u.email,
                   wp.enter_round AS enterRound, wp.enter_at AS enterAt, wp.interview_at AS interviewAt,
                   wp.status, wp.settle_teacher_id AS settleTeacherId, tu.real_name AS settleTeacherName,
                   wp.settle_remark AS settleRemark, wp.settle_at AS settleAt
            FROM wait_pool wp
            JOIN student s ON s.id = wp.student_id AND s.deleted = 0
            JOIN sys_user u ON u.id = s.user_id
            LEFT JOIN teacher t ON t.id = wp.settle_teacher_id
            LEFT JOIN sys_user tu ON tu.id = t.user_id
            WHERE wp.deleted = 0
            <if test="status != null"> AND wp.status = #{status}</if>
            ORDER BY wp.status, wp.enter_at, wp.id
            </script>
            """)
    List<WaitPoolVO> selectWaitPoolVOs(@Param("status") Integer status);
}
