package com.example.webproject.models;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User postCreator;

    @OneToMany
    @JoinTable(name = "posts_comments",
            joinColumns = {@JoinColumn(name = "post_id")})
    private Set<Comment> postComments;

    /*@ManyToMany
    @JoinTable(name = "liked_posts",
            joinColumns = {@JoinColumn(name = "post_id")},
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> usersLikedThePost;

    private int numberOfLikes;

    public Set<User> getUsersLikedPost() {
        return new HashSet<>(usersLikedPost);
    }

    public void setUsersLikedPost(Set<User> usersLikedPost) {
        this.usersLikedPost = usersLikedPost;
    }

    public int getNumberOfLikes() {
        return  getUsersLikedPost().size();*/

    public Post() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getPostCreator() {
        return postCreator;
    }

    public void setPostCreator(User user) {
        this.postCreator = user;
    }

    public Set<Comment> getPostComments() {
        return postComments;
    }

    public void setPostComments(Set<Comment> postComments) {
        this.postComments = postComments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return id == post.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
