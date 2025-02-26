package com.capstone1.sasscapstone1.service.AdminDashboardService;

import com.capstone1.sasscapstone1.dto.AdminDashboardStatsDto.StatsDto;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Folder.FolderRepository;
import com.capstone1.sasscapstone1.repository.Subject.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final AccountRepository accountRepository;
    private final DocumentsRepository documentsRepository;
    private final FolderRepository folderRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public AdminDashboardServiceImpl(AccountRepository accountRepository,
                                     DocumentsRepository documentsRepository,
                                     FolderRepository folderRepository,
                                     SubjectRepository subjectRepository) {
        this.accountRepository = accountRepository;
        this.documentsRepository = documentsRepository;
        this.folderRepository = folderRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public StatsDto getDashboardStats() {
        try {
            StatsDto stats = new StatsDto();

            // Tổng số sinh viên
            stats.setTotalStudents(accountRepository.countByRoles_Name("STUDENT"));

            // Tổng số giảng viên
            stats.setTotalLecturers(accountRepository.countByRoles_Name("LECTURER"));

            // Tổng số tài liệu
            stats.setTotalDocuments(documentsRepository.count());

            // Tổng số thư mục
            stats.setTotalFolders(folderRepository.count());

            // Tổng số môn học
            stats.setTotalSubjects(subjectRepository.count());

            // Tổng số quản trị viên
            stats.setTotalAdmins(accountRepository.countByRoles_Name("ADMIN"));

            return stats;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching dashboard stats: " + e.getMessage(), e);
        }
    }
}
