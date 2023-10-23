package com.example.webproject.services;
import com.example.webproject.exceptions.AuthorizationException;
import com.example.webproject.exceptions.EntityDuplicateException;
import com.example.webproject.exceptions.EntityNotFoundException;
import com.example.webproject.exceptions.UserBannedException;
import com.example.webproject.models.User;
import com.example.webproject.repositories.CommentRepository;
import com.example.webproject.repositories.PostRepository;
import com.example.webproject.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.example.webproject.Helpers.createMockUser;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTests {

    @Mock
    private UserRepository repository;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void getById_Should_Return_User_WhenMatchExists(){
        User user = createMockUser();

        Mockito.when(repository.getById(Mockito.anyInt()))
                .thenReturn(user);

        User testUser = userService.getById(user.getId());

        Assertions.assertEquals(user,testUser);
    }

    @Test
    public void getById_Should_Throw_WhenNoMatch(){
        User user = createMockUser();
        Mockito.when(repository.getById(user.getId()))
                .thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(EntityNotFoundException.class,()->userService.getById(user.getId()));

        Mockito.verify(repository,Mockito.times(1)).getById(user.getId());
    }

    @Test
    public void create_Not_Call_Repository_When_Username_Exists() {
        User user = createMockUser();

        Mockito.when(repository.getByUsername(user.getUsername()))
                .thenReturn(user);
        Mockito.when(repository.getByEmail(user.getEmail()))
                .thenThrow(EntityNotFoundException.class);

        assertThrows(EntityDuplicateException.class,
                () -> userService.createUser(user));

        Mockito.verify(repository, Mockito.times(0)).createUser(user);
    }
    @Test
    public void create_Not_Call_Repository_When_Email_Exists() {
        User user = createMockUser();

        Mockito.when(repository.getByUsername(user.getUsername()))
                .thenThrow(EntityNotFoundException.class);
        Mockito.when(repository.getByEmail(user.getEmail()))
                .thenReturn(user);

        assertThrows(EntityDuplicateException.class,
                () -> userService.createUser(user));

        Mockito.verify(repository, Mockito.times(0)).createUser(user);
    }
    @Test
    public void create_Should_Call_Repository_When_PassedValidDetails() {
        User user = createMockUser();

        Mockito.when(repository.getByUsername(user.getUsername()))
                .thenThrow(EntityNotFoundException.class);
        Mockito.when(repository.getByEmail(user.getEmail()))
                .thenThrow(EntityNotFoundException.class);

        userService.createUser(user);

        Mockito.verify(repository, Mockito.times(1)).createUser(user);
    }
    @Test
    public void deleteUser_Should_Throw_When_NonAdmin_User_Tries_To_Delete_Other_User() {
        User mockUser = createMockUser();

        User userToBeDeleted = createMockUser();

        userToBeDeleted.setUsername("otherUsername");

        Assertions.assertThrows(AuthorizationException.class,
                ()->userService.deleteUser(mockUser,userToBeDeleted));

    }
    @Test
    public void deleteUser_Should_Call_Repository_When_NonAdmin_User_Deletes_Themselves() {
        User mockUser = createMockUser();

        User userToBeDeleted = createMockUser();

        userService.deleteUser(mockUser,userToBeDeleted);

        Mockito.verify(repository, Mockito.times(1)).deleteUser(userToBeDeleted);
    }
    @Test
    public void deleteUser_Should_Call_Repository_When_Admin_User_Delete_Other_User(){
        User adminUser = createMockUser();
        adminUser.setAdmin(true);
        User otherUser = createMockUser();
        otherUser.setUsername("otherUser");

        userService.deleteUser(adminUser,otherUser);

        Mockito.verify(repository, Mockito.times(1)).deleteUser(otherUser);
    }

    @Test
    public void get_User_Posts_Should_Call_Repository_IfLoggedUser_Is_Not_Banned(){
        User loggedUser = createMockUser();

        userService.getUserPosts(loggedUser,repository.getById(Mockito.anyInt()));

        Mockito.verify(repository, Mockito.times(1))
                .getUserPosts(repository.getById(Mockito.anyInt()));
    }
    @Test
    public void get_User_Posts_Should_Throw_When_LoggedUser_Is_Banned(){
        User loggedUser = createMockUser();
        loggedUser.setBlocked(true);
        User userToCheckPosts = createMockUser();

        Assertions.assertThrows(UserBannedException.class,
                ()->userService.getUserPosts(loggedUser,userToCheckPosts));

        Mockito.verify(repository, Mockito.times(0))
                .getUserPosts(repository.getById(Mockito.anyInt()));
    }
    @Test
    public void change_Ban_Status_Should_Ban_User(){
        User mockUser = createMockUser();

        userService.changeBanStatus(mockUser);

        Assertions.assertTrue(mockUser.isBlocked());
    }
    @Test
    public void change_Admin_Status_Should_Make_User_Admin(){
        User mockUser = createMockUser();

        userService.changeAdminStatus(mockUser);

        Assertions.assertTrue(mockUser.isAdmin());
    }
}

