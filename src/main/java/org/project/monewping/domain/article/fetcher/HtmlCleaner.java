package org.project.monewping.domain.article.fetcher;

/**
 * HTML 문자열 내의 태그 및 일부 HTML 엔티티를 제거하고,
 * 클린한 텍스트만 반환하는 유틸리티 클래스입니다.
 */
public class HtmlCleaner {

    /**
     * 입력 문자열에서 HTML 태그를 제거하고,
     * 일부 HTML 엔티티( &quot;, &apos;, &lt;, &gt;, &amp; )를 대응하는 문자로 변환합니다.
     *
     * @param input HTML 문자열 ( null일 수 있음 )
     * @return 태그와 엔티티가 제거된 클린한 텍스트, 입력이 null인 경우 빈 문자열 반환
     */
    public static String strip(String input) {
        if (input == null) return "";
        return input
            .replaceAll("<[^>]*>", "")
            .replaceAll("&quot;", "\"")
            .replaceAll("&apos;", "'")
            .replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">")
            .replaceAll("&amp;", "&");
    }

}
