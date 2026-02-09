package com.example.studentexpensetracker.controller;

import com.example.studentexpensetracker.model.User;
import com.example.studentexpensetracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html";
    }


    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session) {

        Optional<User> existingUserOpt = Optional.ofNullable(userRepository.findByUsername(username));

        if (existingUserOpt.isPresent() &&
                passwordEncoder.matches(password, existingUserOpt.get().getPassword())) {
            session.setAttribute("user", existingUserOpt.get());
            return "redirect:/view-expenses.html";
        } else {
            return "redirect:/login.html?error=true";
        }
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "redirect:/signup.html";
    }



    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute User user,
                         BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "redirect:/signup.html?error=Please+fill+all+fields+correctly";
        }

        if (userRepository.findByRollNo(user.getRollNo()) != null) {
            return "redirect:/signup.html?error=Roll+number+already+exists";
        }

        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "redirect:/signup.html?error=Username+already+taken";
        }

        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "redirect:/signup.html?error=Email+already+registered";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);


        return "redirect:/login.html?success=Signup+successful!+Please+log+in.";
    }


    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        return ResponseEntity.ok(loggedInUser);
    }


    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute User updatedUser,
                                           BindingResult bindingResult,
                                           HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login first");
        }

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        if (!currentUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.findByEmail(updatedUser.getEmail()) != null) {
            return ResponseEntity.badRequest().body(List.of("Email already registered"));
        }

        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setUsername(updatedUser.getUsername());
        currentUser.setRollNo(updatedUser.getRollNo());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(currentUser);
        session.setAttribute("user", currentUser); // refresh session

        return ResponseEntity.ok("Profile updated successfully");
    }



    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login.html";
    }
}