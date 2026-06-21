package com.uni.backend.service;

import com.uni.backend.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    // create
    User createUser(User user);

    // read
    List<User> getAllUsers();
    User getUserById(Long id);

    // update
    User updateUser(User user, Long id);

    // delete
    void deleteUser(Long id);

    Page<User> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection);
}
