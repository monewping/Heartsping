package org.project.monewping.domain.article.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.exception.S3BackupLoadException;
import org.project.monewping.domain.article.exception.S3BackupSaveException;
import org.project.monewping.global.config.S3Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * AWS S3를 활용하여 뉴스 기사 데이터를 백업 및 복원하는 저장소 구현체입니다.
 *
 * <p>날짜별로 JSON 파일을 S3 버킷 내에 저장하며,
 * 백업 및 복구 시 해당 날짜 기준 파일을 읽고 쓰는 기능을 제공합니다.</p>
 *
 * <p>파일명 패턴은 "articles-YYYY-MM-DD.json" 형식이며,
 * 필요에 따라 {@code baseDirectory}를 접두사로 사용합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.s3.backup.enabled", havingValue = "true")
public class S3ArticleBackupStorage implements ArticleBackupStorage {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final ObjectMapper objectMapper;

    /**
     * 지정된 날짜에 해당하는 뉴스 기사 백업 데이터를 S3에서 로드합니다.
     *
     * @param date 백업 파일의 날짜 (예: 2025-07-23)
     * @return 해당 날짜에 저장된 뉴스 기사 리스트,
     *         백업 파일이 없으면 빈 리스트를 반환합니다.
     * @throws S3BackupLoadException S3에서 데이터를 읽는 도중 오류 발생 시
     */
    @Override
    public List<ArticleDto> load(LocalDate date) {
        String key = buildKey(date);

        try {
            // S3 객체 요청 생성
            var getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.backup().bucketName())
                .key(key)
                .build();

            // S3에서 객체를 스트림으로 읽어 List<ArticleDto> -> JSON 역직렬화
            try (var s3Object = s3Client.getObject(getObjectRequest)) {
                return objectMapper.readValue(
                    s3Object,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ArticleDto.class)
                );
            }
        } catch (NoSuchKeyException e) {
            // 해당 키의 파일이 존재하지 않는 경우, 빈 리스트 반환
            log.info("S3 백업 파일이 존재하지 않습니다 : {}", key);
                return List.of();
        } catch (Exception e) {
            // S3 데이터를 읽는 도중 오류 발생 시 커스텀 예외 발생
            log.error("S3 백업 파일 로드 실패 : {}", key, e);
            throw new S3BackupLoadException(key, e);
        }
    }

    /**
     * 지정된 날짜에 뉴스 기사 목록을 JSON으로 직렬화하여 S3에 저장합니다.
     *
     * @param date 백업 파일의 날짜 ( ex: 2025-07-23 )
     * @param articles 저장할 뉴스 기사 목록
     * @throws S3BackupSaveException S3 업로드 실패 또는 Json 직렬화 오류 발생 시
     */
    @Override
    public void save(LocalDate date, List<ArticleDto> articles) {
        String key = buildKey(date);

        try {
            // S3 업로드 요청 생성
            var putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.backup().bucketName())
                .key(key)
                .build();

            // ArticleDto 리스트를 Json 바이트 배열로 직렬화
            byte[] jsonBytes = objectMapper.writeValueAsBytes(articles);

            // S3에 Json 파일 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonBytes));

            log.info("뉴스 기사 데이터 S3 백업 완료 : {}", key);
        } catch (Exception e) {
            // 업로드 또는 직렬화 실패 시 커스텀 예외 발생
            log.error("S3 백업 파일 저장 실패 : {}", key, e);
            throw new S3BackupSaveException(key, e);
        }
    }


    /**
     * 날짜를 기준으로 S3 객체 키 생성.
     * 형식: baseDirectory/articles-YYYY-MM-DD.json
     *
     * @param date 기준 날짜
     * @return S3 저장 키 문자열
     */
    private String buildKey(LocalDate date) {

        String baseDirectory = s3Properties.backup().baseDirectory();

        // 접두사가 없다면 빈 문자열 처리
        if (baseDirectory == null || baseDirectory.isBlank()) baseDirectory = "";

        // 슬래시 누락 방지
        if (!baseDirectory.isBlank() && !baseDirectory.endsWith("/")) baseDirectory += "/";

        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return baseDirectory + "articles-" + dateStr + ".json";
    }
}
