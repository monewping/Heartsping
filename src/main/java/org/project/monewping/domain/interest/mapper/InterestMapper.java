package org.project.monewping.domain.interest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;

import java.util.List;
import java.util.stream.Collectors;

/** 관심사 도메인 객체 변환을 담당하는 매퍼. */
@Mapper(componentModel = "spring")
public interface InterestMapper {

    /** Interest 엔티티를 DTO로 변환한다.
     * @param entity Interest 엔티티
     * @return InterestDto
     */
    @Mapping(target = "keywords", source = "keywords", qualifiedByName = "keywordsToStrings")
    @Mapping(target = "subscribedByMe", constant = "false")
    InterestDto toDto(Interest entity);

    /**
     * Keyword 엔티티 리스트를 String 리스트로 변환합니다.
     *
     * <p>각 Keyword 엔티티의 name 필드를 추출해 반환합니다.</p>
     * @param keywords Keyword 엔티티 리스트
     * @return String 리스트
     */
    @Named("keywordsToStrings")
    default List<String> keywordsToStrings(List<Keyword> keywords) {
        if (keywords == null) {
            return List.of();
        }
        return keywords.stream()
                .map(Keyword::getName)
                .collect(Collectors.toList());
    }
} 