package com.example.edusmile.Service;

import com.example.edusmile.Entity.Visitor;
import com.example.edusmile.Repository.VisitorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class VisitorService {
    private final VisitorRepository visitorRepository;

    public int increaseVisitorCount() {
        LocalDate today = LocalDate.now();
        Visitor visitor = visitorRepository.findByDate(today)
                .orElse(new Visitor(today));

        visitor.increaseCount();
        visitorRepository.save(visitor);
        return visitor.getCount();
    }

    public int getTodayVisitors() {
        return visitorRepository.findByDate(LocalDate.now())
                .map(Visitor::getCount)
                .orElse(0);
    }

    public Map<LocalDate, Integer> weeklyVisitor() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<Object[]> results = visitorRepository.findWeeklyVisitorStats(startDate, endDate);
        Map<LocalDate, Integer> visitorStats = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            visitorStats.put(date, 0);
        }

        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            int count = (int) result[1];
            visitorStats.put(date, count);
        }

        return visitorStats;
    }
}
