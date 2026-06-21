package com.uni.backend.service;

import com.uni.backend.entity.*;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role defaultRole;

    @BeforeEach
    void setUp() {
        defaultRole = new Role();
        defaultRole.setId(1L);
        defaultRole.setName("USER");

        UserDetails testUserDetails = new UserDetails();
        testUserDetails.setId(1L);
        testUserDetails.setFirstName("John");
        testUserDetails.setLastName("Doe");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("rawPassword");
        testUser.setUserDetails(testUserDetails);
    }

    @Test
    void createUser_ShouldAssignDefaultRole_WhenNoRolesProvided() {
        // arrange
        testUser.setRoles(null); // simulate form submitting no roles
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(defaultRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // act
        User savedUser = userService.createUser(testUser);

        // assert
        assertNotNull(savedUser);
        assertEquals("encodedPassword", testUser.getPassword());
        assertTrue(testUser.getRoles().contains(defaultRole)); // Verifies default role was added
        assertEquals(testUser, testUser.getUserDetails().getUser()); // Verifies UserDetails sync
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldNotAssignDefaultRole_WhenRolesAlreadyExist() {
        // arrange
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        testUser.setRoles(Set.of(adminRole)); // user already has a role

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // act
        userService.createUser(testUser);

        // assert
        verify(roleRepository, never()).findByName(anyString()); // Shouldn't fetch default role
        assertTrue(testUser.getRoles().contains(adminRole));
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User user = userService.getUserById(1L);

        assertNotNull(user);
        assertEquals("johndoe", user.getUsername());
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(2L));
    }

    @Test
    void updateUser_ShouldUpdateBasicInfoAndIgnoreEmptyPassword() {
        // arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User updateRequest = new User();
        updateRequest.setUsername("newUsername");
        updateRequest.setEmail("new@example.com");
        updateRequest.setPassword(""); // empty password should be ignored

        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // act
        User updatedUser = userService.updateUser(updateRequest, 1L);

        // assert
        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("new@example.com", updatedUser.getEmail());
        verify(passwordEncoder, never()).encode(anyString()); // Verifies password was NOT encoded/changed
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldEncodeAndSetNewPassword_WhenPasswordIsProvided() {
        // arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User updateRequest = new User();
        updateRequest.setUsername("johndoe");
        updateRequest.setEmail("john@example.com");
        updateRequest.setPassword("newRawPassword"); // valid new password

        when(passwordEncoder.encode("newRawPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // act
        User updatedUser = userService.updateUser(updateRequest, 1L);

        // assert
        assertEquals("newEncodedPassword", updatedUser.getPassword());
        verify(passwordEncoder, times(1)).encode("newRawPassword");
    }

    @Test
    void updateUser_ShouldCreateNewUserDetails_WhenExistingUserHasNone() {
        // arrange
        testUser.setUserDetails(null); // simulate a user that was created without details
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDetails newDetails = new UserDetails();
        newDetails.setFirstName("Jane");
        newDetails.setLastName("Smith");

        User updateRequest = new User();
        updateRequest.setUsername("johndoe");
        updateRequest.setEmail("john@example.com");
        updateRequest.setUserDetails(newDetails);

        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // act
        User updatedUser = userService.updateUser(updateRequest, 1L);

        // assert
        assertNotNull(updatedUser.getUserDetails());
        assertEquals("Jane", updatedUser.getUserDetails().getFirstName());
        assertEquals(testUser, updatedUser.getUserDetails().getUser()); // verifies the bidirectional sync
    }

    @Test
    void deleteUser_ShouldDelete_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenNotExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(2L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void findPaginated_ShouldReturnUserPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<User> result = userService.findPaginated(1, 10, "username", "ASC");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }
}
