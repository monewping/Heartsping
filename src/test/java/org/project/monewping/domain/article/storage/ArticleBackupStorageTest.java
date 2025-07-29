package org.project.monewping.domain.article.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.global.config.S3Properties;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("BackupStorage 테스트")
public class ArticleBackupStorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Properties s3Properties;

    @Mock
    private S3Properties.Backup backupProps;

    private ObjectMapper objectMapper;
    private S3ArticleBackupStorage backupStorage;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        when(s3Properties.backup()).thenReturn(backupProps);
        when(backupProps.bucketName()).thenReturn("test-bucket");
        when(backupProps.baseDirectory()).thenReturn("backup/articles");

        backupStorage = new S3ArticleBackupStorage(s3Client, s3Properties, objectMapper);
    }

    @Test
    @DisplayName("save 메서드 호출 시 정상적으로 S3에 저장이 수행되어야 한다")
    void save_shouldUploadJsonToS3() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 7, 24);
        List<ArticleDto> articlesToSave = List.of(
            new ArticleDto(
                UUID.randomUUID(), "중앙일보", "http://source1", "title1",
                LocalDateTime.of(2025,7,24,12,0), "summary1", 10L, 100L, false),
            new ArticleDto(
                UUID.randomUUID(), "조선일보", "http://source2", "title2",
                LocalDateTime.of(2025,7,24,15,30), "summary2", 5L, 200L, true)
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        // when & then
        assertDoesNotThrow(() -> backupStorage.save(date, articlesToSave));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    @DisplayName("load 메서드는 S3에서 JSON 데이터를 읽어 ArticleDto 리스트로 반환해야 한다")
    void load_shouldReturnArticleDtoList_whenS3ObjectExists() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 7, 24);
        List<ArticleDto> articlesToLoad = List.of(
            new ArticleDto(UUID.randomUUID(), "중앙일보", "http://source1", "title1", LocalDateTime.of(2025,7,24,12,0), "summary1", 10L, 100L, false),
            new ArticleDto(UUID.randomUUID(), "조선일보", "http://source2", "title2", LocalDateTime.of(2025,7,24,15,30), "summary2", 5L, 200L, true)
        );

        byte[] jsonBytes = objectMapper.writeValueAsBytes(articlesToLoad);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonBytes);
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> responseInputStream =
            new ResponseInputStream<>(getObjectResponse, inputStream);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        // when
        List<ArticleDto> loadedArticles = backupStorage.load(date);

        // then
        assertEquals(2, loadedArticles.size());
        assertEquals("http://source1", loadedArticles.get(0).sourceUrl());
        assertEquals("title2", loadedArticles.get(1).title());
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("load 메서드는 S3에 해당 키가 없으면 빈 리스트를 반환해야 한다")
    void load_shouldReturnEmptyList_whenNoSuchKeyExceptionThrown() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 24);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(
            NoSuchKeyException.builder().build());

        // when
        List<ArticleDto> loadedArticles = backupStorage.load(date);

        // then
        assertNotNull(loadedArticles);
        assertTrue(loadedArticles.isEmpty());
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("save 메서드는 S3 putObject 호출 실패 시 예외를 던져야 한다")
    void save_shouldThrowException_whenPutObjectFails() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 24);
        List<ArticleDto> emptyList = List.of();

        doThrow(RuntimeException.class).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // when & then
        assertThrows(Exception.class, () -> backupStorage.save(date, emptyList));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}