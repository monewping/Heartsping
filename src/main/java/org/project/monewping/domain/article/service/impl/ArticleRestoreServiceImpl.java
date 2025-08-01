package org.project.monewping.domain.article.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.exception.InvalidRestoreRangeException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.service.ArticleRestoreService;
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
     * 지정된 날짜 범위(from, to) 내의 백업 데이터를 일별로 로드하여,
     * 현재 DB에 존재하지 않는 뉴스 기사만 필터링 후 저장합니다.
     *
     * @param from 복구 시작일 (포함)
     * @param to 복구 종료일 (포함)
     * @return 날짜별 복구 결과 목록. 각 결과는 복구 일시, 복구된 기사 ID 리스트 및 복구 건수를 포함함.
     * @throws InvalidRestoreRangeException 시작 시각이 종료 시각보다 이후인 경우 발생
     */
    @Override
    public List<ArticleRestoreResultDto> restoreArticlesByRange(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new InvalidRestoreRangeException(from, to);
        }

        List<ArticleRestoreResultDto> result = new ArrayList<>();

        // 시작 | 종료 시각에서 날짜만 추출하여, 일 단위 반복 수행
        LocalDateTime current = from.toLocalDate().atStartOfDay();
        LocalDateTime end = to.toLocalDate().atStartOfDay();

        // 시작일부터 종료일까지 하루씩 반복
        while (!current.isAfter(end)) {
            LocalDate currentDate = current.toLocalDate();
            log.info("뉴스 기사 복구 시작 - 날짜 : {}", currentDate);

            // 해당 날짜에 저장된 백업 기사 목록 로드
            List<ArticleDto> backup = backupStorage.load(currentDate);

            // 백업 데이터가 없으면 빈 결과를 추가하고 다음 날짜로 진행
            if (backup == null || backup.isEmpty()) {
                log.info("복구할 데이터 없음 - 날짜 : {}", currentDate);
                result.add(new ArticleRestoreResultDto(current, List.of(), 0));
            } else {
                // 백업 데이터에서 원본 URL 목록 추출
                List<String> originalLinks = backup.stream()
                    .map(ArticleDto::sourceUrl)
                    .collect(Collectors.toList());

                // DB에서 이미 존재하는 원본 URL 목록 조회
                List<String> existingLinks = articlesRepository.findExistingOriginalLinks(originalLinks);

                // DB에 없는 기사만 필터링 (복구 대상)
                List<ArticleDto> toRestore = backup.stream()
                    .filter(dto -> !existingLinks.contains(dto.sourceUrl()))
                    .toList();

                // 필터링된 DTO를 엔티티로 변환하고 관심사를 세팅 (여기선 null 처리)
                List<Articles> entities = toRestore.stream()
                    .map(dto -> {
                        Articles entity = articlesMapper.toEntity(dto);
                        entity.updateInterest(null);  // 기본 관심사 세팅 (필요시 변경)
                        return entity;
                    })
                    .collect(Collectors.toList());

                // DB에 복구 대상 기사 저장
                articlesRepository.saveAll(entities);

                // 복구 결과 객체를 만들어 결과 리스트에 추가
                result.add(new ArticleRestoreResultDto(
                    current,
                    entities.stream().map(e -> e.getId().toString()).toList(),
                    entities.size()));

                log.info("뉴스 기사 복구 완료 - 날짜 : {}, 복구 건수 : {}", currentDate, entities.size());
            }

            // 현재 날짜를 하루 더해 다음 날짜로 이동
            current = current.plusDays(1);
        }

        // 날짜별 복구 결과 목록 반환
        return result;
    }
}