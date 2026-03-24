package com.ying.learneyjourney.service;

import com.ying.learneyjourney.Util.HtmlSanitizer;
import com.ying.learneyjourney.dto.request.CreateWorksheetRequest;
import com.ying.learneyjourney.dto.request.ExportWorksheetRequest;
import com.ying.learneyjourney.dto.request.SaveWorksheetRequest;
import com.ying.learneyjourney.dto.request.UpdateWorksheetRequest;
import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.dto.response.PagedResponse;
import com.ying.learneyjourney.dto.response.WorksheetDetailResponse;
import com.ying.learneyjourney.entity.ExportUsage;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.entity.Worksheet;
import com.ying.learneyjourney.entity.WorksheetVersion;
import com.ying.learneyjourney.exception.NotFoundException;
import com.ying.learneyjourney.mapper.WorksheetMapper;
import com.ying.learneyjourney.repository.ExportUsageRepository;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.repository.WorksheetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorksheetServiceImpl implements WorksheetService {

    private final WorksheetRepository worksheetRepository;
    private final UserRepository userRepository;
    private final ExportUsageRepository exportUsageRepository;
    private final WorksheetMapper worksheetMapper;
    private final BillingService billingService;

    @Override
    public WorksheetDetailResponse create(CreateWorksheetRequest request) {
        User user = getUserByEmail(request.userEmail());

        Worksheet worksheet = Worksheet.builder()
                .user(user)
                .title(request.title())
                .subject(request.subject())
                .promptText(request.promptText())
                .language(request.language())
                .activeVersionLabel(request.activeVersionLabel())
                .build();

        int i = 0;
        for (CreateWorksheetRequest.VersionPayload payload : request.versions()) {
            WorksheetVersion version = new WorksheetVersion();
            version.setVersionLabel(payload.versionLabel());
            version.setSortOrder(payload.sortOrder() != null ? payload.sortOrder() : i++);
            version.setHtmlContent(HtmlSanitizer.sanitize(payload.htmlContent()));
            version.setWorksheet(worksheet);
            worksheet.addVersion(version);
        }

        if (worksheet.getActiveVersionLabel() == null && !worksheet.getVersions().isEmpty()) {
            worksheet.setActiveVersionLabel(worksheet.getVersions().getFirst().getVersionLabel());
        }

        return worksheetMapper.toDetail(worksheetRepository.save(worksheet));
    }

    @Override
    public WorksheetDetailResponse getById(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));
        worksheet.getVersions().sort(Comparator.comparing(WorksheetVersion::getSortOrder));
        return worksheetMapper.toDetail(worksheet);
    }

    @Override
    public WorksheetDetailResponse updateMeta(String userEmail, UUID id, UpdateWorksheetRequest request) {
        User user = getUserByEmail(userEmail);
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));
        if (request.title() != null) worksheet.setTitle(request.title());
        if (request.subject() != null) worksheet.setSubject(request.subject());
        if (request.language() != null) worksheet.setLanguage(request.language());
        if (request.activeVersionLabel() != null) worksheet.setActiveVersionLabel(request.activeVersionLabel());
        return worksheetMapper.toDetail(worksheet);
    }
    @Override
    public WorksheetDetailResponse saveVersion(UUID worksheetId, SaveWorksheetRequest request) {
        User user = getUserByEmail(request.userEmail());
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(worksheetId, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));

        WorksheetVersion version = worksheet.getVersions().stream()
                .filter(v -> v.getId().equals(request.versionId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Version not found"));

        version.setVersionLabel(request.versionLabel());
        version.setHtmlContent(HtmlSanitizer.sanitize(request.htmlContent()));
        if (Boolean.TRUE.equals(request.setActive())) {
            worksheet.setActiveVersionLabel(request.versionLabel());
        }

        return worksheetMapper.toDetail(worksheet);
    }

    @Override
    public WorksheetDetailResponse duplicate(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Worksheet source = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));

        Worksheet duplicate = Worksheet.builder()
                .user(user)
                .title(source.getTitle() + " (Copy)")
                .subject(source.getSubject())
                .promptText(source.getPromptText())
                .language(source.getLanguage())
                .activeVersionLabel(source.getActiveVersionLabel())
                .build();

        for (WorksheetVersion v : source.getVersions()) {
            WorksheetVersion version = new WorksheetVersion();
            version.setVersionLabel(v.getVersionLabel());
            version.setSortOrder(v.getSortOrder());
            version.setHtmlContent(v.getHtmlContent());
            duplicate.addVersion(version);
        }

        return worksheetMapper.toDetail(worksheetRepository.save(duplicate));
    }
    @Override
    public void delete(String userEmail, UUID id) {
        User user = getUserByEmail(userEmail);
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));
        worksheet.setDeleted(true);
    }
    @Override
    public void export(ExportWorksheetRequest request) {
        User user = getUserByEmail(request.userEmail());
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(request.worksheetId(), user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));

        WorksheetVersion version = worksheet.getVersions().stream()
                .filter(v -> v.getId().equals(request.versionId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Version not found"));

        billingService.consumeExport(user.getEmail());

        worksheet.setExportCount(worksheet.getExportCount() + 1);
        version.setExportCount(version.getExportCount() + 1);

        exportUsageRepository.save(ExportUsage.builder()
                .user(user)
                .worksheet(worksheet)
                .version(version)
                .build());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public PagedResponse<CourseInfoResponse.WorksheetSummaryResponse> list(String userEmail, String keyword, int page, int size) {
        return null;
    }

}