package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.dto.RoomRequest;
import com.pbl.selection.dto.StudentUpsertRequest;
import com.pbl.selection.dto.TeacherUpsertRequest;
import com.pbl.selection.dto.UserAdminRequest;
import com.pbl.selection.entity.Student;
import com.pbl.selection.entity.SysUser;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.entity.TeacherQuotaLog;
import com.pbl.selection.entity.TrainingRoom;
import com.pbl.selection.mapper.StudentMapper;
import com.pbl.selection.mapper.SysUserMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.TeacherQuotaLogMapper;
import com.pbl.selection.mapper.TrainingRoomMapper;
import com.pbl.selection.vo.PageResult;
import com.pbl.selection.vo.StudentAdminVO;
import com.pbl.selection.vo.TeacherAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理端基础数据：实训室 / 老师 / 学生 / 账号 / 名额（US-31/32）
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final TrainingRoomMapper roomMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final TeacherQuotaLogMapper quotaLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final ConfigService configService;
    private final FlowService flowService;
    private final QuotaService quotaService;

    // ==================== 实训室 ====================

    public List<TrainingRoom> listRooms() {
        return roomMapper.selectList(Wrappers.<TrainingRoom>lambdaQuery().orderByAsc(TrainingRoom::getId));
    }

    @Transactional
    public void createRoom(RoomRequest req) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw BizException.conflict("实训室编号不能为空");
        }
        TrainingRoom room = new TrainingRoom();
        room.setName(req.getName());
        room.setCode(req.getCode());
        room.setDescription(req.getDescription());
        room.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        room.setDeleted(0);
        roomMapper.insert(room);
    }

    @Transactional
    public void updateRoom(Long id, RoomRequest req) {
        TrainingRoom room = roomMapper.selectById(id);
        if (room == null) {
            throw BizException.notFound("实训室不存在");
        }
        if (req.getName() != null) {
            room.setName(req.getName());
        }
        if (req.getCode() != null) {
            room.setCode(req.getCode());
        }
        if (req.getDescription() != null) {
            room.setDescription(req.getDescription());
        }
        if (req.getStatus() != null) {
            room.setStatus(req.getStatus());
        }
        roomMapper.updateById(room);
    }

    // ==================== 老师 ====================

    public PageResult<TeacherAdminVO> teacherPage(long page, long size, Long roomId, String keyword) {
        return PageResult.of(teacherMapper.selectTeacherPage(Page.of(page, size), roomId, keyword),
                java.util.function.Function.identity());
    }

    @Transactional
    public void createTeacher(TeacherUpsertRequest req) {
        checkUsernameFree(req.getUsername());
        if (req.getRoomId() == null) {
            throw BizException.conflict("所属实训室不能为空");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(
                req.getPassword() == null || req.getPassword().isBlank() ? "123456" : req.getPassword()));
        user.setRealName(req.getRealName());
        user.setRole(Enums.Role.TEACHER.code);
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setEnabled(1);
        user.setDeleted(0);
        userMapper.insert(user);

        Teacher t = new Teacher();
        t.setUserId(user.getId());
        t.setTrainingRoomId(req.getRoomId());
        t.setTeacherNo(req.getTeacherNo() == null || req.getTeacherNo().isBlank()
                ? req.getUsername() : req.getTeacherNo());
        t.setTitle(req.getTitle());
        t.setResearchDirection(req.getResearchDirection());
        t.setBio(req.getBio());
        t.setQuota(req.getQuota() == null
                ? configService.getInt(ConfigService.KEY_TEACHER_DEFAULT_QUOTA, 16) : req.getQuota());
        t.setStatus(1);
        t.setDeleted(0);
        teacherMapper.insert(t);
    }

    @Transactional
    public void updateTeacher(Long id, TeacherUpsertRequest req, Long operatorId) {
        Teacher t = teacherMapper.selectById(id);
        if (t == null) {
            throw BizException.notFound("老师不存在");
        }
        if (req.getRoomId() != null) {
            t.setTrainingRoomId(req.getRoomId());
        }
        if (req.getTeacherNo() != null) {
            t.setTeacherNo(req.getTeacherNo());
        }
        if (req.getTitle() != null) {
            t.setTitle(req.getTitle());
        }
        if (req.getResearchDirection() != null) {
            t.setResearchDirection(req.getResearchDirection());
        }
        if (req.getBio() != null) {
            t.setBio(req.getBio());
        }
        if (req.getQuota() != null && !req.getQuota().equals(t.getQuota())) {
            int before = t.getQuota();
            t.setQuota(req.getQuota());
            TeacherQuotaLog log = new TeacherQuotaLog();
            log.setTeacherId(id);
            log.setQuotaBefore(before);
            log.setQuotaAfter(req.getQuota());
            log.setReason("ADMIN_MODIFY");
            log.setOperatorId(operatorId);
            log.setDeleted(0);
            quotaLogMapper.insert(log);
        }
        if (req.getStatus() != null) {
            t.setStatus(req.getStatus());
        }
        teacherMapper.updateById(t);

        // 账号信息
        SysUser user = userMapper.selectById(t.getUserId());
        if (user != null) {
            if (req.getRealName() != null) {
                user.setRealName(req.getRealName());
            }
            if (req.getPhone() != null) {
                user.setPhone(req.getPhone());
            }
            if (req.getEmail() != null) {
                user.setEmail(req.getEmail());
            }
            userMapper.updateById(user);
        }
    }

    public List<TeacherQuotaLog> quotaLogs(Long teacherId) {
        return quotaLogMapper.selectList(Wrappers.<TeacherQuotaLog>lambdaQuery()
                .eq(TeacherQuotaLog::getTeacherId, teacherId)
                .orderByDesc(TeacherQuotaLog::getId));
    }

    // ==================== 学生 ====================

    public PageResult<StudentAdminVO> studentPage(long page, long size, String keyword) {
        return PageResult.of(studentMapper.selectStudentPage(Page.of(page, size), keyword),
                java.util.function.Function.identity());
    }

    @Transactional
    public void createStudent(StudentUpsertRequest req) {
        checkUsernameFree(req.getUsername());
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(
                req.getPassword() == null || req.getPassword().isBlank() ? "123456" : req.getPassword()));
        user.setRealName(req.getRealName());
        user.setRole(Enums.Role.STUDENT.code);
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setEnabled(1);
        user.setDeleted(0);
        userMapper.insert(user);

        Student s = new Student();
        s.setUserId(user.getId());
        s.setStudentNo(req.getStudentNo() == null || req.getStudentNo().isBlank()
                ? req.getUsername() : req.getStudentNo());
        s.setMajor(req.getMajor());
        s.setGrade(req.getGrade());
        s.setClassName(req.getClassName());
        s.setDeleted(0);
        studentMapper.insert(s);
    }

    @Transactional
    public void updateStudent(Long id, StudentUpsertRequest req) {
        Student s = studentMapper.selectById(id);
        if (s == null) {
            throw BizException.notFound("学生不存在");
        }
        if (req.getStudentNo() != null) {
            s.setStudentNo(req.getStudentNo());
        }
        if (req.getMajor() != null) {
            s.setMajor(req.getMajor());
        }
        if (req.getGrade() != null) {
            s.setGrade(req.getGrade());
        }
        if (req.getClassName() != null) {
            s.setClassName(req.getClassName());
        }
        studentMapper.updateById(s);

        SysUser user = userMapper.selectById(s.getUserId());
        if (user != null) {
            if (req.getRealName() != null) {
                user.setRealName(req.getRealName());
            }
            if (req.getPhone() != null) {
                user.setPhone(req.getPhone());
            }
            if (req.getEmail() != null) {
                user.setEmail(req.getEmail());
            }
            userMapper.updateById(user);
        }
    }

    // ==================== 账号 ====================

    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        user.setEnabled(enabled ? 1 : 0);
        userMapper.updateById(user);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private void checkUsernameFree(String username) {
        Long cnt = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username));
        if (cnt != null && cnt > 0) {
            throw BizException.conflict("登录名已存在: " + username);
        }
    }
}
