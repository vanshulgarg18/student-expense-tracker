package com.example.studentexpensetracker.repository;

import com.example.studentexpensetracker.model.Expense;
import com.example.studentexpensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);
}
