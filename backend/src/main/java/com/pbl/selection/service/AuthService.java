package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.dto.ChangePasswordRequest;
import com.pbl.selection.dto.LoginRequest;
import com.pbl.selection.entity.Student;
import com.pbl.selection.entity.SysUser;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.entity.TrainingRoom;
import com.pbl.selection.mapper.StudentMapper;
import com.pbl.selection.mapper.SysUserMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.TrainingRoomMapper;
import com.pbl.selection.security.JwtUtil;
import com.pbl.selection.security.LoginUser;
import com.pbl.selection.vo.TeacherCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证服务：登录 / 个人信息 / 修改密码
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final TrainingRoomMapper roomMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final QuotaService quotaService;
    private final FlowService flowService;

    public Map<String, Object> login(LoginRequest req) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, req.getUsername()).last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getEnabled() == null || user.getEnabled() != 1) {
            throw BizException.conflict("账号已被禁用，请联系管理员");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        LoginUser principal = new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", jwtUtil.generate(principal));
        data.put("user", profile(user));
        return data;
    }

    public Map<String, Object> me(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        return profile(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw BizException.conflict("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
    }

    private Map<String, Object> profile(SysUser user) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", user.getId());
        p.put("username", user.getUsername());
        p.put("realName", user.getRealName());
        p.put("role", user.getRole());
        p.put("phone", user.getPhone());
        p.put("email", user.getEmail());

        if (user.getRole() == 1) {
            Student s = studentMapper.selectOne(Wrappers.<Student>lambdaQuery()
                    .eq(Student::getUserId, user.getId()).last("LIMIT 1"));
            if (s != null) {
                p.put("studentId", s.getId());
                p.put("studentNo", s.getStudentNo());
                p.put("major", s.getMajor());
                p.put("grade", s.getGrade());
                p.put("className", s.getClassName());
            }
        } else if (user.getRole() == 2) {
            Teacher t = teacherMapper.selectOne(Wrappers.<Teacher>lambdaQuery()
                    .eq(Teacher::getUserId, user.getId()).last("LIMIT 1"));
            if (t != null) {
                p.put("teacherId", t.getId());
                p.put("teacherNo", t.getTeacherNo());
                p.put("title", t.getTitle());
                p.put("researchDirection", t.getResearchDirection());
                p.put("roomId", t.getTrainingRoomId());
                TrainingRoom room = t.getTrainingRoomId() == null
                        ? null : roomMapper.selectById(t.getTrainingRoomId());
                p.put("roomName", room == null ? null : room.getName());
                p.put("quota", t.getQuota());
                Long currentRoundId = flowService.getCurrentRound() == null
                        ? null : flowService.getCurrentRound().getId();
                p.put("remaining", quotaService.remaining(t, currentRoundId));
            }
        }
        return p;
    }
}
