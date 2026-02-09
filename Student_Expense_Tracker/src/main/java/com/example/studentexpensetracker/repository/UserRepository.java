package com.example.studentexpensetracker.repository;

import com.example.studentexpensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByRollNo(String rollNo);
}
