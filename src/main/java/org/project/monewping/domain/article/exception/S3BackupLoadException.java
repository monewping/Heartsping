package org.project.monewping.domain.article.exception;

// S3에서 백업 데이터를 불러오다가 실패했을 때 발생하는 예외
public class S3BackupLoadException extends RuntimeException {

    public S3BackupLoadException(String key, Throwable cause) {
        super("S3에서 백업 데이터를 불러오는데 실패함 : Key = " + key, cause);
    }
}
