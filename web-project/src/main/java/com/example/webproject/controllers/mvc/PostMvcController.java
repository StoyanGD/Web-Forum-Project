package com.example.webproject.controllers.mvc;

import com.example.webproject.dtos.CommentDto;
import com.example.webproject.dtos.PostDto;
import com.example.webproject.dtos.PostFilterDto;
import com.example.webproject.dtos.mvcDtos.SingletonCommentDto;
import com.example.webproject.exceptions.AuthorizationException;
import com.example.webproject.exceptions.EntityDuplicateException;
import com.example.webproject.exceptions.EntityNotFoundException;
import com.example.webproject.exceptions.UserBannedException;
import com.example.webproject.helpers.AuthenticationHelper;
import com.example.webproject.helpers.CommentMapper;
import com.example.webproject.helpers.PostMapper;
import com.example.webproject.models.Comment;
import com.example.webproject.models.Post;
import com.example.webproject.models.PostFilter;
import com.example.webproject.models.User;
import com.example.webproject.services.contracts.CommentService;
import com.example.webproject.services.contracts.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/posts")
public class PostMvcController {

    private static final int DEFAULT_PAGE_SIZE = 5;
    private final AuthenticationHelper authenticationHelper;
    private final PostMapper postMapper;
    private final PostService postService;
    private final CommentMapper commentMapper;
    private final CommentService commentService;
    private final SingletonCommentDto singletonCommentDto;

    @Autowired
    public PostMvcController(AuthenticationHelper authenticationHelper,
                             PostMapper postMapper,
                             PostService postService,
                             CommentMapper commentMapper, CommentService commentService, SingletonCommentDto singletonCommentDto) {
        this.authenticationHelper = authenticationHelper;
        this.postMapper = postMapper;
        this.postService = postService;
        this.commentMapper = commentMapper;
        this.commentService = commentService;
        this.singletonCommentDto = singletonCommentDto;
    }

    @ModelAttribute("isAuthenticated")
    public boolean populateIsAuthenticated(HttpSession session) {

        return session.getAttribute("currentUser") != null;
    }
    @ModelAttribute("user")
    public User populateUser(HttpSession session){
       return authenticationHelper.tryPopulateUser(session);
    }
    
    @GetMapping
    public String getPaginationPage(@RequestParam(value = "page", required = false) Integer page,
                                    Model model,
                                    @Valid @ModelAttribute("postFilter") PostFilterDto filterDto) {
        PostFilter postFilter = new PostFilter(
                filterDto.getTitle(),
                filterDto.getContent(),
                filterDto.getSortBy(),
                filterDto.getSortOrder()
        );
        if (page == null) {
            page = 1;
        }
        List<Post> dataList = postService.getPaginatedPosts(page, DEFAULT_PAGE_SIZE);

        int totalItems = postService.getAll(postFilter).size();
        int totalPages = (int) Math.ceil((double) totalItems / DEFAULT_PAGE_SIZE);

        model.addAttribute("posts", dataList);
        model.addAttribute("postService", postService);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("postFilter", filterDto);
        return "PostsView";
    }

