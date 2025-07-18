package org.project.monewping.domain.article.service;

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
import org.project.monewping.domain.article.storage.ArticleBackupStorage;
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
     * 주어진 날짜 범위(from, to)에 대해 일별로 복구 작업을 수행합니다.
     * 각 날짜별 백업 데이터를 로드하고,
     * 현재 DB에 없는 원본 링크의 기사만 필터링하여 저장합니다.
     *
     * @param from 복구 시작일 (포함)
     * @param to 복구 종료일 (포함)
     * @return 복구 결과 목록 (날짜별 복구 결과 포함)
     * @throws IllegalArgumentException 시작일이 종료일보다 늦으면 예외 발생
     */
    @Override
    public List<ArticleRestoreResultDto> restoreArticlesByRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("복구 시작일(from)은 종료일(to)보다 빠르거나 같아야 합니다.");
        }

        List<ArticleRestoreResultDto> result = new ArrayList<>();

        // from부터 to까지 하루씩 증가시키며 복구 작업 수행
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            log.info("뉴스 기사 복구 시작 - 날짜 : {}", date);

            // 백업 저장소에서 해당 날짜 백업 데이터 로드
            List<ArticleDto> backup = backupStorage.load(date);

            // 백업 데이터가 없으면 빈 결과 추가 후 다음 날짜로 이동
            if (backup == null || backup.isEmpty()) {
                log.info("복구할 데이터 없음 - 날짜 : {}", date);
                result.add(new ArticleRestoreResultDto(date.atStartOfDay(), List.of(), 0));
                continue;
            }

            // 백업 데이터에서 원본 링크만 추출
            List<String> originalLinks = backup.stream()
                .map(ArticleDto::sourceUrl)
                .collect(Collectors.toList());

            // DB에 이미 존재하는 원본 링크 목록 조회
            List<String> existingLinks = articlesRepository.findExistingOriginalLinks(originalLinks);

            // DB에 없는(복구 대상) 기사 필터링
            List<ArticleDto> toRestore = backup.stream()
                .filter(dto -> !existingLinks.contains(dto.sourceUrl()))
                .toList();

            // DTO -> 엔티티 변환
            List<Articles> entities = toRestore.stream()
                .map(articlesMapper::toEntity)
                .collect(Collectors.toList());

            // 복구 대상 기사 저장
            articlesRepository.saveAll(entities);

            // 복구 결과 추가
            result.add(new ArticleRestoreResultDto(
                date.atStartOfDay(),
                entities.stream().map(e -> e.getId().toString()).collect(Collectors.toList()),
                entities.size()));

            log.info("뉴스 기사 복구 완료 - 날짜 : {}, 복구 건수 : {}", date, entities.size());
        }

        return result;
    }
}
