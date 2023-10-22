package com.example.webproject.services;

import com.example.webproject.exceptions.EntityNotFoundException;
import com.example.webproject.helpers.ValidationHelper;
import com.example.webproject.models.*;
import com.example.webproject.repositories.CommentRepository;
import com.example.webproject.repositories.PostRepository;
import com.example.webproject.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    public static final String AUTHENTICATION_ERROR = "Only admins or the creator of the post can modify it.";

    @Autowired
    public PostServiceImpl(PostRepository postRepository, CommentRepository commentRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Post> getAll(PostFilter filter) {
        return postRepository.getAll(filter);
    }

    @Override
    public Post get(int id) {
        return postRepository.get(id);
    }
    @Override
    public List<Post> getPostsWithTag(String tag){
        List<Post> tagPosts = postRepository.getAll();
        Tag repoTag = tagRepository.get(tag);
        return tagPosts.stream()
                .filter(post -> post.getTags().stream().anyMatch(tag1 -> tag1.getName().equals(repoTag.getName())))
                .collect(Collectors.toList());
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
    public void createPost(Post post, User user) {
        ValidationHelper.checkIfBanned(user);
        post.setPostCreator(user);
        postRepository.createPost(post);
    }

    @Override
    public void updatePost(Post post, User user) {
        ValidationHelper.validatePostExists(postRepository,post);
        ValidationHelper.validateModifyPermissions(postRepository,post, user);
        ValidationHelper.checkIfBanned(user);
        postRepository.updatePost(post);
    }

    @Override
    public void deletePost(Post post, User user) {
        ValidationHelper.validateModifyPermissions(postRepository,post, user);
        ValidationHelper.checkIfBanned(user);
        postRepository.deletePost(post);
    }
    @Override
    public void likePost(User user, Post post) {
        ValidationHelper.checkIfBanned(user);
        if(!post.getLikes().contains(user)){
            post.likePost(user);
        }else {
            post.dislikePost(user);
        }
        postRepository.updatePost(post);
    }

    @Override
    public void addComment(User user, Post post, Comment comment) {
        ValidationHelper.checkIfBanned(user);
        comment.setPost(post);
        comment.setUser(user);
        commentRepository.createComment(comment);
    }

    @Override
    public void updateComment(User user, Comment comment) {
        ValidationHelper.checkIfBanned(user);
        ValidationHelper.validateCommentExists(commentRepository, comment);
        ValidationHelper.validateModifyPermissions(commentRepository, comment, user);
        Comment commentToModify = commentRepository.get(comment.getId());
        comment.setPost(commentToModify.getPost());
        comment.setUser(user);
        commentRepository.updateComment(comment);
    }

    @Override
    public List<Post> getPostsAsAnonymousUser() {
        return postRepository.getPostsAsAnonymousUser();
    }

    @Override
    public List<Comment> getPostComments(Post post) {
        return postRepository.getPostComments(post);
    }

    public int getLikesCount(Post post) {
        ValidationHelper.validatePostExists(postRepository,post);
        return postRepository.getLikesCount(post);
    }

    /*private void verifyPostExists(Post post) {
        Post existingPost = postRepository.get(post.getId());
        if(existingPost.getId() != post.getId()) {
            throw new EntityNotFoundException("Post", "id", String.valueOf(post.getId()));
        }
    }*/

  /*  private void checkModifyPermissions(Post post, User user) {
        Post postToUpdate = postRepository.get(post.getId());
        if (!(user.isAdmin() || postToUpdate.getPostCreator().equals(user))) {
            throw new AuthorizationException(PostServiceImpl.AUTHENTICATION_ERROR);
        }
    }*/

   /* private void checkIfBanned(User user) {
        if (user.isBlocked()) {
            throw new UserBannedException();
        }
    }*/
}
