package com.stardust.easyassess.assessment.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "readers")
public class ArticleReader extends DataModel {
    @Id
    private String id;

    @DBRef
    private Article article;

    private String readerId;

    private boolean hasBeenRead;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArticleReader() {
    }

    public ArticleReader(Article article, String readerId) {
        this.article = article;
        this.readerId = readerId;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public boolean hasBeenRead() {
        return hasBeenRead;
    }

    public void setHasBeenRead(boolean hasBeenRead) {
        this.hasBeenRead = hasBeenRead;
    }

    public String getSubject() {
        return article.getSubject();
    }

    @JsonFormat(pattern="yyyy-MM-dd")
    public Date getArticleDate() {
        return article.getDate();
    }

    public String getAuthorName() {
        return article.getAuthorName();
    }
}
