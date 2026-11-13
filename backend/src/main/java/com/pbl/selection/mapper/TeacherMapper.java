package com.pbl.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.vo.TeacherAdminVO;
import com.pbl.selection.vo.TeacherCardVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeacherMapper extends BaseMapper<Teacher> {

    /** 管理端老师分页（含账号信息） */
    @Select("""
            <script>
            SELECT t.id, u.id AS userId, u.username, t.teacher_no AS teacherNo, u.real_name AS realName,
                   u.phone, u.email, u.enabled, t.title, t.research_direction AS researchDirection,
                   t.bio, t.quota, t.status, t.training_room_id AS roomId, r.name AS roomName
            FROM teacher t
            JOIN sys_user u ON u.id = t.user_id AND u.deleted = 0
            JOIN training_room r ON r.id = t.training_room_id
            WHERE t.deleted = 0
            <if test="roomId != null"> AND t.training_room_id = #{roomId}</if>
            <if test="keyword != null and keyword != ''">
              AND (u.real_name LIKE CONCAT('%', #{keyword}, '%')
                   OR t.teacher_no LIKE CONCAT('%', #{keyword}, '%')
                   OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY r.id, t.id
            </script>
            """)
    IPage<TeacherAdminVO> selectTeacherPage(Page<?> page,
                                            @Param("roomId") Long roomId,
                                            @Param("keyword") String keyword);


    /**
     * 老师卡片列表（含实时剩余名额）
     * 剩余 = quota − 已录取数 − 本轮暂选未落地人数
     */
    @Select("""
            <script>
            SELECT t.id, t.teacher_no AS teacherNo, u.real_name AS realName, t.title,
                   t.research_direction AS researchDirection, t.bio, t.quota, t.status AS status,
                   t.training_room_id AS roomId, r.name AS roomName,
                   (t.quota
                     - (SELECT COUNT(*) FROM match_result m
                        WHERE m.teacher_id = t.id AND m.match_status = 1 AND m.deleted = 0)
                     - (SELECT COUNT(*) FROM teacher_pick p
                        WHERE p.teacher_id = t.id AND p.deleted = 0
                          AND p.round_id = COALESCE(#{currentRoundId}, 0)
                          AND NOT EXISTS (SELECT 1 FROM match_result m2
                                          WHERE m2.student_id = p.student_id
                                            AND m2.match_status = 1 AND m2.deleted = 0))
                   ) AS remaining
            FROM teacher t
            JOIN sys_user u ON u.id = t.user_id AND u.deleted = 0
            JOIN training_room r ON r.id = t.training_room_id AND r.deleted = 0
            WHERE t.deleted = 0
            <if test="roomId != null"> AND t.training_room_id = #{roomId}</if>
            <if test="onlyActive"> AND t.status = 1 AND u.enabled = 1 AND r.status = 1</if>
            ORDER BY r.id, t.id
            </script>
            """)
    List<TeacherCardVO> selectTeacherCards(@Param("roomId") Long roomId,
                                           @Param("currentRoundId") Long currentRoundId,
                                           @Param("onlyActive") boolean onlyActive);
}
