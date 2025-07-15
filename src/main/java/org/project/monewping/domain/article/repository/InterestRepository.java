package org.project.monewping.domain.article.repository;

import java.util.UUID;
import org.project.monewping.domain.article.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

}
