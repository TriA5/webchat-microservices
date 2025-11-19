package chat_service.example.chat_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat_service.example.chat_service.repository.MessageRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private MessageRepository messageRepository;

    // Tổng số tin nhắn hôm nay
    @GetMapping("/messages-today")
    public ResponseEntity<?> getMessagesToday() {
        try {
            Long count = messageRepository.countMessagesToday();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Số lượng tin nhắn theo từng ngày trong tuần
    @GetMapping("/messages-by-week")
    public ResponseEntity<?> getMessagesByWeek() {
        try {
            // Tính ngày đầu tuần (Thứ 2)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            List<Object[]> rawData = messageRepository.countMessagesByDayOfWeek(startOfWeek);
            
            // Khởi tạo map với 7 ngày (1=Chủ nhật, 2=Thứ 2, ..., 7=Thứ 7)
            Map<Integer, Long> dayData = new HashMap<>();
            for (int i = 1; i <= 7; i++) {
                dayData.put(i, 0L);
            }
            
            // Điền dữ liệu thực tế
            for (Object[] row : rawData) {
                int dayOfWeek = ((Number) row[0]).intValue();
                long count = ((Number) row[1]).longValue();
                dayData.put(dayOfWeek, count);
            }
            
            // Tạo kết quả theo thứ tự Thứ 2 -> Chủ nhật
            List<Map<String, Object>> result = new ArrayList<>();
            String[] dayNames = {
                "Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"
            };
            
            // Bắt đầu từ Thứ 2 (dayOfWeek = 2)
            for (int i = 0; i < 7; i++) {
                int dayIndex = (i + 2) > 7 ? (i + 2 - 7) : (i + 2); // 2,3,4,5,6,7,1
                
                Map<String, Object> item = new HashMap<>();
                item.put("day", dayNames[dayIndex - 1]);
                item.put("dayOfWeek", dayIndex);
                item.put("count", dayData.get(dayIndex));
                result.add(item);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
