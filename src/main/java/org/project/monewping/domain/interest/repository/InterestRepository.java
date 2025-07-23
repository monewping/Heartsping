package org.project.monewping.domain.interest.repository;

import org.project.monewping.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 관심사 엔티티의 데이터 접근을 담당하는 레포지토리입니다.
 *
 * <p>JPA를 통해 관심사 관련 CRUD 및
 * 이름 기반 조회 기능을 제공합니다.</p>
 */
@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

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
    
    /** 모든 관심사 이름을 조회한다.
     * @return 모든 관심사 이름 리스트
     */
    @Query("SELECT i.name FROM Interest i")
    List<String> findAllNames();

    @Query("SELECT i.name FROM Interest i " +
            "WHERE i.name LIKE %:searchName% OR :searchName LIKE CONCAT('%', i.name, '%')")
    List<String> findNamesByRoughMatch(@Param("searchName") String searchName);
} 