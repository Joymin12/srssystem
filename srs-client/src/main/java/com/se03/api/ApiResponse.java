package com.se03.api;

// 보고서/다이어그램에는 DTO 응답이 등장하지만, 이 과제 구현은 외부 라이브러리 없이
// Java 표준 HTTP만 사용하기 위해 단순 wire text를 공통 응답 객체로 감싼다.
public record ApiResponse(String raw) {
    public boolean ok() { return raw != null && raw.startsWith("OK"); }
    public String value(String key) {
        for (String line : raw.split("\n")) {
            int idx = line.indexOf('=');
            if (idx > 0 && line.substring(0, idx).equals(key)) return line.substring(idx + 1);
        }
        return "";
    }
    public String message() {
        String m = value("message");
        return m.isBlank() ? raw : m;
    }
}
