package com.example.webproject.services;

import com.example.webproject.exceptions.EntityNotFoundException;
import com.example.webproject.helpers.ValidationHelper;
import com.example.webproject.models.*;
import com.example.webproject.repositories.contracts.CommentRepository;
import com.example.webproject.repositories.contracts.PostRepository;
import com.example.webproject.repositories.contracts.TagRepository;
import com.example.webproject.services.contracts.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    public static final String AUTHENTICATION_ERROR = "Only admins or the creator of the post can modify it.";

    @Autowired
    public PostServiceImpl(PostRepository postRepository,TagRepository tagRepository) {
        this.postRepository = postRepository;

        this.tagRepository = tagRepository;
    }
    @Override
    public List<Post> getAll(PostFilter filter) {
        return postRepository.getAll(filter);
    }
    @Override
    public List<Post> getTenMostCommentedPosts() {
        return postRepository.getTenMostCommentedPosts();
    }
    @Override
    public List<Post> getPaginatedPosts(int page, int postsPerPage) {
        return postRepository.getPaginatedPosts(page,postsPerPage);
    }
    @Override
    public List<Post> getMostRecentPosts() {
        return postRepository.getMostRecentPosts();
    }
    @Override
    public Post get(int id) {
        return postRepository.get(id);
    }
    @Override
    public List<Post> getPostsWithTag(String tag){
        Tag repoTag = tagRepository.get(tag);
        return postRepository.getPostsWithTags(repoTag);
    }
    @Override
    public List<Comment> getPostComments(Post post) {
        return postRepository.getPostComments(post);
    }
    @Override
    public void createPost(Post post, User user) {
        ValidationHelper.checkIfBanned(user);
        post.setPostCreator(user);
        postRepository.createPost(post);
    }
    @Override
    public void updatePost(Post post, User user) {
        ValidationHelper.validateModifyPermissions(postRepository,post, user);
        ValidationHelper.checkIfBanned(user);
        Post existingPost = postRepository.get(post.getId());
        verifyPostFields(post, existingPost);
        postRepository.updatePost(post);
    }
    @Override
    public void deletePost(Post post, User user) {
        ValidationHelper.validateModifyPermissions(postRepository,post, user);
        ValidationHelper.checkIfBanned(user);
        postRepository.deletePost(post);
    }

    @Override
    public void addTagToPost(Post post, Tag tag, User loggedUser){
        ValidationHelper.checkIfBanned(loggedUser);
        ValidationHelper.validateModifyPermissions(postRepository,post,loggedUser);
        try {
            Tag repoTag = tagRepository.get(tag.getName().toLowerCase());
            post.getTags().add(repoTag);
            postRepository.updatePost(post);
        } catch (EntityNotFoundException e){
            tagRepository.createTag(tag);
            post.getTags().add(tag);
            postRepository.updatePost(post);
        }
    }
    @Override
    public void deleteTagFromPost(Post post, User loggedUser, Tag tag) {
        ValidationHelper.checkIfBanned(loggedUser);
        ValidationHelper.validateModifyPermissions(postRepository,post,loggedUser);
        Tag repoTag = tagRepository.get(tag.getName());
        post.getTags().remove(repoTag);
        postRepository.updatePost(post);
    }

    @Override
    public void likePost(User user, Post post) {
        ValidationHelper.checkIfBanned(user);
        if(!post.getLikes().contains(user)) {
            post.likePost(user);
        } else {
            post.dislikePost(user);
        }
        postRepository.updatePost(post);
    }
    private static void verifyPostFields(Post post, Post existingPost) {
        if(post.getTitle() == null || post.getTitle().isBlank()){
            post.setTitle(existingPost.getTitle());
        }
        if(post.getContent() == null || post.getContent().isBlank()){
            post.setContent(existingPost.getContent());
        }
    }
}
