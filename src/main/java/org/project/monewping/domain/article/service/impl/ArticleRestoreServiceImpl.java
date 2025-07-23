package org.project.monewping.domain.article.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.service.ArticleRestoreService;
import org.project.monewping.domain.article.storage.ArticleBackupStorage;
import org.project.monewping.domain.interest.entity.Interest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로컬 또는 외부 백업 소스로부터 백업 데이터를 불러와,
 * DB에 존재하지 않는 기사만 복원합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ArticleRestoreServiceImpl implements ArticleRestoreService {

    private final ArticleBackupStorage backupStorage;
    private final ArticlesRepository articlesRepository;
    private final ArticlesMapper articlesMapper;

    /**
     * 지정된 날짜 범위(from, to) 내의 백업 데이터를 일별로 로드하여,
     * 현재 DB에 존재하지 않는 뉴스 기사만 필터링 후 저장합니다.
     *
     * @param from 복구 시작일 (포함)
     * @param to 복구 종료일 (포함)
     * @return 날짜별 복구 결과 목록. 각 결과는 복구 일시, 복구된 기사 ID 리스트 및 복구 건수를 포함함.
     * @throws IllegalArgumentException 시작일이 종료일보다 늦을 경우 발생.
     */
    @Override
    public List<ArticleRestoreResultDto> restoreArticlesByRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("복구 시작일(from)은 종료일(to)보다 빠르거나 같아야 합니다.");
        }

        List<ArticleRestoreResultDto> result = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            log.info("뉴스 기사 복구 시작 - 날짜 : {}", date);

            // 해당 날짜 백업 데이터 로드
            List<ArticleDto> backup = backupStorage.load(date);

            // 백업 데이터가 없으면 빈 결과 추가 후 다음 날짜로 이동
            if (backup == null || backup.isEmpty()) {
                log.info("복구할 데이터 없음 - 날짜 : {}", date);
                result.add(new ArticleRestoreResultDto(date.atStartOfDay(), List.of(), 0));
                continue;
            }

            // 백업 데이터의 원본 링크 목록 추출
            List<String> originalLinks = backup.stream()
                .map(ArticleDto::sourceUrl)
                .collect(Collectors.toList());

            // DB에 이미 존재하는 원본 링크 조회
            List<String> existingLinks = articlesRepository.findExistingOriginalLinks(originalLinks);

            // DB에 없는 기사만 필터링
            List<ArticleDto> toRestore = backup.stream()
                .filter(dto -> !existingLinks.contains(dto.sourceUrl()))
                .toList();

            // DTO를 엔티티로 변환 후 interest 별도 세팅
            List<Articles> entities = toRestore.stream()
                .map(dto -> {
                    Articles entity = articlesMapper.toEntity(dto);

                    Interest defaultInterest = null;
                    entity.updateInterest(defaultInterest);

                    return entity;
                })
                .collect(Collectors.toList());

            // DB에 복구 대상 기사 저장
            articlesRepository.saveAll(entities);

            // 복구 결과 리스트에 추가
            result.add(new ArticleRestoreResultDto(
                date.atStartOfDay(),
                entities.stream().map(e -> e.getId().toString()).collect(Collectors.toList()),
                entities.size()));

            log.info("뉴스 기사 복구 완료 - 날짜 : {}, 복구 건수 : {}", date, entities.size());
        }

        return result;
    }
}
