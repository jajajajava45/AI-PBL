package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pbl.selection.entity.DiversionRecord;
import com.pbl.selection.vo.DiversionRecordVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DiversionRecordMapper extends BaseMapper<DiversionRecord> {

    /** 分流记录（带学生/老师姓名） */
    @Select("""
            <script>
            SELECT d.id, d.round_id AS roundId, d.student_id AS studentId, s.student_no AS studentNo,
                   u.real_name AS studentName, d.step, d.teacher_id AS teacherId, tu.real_name AS teacherName,
                   d.scope_type AS scopeType, d.result, d.created_at AS createdAt
            FROM diversion_record d
            JOIN student s ON s.id = d.student_id AND s.deleted = 0
            JOIN sys_user u ON u.id = s.user_id
            LEFT JOIN teacher t ON t.id = d.teacher_id
            LEFT JOIN sys_user tu ON tu.id = t.user_id
            WHERE d.deleted = 0
            <if test="roundId != null"> AND d.round_id = #{roundId}</if>
            <if test="studentId != null"> AND d.student_id = #{studentId}</if>
            ORDER BY d.student_id, d.step, d.id
            </script>
            """)
    List<DiversionRecordVO> selectRecordVOs(@Param("roundId") Long roundId,
                                            @Param("studentId") Long studentId);
}
