package com.example.webproject.models;

import java.util.Optional;

public class PostFilter {

   private Optional<String> title;
   private Optional<String> content;
   private Optional<String> postCreator;
   private Optional<String> sortBy;
   private Optional<String> sortOrder;

    public PostFilter() {
    }

    public PostFilter(String title, String content,
                      String postCreator,String sortBy,String sortOrder) {
    this.title = Optional.ofNullable(title);
    this.content = Optional.ofNullable(content);
    this.postCreator = Optional.ofNullable(postCreator);
    this.sortBy = Optional.ofNullable(sortBy);
    this.sortOrder = Optional.ofNullable(sortOrder);
    }

    public Optional<String> getTitle() {
        return title;
    }

    public Optional<String> getContent() {
        return content;
    }

    public Optional<String> getPostCreator() {
        return postCreator;
    }

    public Optional<String> getSortBy() {
        return sortBy;
    }

    public Optional<String> getSortOrder() {
        return sortOrder;
    }
}
