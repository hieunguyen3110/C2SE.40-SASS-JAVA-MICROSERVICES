package com.capstone1.sasscapstone1.util;

import com.capstone1.sasscapstone1.entity.DocumentView;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.History;
import com.capstone1.sasscapstone1.entity.Ratings;
import com.capstone1.sasscapstone1.repository.DocumentView.DocumentViewRepository;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.History.HistoryRepository;
import com.capstone1.sasscapstone1.repository.Ratings.RatingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UpdateDocumentPopularity {
    private final DocumentViewRepository documentViewRepository;
    private final DocumentsRepository documentsRepository;
    private final RatingsRepository ratingsRepository;
    private final HistoryRepository historyRepository;
    private static final double RATING_WEIGHT=60.0; // range [60-120]
    private static final double DOWNLOAD_WEIGHT=40.0; // range [30-80]
    private static final double NORMALIZED_NUMBER=500.0;
    private static final double SIGNIFICANT_DECREASE_THRESHOLD = 0.4;

    public List<Documents> updateDocumentPopularity(LocalDateTime startDate, LocalDateTime endDate) {
        final double DECAY_RATE = 0.9;
        final int MIN_EXPECTED_INTERACTIONS = 50;

        List<Documents> documents = documentsRepository.findAllByIsActiveIsTrue();

        // Load toàn bộ view, rating, history trong khoảng thời gian
        List<DocumentView> allViews = documentViewRepository.findAllByCreatedAtBetween(startDate, endDate);
        List<Ratings> allRatings = ratingsRepository.findAllByCreatedAt(startDate, endDate);
        List<History> allHistories = historyRepository.findAllByCreatedAtBetween(startDate, endDate);

        // Group dữ liệu theo documentId
        Map<Long, List<DocumentView>> viewsByDoc = allViews.stream()
                .collect(Collectors.groupingBy(DocumentView::getDocumentId));
        Map<Long, List<Ratings>> ratingsByDoc = allRatings.stream()
                .collect(Collectors.groupingBy(r -> r.getDocuments().getDocId()));
        Map<Long, List<History>> historyByDoc = allHistories.stream()
                .collect(Collectors.groupingBy(h -> h.getDocument().getDocId()));

        List<Documents> documentsToUpdate = new ArrayList<>();

        for (Documents document : documents) {
            long docId = document.getDocId();
            double previousPopularity = document.getPopularity();

            List<DocumentView> documentViews = viewsByDoc.getOrDefault(docId, Collections.emptyList());
            List<Ratings> ratings = ratingsByDoc.getOrDefault(docId, Collections.emptyList());
            List<History> histories = historyByDoc.getOrDefault(docId, Collections.emptyList());

            if (!documentViews.isEmpty() || !ratings.isEmpty() || !histories.isEmpty()) {
                // Tính tổng thời gian xem trung bình theo từng user
                Map<Long, List<DocumentView>> viewsByUser = documentViews.stream()
                        .collect(Collectors.groupingBy(DocumentView::getAccountId));

                double totalViewTime = viewsByUser.values().stream()
                        .mapToDouble(viewList -> {
                            long totalDuration = viewList.stream().mapToLong(DocumentView::getDurationSeconds).sum();
                            return (double) totalDuration / viewList.size();
                        })
                        .sum();

                // Tính rating trung bình
                List<Integer> ratingValues = ratings.stream()
                        .map(Ratings::getRating)
                        .toList();
                double avgRating = ratingValues.isEmpty() ? 0 : ratingValues.stream().mapToInt(Integer::intValue).average().orElse(0);
                int totalRating = ratingValues.size();

                long totalDownload = histories.size();

                double rawPopularity = totalViewTime
                        + (RATING_WEIGHT * avgRating * totalRating)
                        + (DOWNLOAD_WEIGHT * totalDownload);

                int interactionCount = viewsByUser.size() + ratings.size() + histories.size();
                double engagementWeight = Math.min(1.0, (double) interactionCount / MIN_EXPECTED_INTERACTIONS);
                double weightedPopularity = rawPopularity * engagementWeight;
                double normalizedPopularity = Math.min(10.0, weightedPopularity / NORMALIZED_NUMBER);

                if (normalizedPopularity < previousPopularity * (1 - SIGNIFICANT_DECREASE_THRESHOLD)) {
                    document.setPopularity(Math.max(normalizedPopularity, previousPopularity * DECAY_RATE));
                } else {
                    document.setPopularity(normalizedPopularity);
                }
            } else {
                // Không có tương tác mới, giảm nhẹ theo decay
                document.setPopularity(previousPopularity * DECAY_RATE);
            }

            documentsToUpdate.add(document);
        }

        return documentsRepository.saveAll(documentsToUpdate);
    }

}
