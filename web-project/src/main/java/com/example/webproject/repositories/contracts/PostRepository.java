package com.example.webproject.repositories.contracts;

import com.example.webproject.models.Comment;
import com.example.webproject.models.Post;
import com.example.webproject.models.PostFilter;
import com.example.webproject.models.Tag;

import java.util.List;

public interface PostRepository {

    List <Post> getAll(PostFilter filter);
    Post get(int id);
    Post createPost (Post post);
    Post updatePost (Post post);
    void deletePost (Post post);
    List<Post> getMostRecentPosts();
    List<Comment> getPostComments(Post post);
    List<Post> getTenMostCommentedPosts();
    List<Post> getPostsWithTags(Tag tag);
    List<Post> getPaginatedPosts(int page, int postPerPage, PostFilter postFilter);

}
