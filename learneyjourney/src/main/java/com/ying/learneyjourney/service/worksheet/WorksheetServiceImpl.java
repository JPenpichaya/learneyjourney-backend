package com.ying.learneyjourney.service.worksheet;

import com.ying.learneyjourney.criteria.WorksheetCriteria;
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
import com.ying.learneyjourney.master.PageCriteria;
import com.ying.learneyjourney.repository.ExportUsageRepository;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.repository.WorksheetRepository;
import com.ying.learneyjourney.repository.WorksheetVersionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    private final WorksheetVersionRepository worksheetVersionRepository;

    @Override
    public WorksheetDetailResponse create(CreateWorksheetRequest request, String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        Worksheet worksheet = new Worksheet();
        worksheet.setUser(user);
        worksheet.setTitle(request.title());
        worksheet.setSubject(request.subject());
        worksheet.setPromptText(request.promptText());
        worksheet.setLanguage(request.language());
        worksheet.setActiveVersionLabel(request.activeVersionLabel());

        worksheetRepository.save(worksheet);

        List<WorksheetVersion> versions = new ArrayList<>();
        int i = 0;
        for (CreateWorksheetRequest.VersionPayload payload : request.versions()) {
            WorksheetVersion version = new WorksheetVersion();
            version.setWorksheet(worksheet);
            version.setVersionLabel(payload.versionLabel());
            version.setSortOrder(payload.sortOrder() != null ? payload.sortOrder() : i++);
            version.setHtmlContent(payload.htmlContent());
            worksheetVersionRepository.save(version);
            versions.add(version);
        }
        worksheet.setVersions(versions);

        if (worksheet.getActiveVersionLabel() == null && !worksheet.getVersions().isEmpty()) {
            worksheet.setActiveVersionLabel(worksheet.getVersions().getFirst().getVersionLabel());
        }

        return worksheetMapper.toDetail(worksheetRepository.save(worksheet));
    }

    @Override
    public WorksheetDetailResponse getById(String userId, UUID id) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));
        worksheet.getVersions().sort(Comparator.comparing(WorksheetVersion::getSortOrder));
        return worksheetMapper.toDetail(worksheet);
    }

    @Override
    public WorksheetDetailResponse updateMeta(String userId, UUID id, UpdateWorksheetRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));
        if (request.title() != null) worksheet.setTitle(request.title());
        if (request.subject() != null) worksheet.setSubject(request.subject());
        if (request.language() != null) worksheet.setLanguage(request.language());
        if (request.activeVersionLabel() != null) worksheet.setActiveVersionLabel(request.activeVersionLabel());
        return worksheetMapper.toDetail(worksheet);
    }

    @Override
    public WorksheetDetailResponse saveVersion(UUID worksheetId, SaveWorksheetRequest request, String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(worksheetId, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));

        WorksheetVersion version = worksheet.getVersions().stream()
                .filter(v -> v.getId().equals(request.versionId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Version not found"));

        version.setVersionLabel(request.versionLabel());
        version.setHtmlContent(request.htmlContent());
        if (Boolean.TRUE.equals(request.setActive())) {
            worksheet.setActiveVersionLabel(request.versionLabel());
        }

        return worksheetMapper.toDetail(worksheet);
    }

    @Override
    public WorksheetDetailResponse duplicate(String userId, UUID id) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Worksheet source = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));

        Worksheet duplicate = new Worksheet();
        duplicate.setUser(user);
        duplicate.setTitle(source.getTitle() + " (Copy)");
        duplicate.setSubject(source.getSubject());
        duplicate.setPromptText(source.getPromptText());
        duplicate.setLanguage(source.getLanguage());
        duplicate.setActiveVersionLabel(source.getActiveVersionLabel());

        worksheetRepository.save(duplicate);

        List<WorksheetVersion> versions = new ArrayList<>();

        for (WorksheetVersion v : source.getVersions()) {
            WorksheetVersion version = new WorksheetVersion();
            version.setWorksheet(duplicate);
            version.setVersionLabel(v.getVersionLabel());
            version.setSortOrder(v.getSortOrder());
            version.setHtmlContent(v.getHtmlContent());
            worksheetVersionRepository.save(version);
            versions.add(version);
        }
        duplicate.setVersions(versions);

        return worksheetMapper.toDetail(worksheetRepository.save(duplicate));
    }

    @Override
    public void delete(String userId, UUID id) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));
        worksheet.setDeleted(true);
    }

    @Override
    public void export(ExportWorksheetRequest request, String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Worksheet worksheet = worksheetRepository.findByIdAndUserIdAndDeletedFalse(request.worksheetId(), user.getId())
                .orElseThrow(() -> new NotFoundException("Worksheet not found"));

        WorksheetVersion version = worksheet.getVersions().stream()
                .filter(v -> v.getId().equals(request.versionId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Version not found"));

        billingService.consumeExport(user);

        worksheet.setExportCount(worksheet.getExportCount() + 1);
        version.setExportCount(version.getExportCount() + 1);
        ExportUsage export = new ExportUsage();
        export.setUser(user);
        export.setWorksheet(worksheet);
        export.setVersion(version);

        exportUsageRepository.save(export);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public PagedResponse<CourseInfoResponse.WorksheetSummaryResponse> list(String userId, String keyword, int page, int size) {
        PageCriteria<WorksheetCriteria> condition = new PageCriteria<>();
        WorksheetCriteria worksheetCriteria = new WorksheetCriteria();
        worksheetCriteria.setSearchText(keyword);
        worksheetCriteria.setUserId(userId);
        condition.setCondition(worksheetCriteria);
        condition.setPageNumber(page);
        condition.setPageSize(size);
        condition.setSortBy("createdAt");
        condition.setDirection(Sort.Direction.DESC);
        Page<Worksheet> all = worksheetRepository.findAll(condition.getSpecification(), condition.generatePageRequest());
        return PagedResponse.from(all.map(p -> {
            return new CourseInfoResponse.WorksheetSummaryResponse(
                    p.getId(),
                    p.getTitle(),
                    p.getSubject(),
                    p.getLanguage(),
                    p.getActiveVersionLabel(),
                    p.getVersions().size(),
                    p.getExportCount(),
                    p.getCreatedAt(),
                    p.getUpdatedAt()
            );
        }));
    }

}