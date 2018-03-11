package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.models.ArticleReader;

public interface ArticleReaderRepository extends DataRepository<ArticleReader, String> {
    default Class<ArticleReader> getEntityClass() {
        return ArticleReader.class;
    }

    void removeArticlesReaderByArticleId(String articleId);

    void removeArticlesReaderByArticleIdAndReaderId(String articleId, String readerId);
}
