package com.example.studentexpensetracker.controller;

import com.example.studentexpensetracker.model.Expense;
import com.example.studentexpensetracker.model.User;
import com.example.studentexpensetracker.repository.ExpenseRepository;
import com.example.studentexpensetracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;

    public ExpenseController(ExpenseRepository expenseRepo, UserRepository userRepo) {
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }


    @GetMapping
    public ResponseEntity<?> getUserExpenses(HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        return ResponseEntity.ok(expenseRepo.findByUserId(loggedInUser.getId()));
    }


    @PostMapping
    public ResponseEntity<?> addExpense(@RequestBody Expense expense, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Authentication failed. Please log in again.");
        }

        expense.setUser(loggedInUser);
        Expense savedExpense = expenseRepo.save(expense);
        return ResponseEntity.ok(savedExpense);
    }


    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateExpense(@PathVariable Long id,
                                           @RequestBody Expense expenseDetails,
                                           HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        Expense existing = expenseRepo.findById(id).orElse(null);
        if (existing != null && existing.getUser().getId().equals(loggedInUser.getId())) {
            existing.setTitle(expenseDetails.getTitle());
            existing.setDescription(expenseDetails.getDescription());
            existing.setAmount(expenseDetails.getAmount());
            return ResponseEntity.ok(expenseRepo.save(existing)); // save changes
        }
        return ResponseEntity.status(404).body("Expense not found or you do not have permission to edit it.");
    }



    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        return expenseRepo.findById(id)
                .map(expense -> {
                    if (!expense.getUser().getId().equals(loggedInUser.getId())) {
                        return ResponseEntity.status(403).body("You do not have permission to delete this expense.");
                    }
                    expenseRepo.delete(expense);
                    return ResponseEntity.ok("Deleted successfully");
                })
                .orElse(ResponseEntity.status(404).body("Expense not found."));
    }
}