    @GetMapping("/{id}")
    public String getPost(@ModelAttribute SingletonCommentDto singletonCommentDto, @PathVariable int id, Model model) {

        try {
            Post post = postService.get(id);
            model.addAttribute("comment", singletonCommentDto);
            model.addAttribute("post", post);
            model.addAttribute("postComments", postService.getPostComments(post));
            return "SinglePostView";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 404);
            return "ErrorView";
        }
    }

    @GetMapping("/tags")
    public String getPostsWithTag(@Valid @ModelAttribute("filterOptions") PostFilterDto filterDto,
                                  @ModelAttribute("tag") String tag,
                                  Model model) {
        PostFilter postFilter = new PostFilter(
                filterDto.getTitle(),
                filterDto.getContent(),
                filterDto.getSortBy(),
                filterDto.getSortOrder()
        );
        model.addAttribute("posts", postService.getPostsWithTag(tag));
        model.addAttribute("filterOptions", postService.getAll(postFilter));
        return "PostsView";
    }

    @GetMapping("/create")
    public String showCreatePostView(Model model, HttpSession session) {
        try {
            authenticationHelper.tryGetCurrentUser(session);
        } catch (AuthorizationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 401);
            return "Not-Found";
        }
        model.addAttribute("post", new PostDto());
        return "Post-New";
    }
    @GetMapping("{id}/like")
    public String likePost(HttpSession session, Model model, @PathVariable int id) {
        try {

            User loggedUser = authenticationHelper.tryGetCurrentUser(session);
            Post postToLike = postService.get(id);
            postService.likePost(loggedUser, postToLike);
            return "redirect:/posts/{id}";
        } catch (AuthorizationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 401);
            return "ErrorView";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 404);
            return "ErrorView";
        }
    }

    @GetMapping("{id}/dislike")
    public String dislikePost(HttpSession session, Model model, @PathVariable int id) {
        try {
            User loggedUser = authenticationHelper.tryGetCurrentUser(session);
            Post postToLike = postService.get(id);
            postService.dislikePost(loggedUser, postToLike);
            return "redirect:/posts/{id}";
        } catch (AuthorizationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 401);
            return "ErrorView";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 404);
            return "ErrorView";
        }
    }

    @PostMapping()
    public String createPost(@Valid @ModelAttribute("post") PostDto postDto,
                             BindingResult errors,
                             Model model,
                             HttpSession session) {
        User user;
        try {
            user = authenticationHelper.tryGetCurrentUser(session);
        } catch (AuthorizationException e) {
            return "redirect:/auth/login";
        }
        if (errors.hasErrors()) {
            return "Post-New";
        }

        try {
            Post postToCreate = postMapper.fromDto(postDto);
            postService.createPost(postToCreate, user);
            return "redirect:/posts";
        } catch (EntityDuplicateException | UserBannedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 409);
            return "ErrorView";
        }
    }

    @GetMapping("/{id}/delete")
    public String deletePost(@PathVariable int id, Model model, HttpSession session) {

        User user;
        try {
            user = authenticationHelper.tryGetCurrentUser(session);
        } catch (AuthorizationException e) {
            return "redirect:/auth/login";
        }

        try {
            postService.deletePost(postService.get(id), user);
            return "redirect:/posts";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 404);
            return "ErrorView";
        } catch (UserBannedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 409);
            return "ErrorView";
        }
    }
    @PostMapping("/{id}/comment")
    public String addCommentToPost(@ModelAttribute("comment") @Valid CommentDto commentDto,BindingResult bindingResult, @PathVariable int id, Model model, HttpSession session) {
        User user;
        if(bindingResult.hasErrors()){
            return "ErrorView";
        }
        try {
            user = authenticationHelper.tryGetCurrentUser(session);
        } catch (AuthorizationException e) {
            return "redirect:/auth/login";
        }
        try {
            Post post = postService.get(id);
            Comment comment = commentMapper.fromDto(commentDto);
            postService.addComment(user, post, comment);
            return "redirect:/posts/{id}";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 404);
            return "ErrorView";
        } catch (UserBannedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 409);
            return "ErrorView";
        } catch (AuthorizationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 401);
            return "ErrorView";
        }

    }
    @GetMapping("{postId}/comment/{id}/update")
    public String updatePostComment(@PathVariable int postId, @ModelAttribute("comment") @Valid CommentDto commentDto, BindingResult bindingResult, @PathVariable int id, HttpSession session, Model model) {
        if(bindingResult.hasErrors()){
            return "ErrorView";
        }
        try {
            User user = authenticationHelper.tryGetCurrentUser(session);
            Comment comment = commentMapper.fromDto(commentDto);
            comment.setId(id);
            commentService.updateComment(comment, user, id);
            model.addAttribute("postId",postId);
            return "redirect:/posts/{postId}";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 404);
            return "ErrorView";
        } catch (AuthorizationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 401);
            return "ErrorView";
        } catch (UserBannedException e) {
            return "ErrorView";
        }
    }
    @GetMapping("{postId}/comment/{id}/delete")
    public String deletePostComment(@PathVariable int postId,@PathVariable int id, HttpSession session, Model model) {

        try {
            User user = authenticationHelper.tryGetCurrentUser(session);
            commentService.deleteComment(user, id);
            model.addAttribute("postId", postId);
            return "redirect:/posts/{postId}";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 404);
            return "ErrorView";
        } catch (AuthorizationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statusCode", 401);
            return "ErrorView";
        } catch (UserBannedException e){
            return "ErrorView";
        }
    }
}