package org.project.monewping.domain.interest.repository;

import org.project.monewping.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** 관심사 엔티티의 데이터 접근을 담당하는 레포지토리. */
@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID> {

    /** 관심사 이름으로 조회한다.
     * @param name 관심사 이름
     * @return 조회된 관심사 (Optional)
     */
    Optional<Interest> findByName(String name);
    /** 지정된 이름의 관심사가 존재하는지 확인한다.
     * @param name 관심사 이름
     * @return 존재하면 true
     */
    boolean existsByName(String name);
} 