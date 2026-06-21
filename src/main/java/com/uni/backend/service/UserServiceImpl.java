package com.uni.backend.service;

import com.uni.backend.entity.Role;
import com.uni.backend.entity.User;
import com.uni.backend.entity.UserDetails;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.RoleRepository;
import com.uni.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User user) {
        log.info("Register a new user...");

        log.info("eEncrypt the password...");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // only assign default USER role if the form didn't pass any roles
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "role", "USER"));
            user.setRoles(Set.of(userRole));
        }

        log.info("Sync for UserDetails...");
        if (user.getUserDetails() != null) {
            user.getUserDetails().setUser(user);
        }

        log.info("Save user...");
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Retrieving all users...");
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Retrieving user by id {}...", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Retrieving user by id {} failed.", id);
                    return new ResourceNotFoundException("User", "id", id);
                });
    }

    @Override
    @Transactional
    public User updateUser(User user, Long id) {
        log.info("Updating user with id {}...", id);

        log.debug("Check if the user exists else throw an error...");
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Retrieving user by id {} failed.", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        log.info("Update the fields of the user {}...", existingUser.getId());
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());

        log.info("Encrypt the password if it was updated...");
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getUserDetails() != null) {
            UserDetails existingDetails = existingUser.getUserDetails();

            if (existingDetails == null) {
                existingDetails = new UserDetails();
                existingDetails.setUser(existingUser);
                existingUser.setUserDetails(existingDetails);
            }

            existingDetails.setFirstName(user.getUserDetails().getFirstName());
            existingDetails.setLastName(user.getUserDetails().getLastName());
            existingDetails.setPhoneNumber(user.getUserDetails().getPhoneNumber());
            existingDetails.setAddress(user.getUserDetails().getAddress());
        }

        log.info("Save and returned the updated movie...");
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id {}...", id);

        log.debug("Check if the user exists else throw an error...");
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Retrieving user by id {} failed.", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        userRepository.delete(existingUser);
    }

    @Override
    public Page<User> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        return userRepository.findAll(pageable);
    }
}
