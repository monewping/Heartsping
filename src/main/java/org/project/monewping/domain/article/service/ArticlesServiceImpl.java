package org.project.monewping.domain.article.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.domain.article.exception.DuplicateArticleException;
import org.project.monewping.domain.article.exception.InterestNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.repository.InterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticlesServiceImpl implements ArticlesService {

    private final ArticlesRepository articlesRepository;
    private final InterestRepository interestRepository;
    private final ArticlesMapper articlesMapper;

    @Override
    public void save(ArticleSaveRequest request) {

        if (articlesRepository.existsByOriginalLink(request.originalLink())) {
            throw new DuplicateArticleException(request.originalLink());
        }

        Interest interest = interestRepository.findById(request.interestId())
            .orElseThrow(() -> new InterestNotFoundException(request.interestId()));

        Articles article = articlesMapper.toEntity(request, interest);
        articlesRepository.save(article);
    }

    @Override
    public void saveAll(List<ArticleSaveRequest> requests) {

        if (requests == null || requests.isEmpty()) return;

        UUID interestId = requests.get(0).interestId();
        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));

        for (ArticleSaveRequest request : requests) {
            if (articlesRepository.existsByOriginalLink(request.originalLink())) {
                continue;
            }

            Articles article = articlesMapper.toEntity(request, interest);
            articlesRepository.save(article);
        }

    }
}
