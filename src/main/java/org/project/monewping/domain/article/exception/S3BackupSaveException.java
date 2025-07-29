package org.project.monewping.domain.article.exception;

// S3에 백업 데이터 저장 중 실패 시 발생 예외
public class S3BackupSaveException extends RuntimeException {

    public S3BackupSaveException(String key, Throwable cause) {
        super("S3에 백업 데이터 저장 실패 : key = " + key, cause);
    }
}
