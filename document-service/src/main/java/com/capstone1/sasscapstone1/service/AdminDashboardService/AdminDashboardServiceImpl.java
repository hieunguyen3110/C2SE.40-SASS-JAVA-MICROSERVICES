package com.capstone1.sasscapstone1.service.AdminDashboardService;

import com.capstone1.sasscapstone1.dto.AdminDashboardStatsDto.StatsDto;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Folder.FolderRepository;
import com.capstone1.sasscapstone1.repository.Subject.SubjectRepository;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final DocumentsRepository documentsRepository;
    private final FolderRepository folderRepository;
    private final SubjectRepository subjectRepository;
    private final IdentityClient identityClient;

    @Override
    public StatsDto getDashboardStats() {
        try {
            StatsDto stats = new StatsDto();

//            // Tổng số sinh viên
            stats.setTotalStudents(identityClient.countStatsByRoleName("STUDENT").getData());

//            // Tổng số giảng viên
            stats.setTotalStudents(identityClient.countStatsByRoleName("LECTURER").getData());

            // Tổng số tài liệu
            stats.setTotalDocuments(documentsRepository.count());

            // Tổng số thư mục
            stats.setTotalFolders(folderRepository.count());

            // Tổng số môn học
            stats.setTotalSubjects(subjectRepository.count());

            // Tổng số quản trị viên
            stats.setTotalStudents(identityClient.countStatsByRoleName("ADMIN").getData());

            return stats;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching dashboard stats: " + e.getMessage(), e);
        }
    }
}
