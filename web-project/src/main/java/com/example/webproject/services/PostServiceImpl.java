package com.example.webproject.services;

import com.example.webproject.exceptions.AuthorizationException;
import com.example.webproject.exceptions.EntityDuplicateException;
import com.example.webproject.exceptions.EntityNotFoundException;
import com.example.webproject.exceptions.UserBannedException;
import com.example.webproject.models.Post;
import com.example.webproject.models.User;
import com.example.webproject.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    public static final String AUTHENTICATION_ERROR = "Only admins or the creator of the post can modify it.";

    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<Post> getAll() {
        return postRepository.getAll();
    }

    @Override
    public Post get(int id) {
        return postRepository.get(id);
    }

    @Override
    public void createPost(Post post, User user) {
        checkIfBanned(user);
        post.setPostCreator(user);
        postRepository.createPost(post);
    }

    @Override
    public void updatePost(Post post, User user) {
        checkModifyPermissions(post, user);
        checkIfBanned(user);

        boolean postExist =true;
        try {
            Post existingPost = postRepository.get(post.getId());
            if (existingPost.getId() == post.getId()) {
                postExist = false;
            }
        } catch (EntityNotFoundException e) {
            postExist = false;
        }

        if (postExist) {
            throw new EntityDuplicateException("Post", "id", String.valueOf(post.getId()));
        }

        postRepository.updatePost(post);
    }

    @Override
    public void deletePost(Post post, User user) {
        checkModifyPermissions(post, user);
        checkIfBanned(user);
        postRepository.deletePost(post);
    }

    private void checkModifyPermissions(Post post, User user) {
        Post postToUpdate = postRepository.get(post.getId());
        if (!(user.isAdmin() || postToUpdate.getPostCreator().equals(user))) {
            throw new AuthorizationException(PostServiceImpl.AUTHENTICATION_ERROR);
        }
    }

    private void checkIfBanned(User user) {
        if (user.isBlocked()) {
            throw new UserBannedException();
        }
    }
}
