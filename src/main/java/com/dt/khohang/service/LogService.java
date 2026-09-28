package com.dt.khohang.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Lưu trữ danh sách log hoạt động hệ thống (in-memory)
 * cho AOP Logging trực quan trên giao diện quản trị.
 */
@Service
public class LogService {

    private final List<LogEntry> logs = Collections.synchronizedList(new ArrayList<>());

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void addLog(String action, String detail, String username) {
        LogEntry entry = new LogEntry(
                LocalDateTime.now().format(FORMATTER),
                action,
                detail,
                username
        );
        logs.add(0, entry); // Thêm vào đầu danh sách (mới nhất trước)

        // Giữ tối đa 200 dòng log
        if (logs.size() > 200) {
            logs.remove(logs.size() - 1);
        }
    }

    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(logs);
    }

    public int getLogCount() {
        return logs.size();
    }

    /**
     * Lớp DTO lưu 1 dòng log
     */
    public static class LogEntry {
        private final String timestamp;
        private final String action;
        private final String detail;
        private final String username;

        public LogEntry(String timestamp, String action, String detail, String username) {
            this.timestamp = timestamp;
            this.action = action;
            this.detail = detail;
            this.username = username;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getAction() {
            return action;
        }

        public String getDetail() {
            return detail;
        }

        public String getUsername() {
            return username;
        }
    }
}
