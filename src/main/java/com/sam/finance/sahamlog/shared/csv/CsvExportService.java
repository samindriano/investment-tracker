package com.sam.finance.sahamlog.shared.csv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CsvExportService {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public <T> ResponseEntity<String> export(String prefix, List<String> headers, List<T> rows, Function<T, List<String>> columnMapper) {
        StringBuilder csv = new StringBuilder(String.join(",", headers)).append('\n');
        rows.forEach(row -> csv.append(formatRow(columnMapper.apply(row))).append('\n'));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s-%s.csv".formatted(prefix, LocalDateTime.now().format(FILE_TS)))
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.toString());
    }

    private String formatRow(List<String> columns) {
        return columns.stream()
            .map(this::escape)
            .collect(java.util.stream.Collectors.joining(","));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
