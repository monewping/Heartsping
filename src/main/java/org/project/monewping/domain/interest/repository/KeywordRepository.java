package org.project.monewping.domain.interest.repository;

import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.interest.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordRepository extends JpaRepository<Keyword, UUID> {

    @Query("SELECT k.name FROM Keyword k WHERE k.interest.id = :interestId")
    List<String> findNamesByInterestId(UUID interestId);

}
