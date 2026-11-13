package com.pbl.selection.controller;

import com.pbl.selection.common.Result;
import com.pbl.selection.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 管理端：统计与导出（US-40/41）
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final StatsService statsService;

    /** 总览统计 */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.ok(statsService.summary());
    }

    /** 各实训室统计 */
    @GetMapping("/by-room")
    public Result<List<Map<String, Object>>> byRoom() {
        return Result.ok(statsService.byRoom());
    }

    /** 各老师名额使用 */
    @GetMapping("/by-teacher")
    public Result<List<Map<String, Object>>> byTeacher() {
        return Result.ok(statsService.byTeacher());
    }

    /** 各轮次统计 */
    @GetMapping("/by-round")
    public Result<List<Map<String, Object>>> byRound() {
        return Result.ok(statsService.byRound());
    }

    /** 匹配结果 CSV 导出 */
    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] body = statsService.exportMatchCsv().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=match-result.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }
}
