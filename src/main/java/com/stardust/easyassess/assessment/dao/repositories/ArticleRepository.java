package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.models.Article;

public interface ArticleRepository extends DataRepository<Article, String> {
    default Class<Article> getEntityClass() {
        return Article.class;
    }
}
