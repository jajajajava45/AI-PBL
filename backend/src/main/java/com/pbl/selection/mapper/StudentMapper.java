package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pbl.selection.entity.Student;
import com.pbl.selection.vo.StudentAdminVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StudentMapper extends BaseMapper<Student> {

    /** 管理端学生分页（含账号信息） */
    @Select("""
            <script>
            SELECT s.id, u.id AS userId, u.username, s.student_no AS studentNo, u.real_name AS realName,
                   u.phone, u.email, u.enabled, s.major, s.grade, s.class_name AS className
            FROM student s
            JOIN sys_user u ON u.id = s.user_id AND u.deleted = 0
            WHERE s.deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (u.real_name LIKE CONCAT('%', #{keyword}, '%')
                   OR s.student_no LIKE CONCAT('%', #{keyword}, '%')
                   OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY s.student_no
            </script>
            """)
    IPage<StudentAdminVO> selectStudentPage(Page<?> page, @Param("keyword") String keyword);
}
