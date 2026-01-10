package com.whdgkr.tripsplite.controller;

import com.whdgkr.tripsplite.service.DevService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 개발/테스트 전용 컨트롤러
 * - 운영 환경에서는 비활성화 필요
 * - 데이터 초기화 등 개발 편의 기능 제공
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Slf4j
public class DevController {

    private final DevService devService;

    /**
     * 모든 데이터 초기화 (앱 최초 설치 상태로 복원)
     * - DELETE 방식 사용 (TRUNCATE/DROP 절대 금지)
     * - 개발/테스트 전용
     */
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> resetAllData() {
        log.warn("=== DATA RESET REQUESTED ===");

        Map<String, Integer> deletedCounts = devService.resetAllData();

        log.warn("=== DATA RESET COMPLETED === deleted: {}", deletedCounts);

        return Map.of(
            "status", "success",
            "message", "앱 최초 설치 상태로 초기화되었습니다",
            "deletedCounts", deletedCounts
        );
    }

    /**
     * 현재 데이터 통계 조회
     */
    @GetMapping("/stats")
    public Map<String, Object> getDataStats() {
        return devService.getDataStats();
    }
}
