package com.pbl.selection.controller;

import com.pbl.selection.common.Result;
import com.pbl.selection.dto.ConfigUpdateRequest;
import com.pbl.selection.dto.RoomRequest;
import com.pbl.selection.dto.StudentUpsertRequest;
import com.pbl.selection.dto.TeacherUpsertRequest;
import com.pbl.selection.dto.UserAdminRequest;
import com.pbl.selection.entity.SystemConfig;
import com.pbl.selection.entity.TeacherQuotaLog;
import com.pbl.selection.entity.TrainingRoom;
import com.pbl.selection.security.LoginUser;
import com.pbl.selection.service.AdminService;
import com.pbl.selection.service.ConfigService;
import com.pbl.selection.vo.PageResult;
import com.pbl.selection.vo.StudentAdminVO;
import com.pbl.selection.vo.TeacherAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端：基础数据 / 账号 / 系统配置（US-31/32/36）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDataController {

    private final AdminService adminService;
    private final ConfigService configService;

    // ==================== 实训室 ====================

    @GetMapping("/rooms")
    public Result<List<TrainingRoom>> rooms() {
        return Result.ok(adminService.listRooms());
    }

    @PostMapping("/rooms")
    public Result<Void> createRoom(@Valid @RequestBody RoomRequest req) {
        adminService.createRoom(req);
        return Result.ok();
    }

    @PutMapping("/rooms/{id}")
    public Result<Void> updateRoom(@PathVariable Long id, @RequestBody RoomRequest req) {
        adminService.updateRoom(id, req);
        return Result.ok();
    }

    // ==================== 老师 ====================

    @GetMapping("/teachers")
    public Result<PageResult<TeacherAdminVO>> teachers(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long size,
                                                       @RequestParam(required = false) Long roomId,
                                                       @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.teacherPage(page, size, roomId, keyword));
    }

    @PostMapping("/teachers")
    public Result<Void> createTeacher(@Valid @RequestBody TeacherUpsertRequest req) {
        adminService.createTeacher(req);
        return Result.ok();
    }

    @PutMapping("/teachers/{id}")
    public Result<Void> updateTeacher(@AuthenticationPrincipal LoginUser admin,
                                      @PathVariable Long id,
                                      @RequestBody TeacherUpsertRequest req) {
        adminService.updateTeacher(id, req, admin.userId());
        return Result.ok();
    }

    @GetMapping("/teachers/{id}/quota-logs")
    public Result<List<TeacherQuotaLog>> quotaLogs(@PathVariable Long id) {
        return Result.ok(adminService.quotaLogs(id));
    }

    // ==================== 学生 ====================

    @GetMapping("/students")
    public Result<PageResult<StudentAdminVO>> students(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long size,
                                                       @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.studentPage(page, size, keyword));
    }

    @PostMapping("/students")
    public Result<Void> createStudent(@Valid @RequestBody StudentUpsertRequest req) {
        adminService.createStudent(req);
        return Result.ok();
    }

    @PutMapping("/students/{id}")
    public Result<Void> updateStudent(@PathVariable Long id, @RequestBody StudentUpsertRequest req) {
        adminService.updateStudent(id, req);
        return Result.ok();
    }

    // ==================== 账号 ====================

    @PutMapping("/users/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable Long id,
                                   @Valid @RequestBody UserAdminRequest.EnabledRequest req) {
        adminService.setUserEnabled(id, req.getEnabled());
        return Result.ok();
    }

    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody UserAdminRequest.ResetPasswordRequest req) {
        adminService.resetPassword(id, req.getNewPassword());
        return Result.ok();
    }

    // ==================== 系统配置 ====================

    @GetMapping("/config")
    public Result<List<SystemConfig>> config() {
        return Result.ok(configService.listAll());
    }

    @PutMapping("/config/{key}")
    public Result<Void> updateConfig(@PathVariable String key,
                                     @Valid @RequestBody ConfigUpdateRequest req) {
        configService.update(key, req.getValue());
        return Result.ok();
    }
}
