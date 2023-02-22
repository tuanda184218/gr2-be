package com.example.myproject.controllers;

import com.example.myproject.models.*;
import com.example.myproject.payload.request.SignupRequest;
import com.example.myproject.repository.RoleRepository;
import com.example.myproject.repository.UserRepository;
import com.example.myproject.security.services.UserDetailsServiceImpl;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/users")
public class UserController {
    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    PasswordEncoder encoder;

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    List<User> getALLUsers() {
        return repository.findAll();
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ResponseObject> createNewUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (repository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Username already taken", "")
            );
        }

        if (repository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Email already taken", "")
            );
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "ROLE_MODERATOR":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        repository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "Create user successfully", user)
        );
    }

    //get detail user
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    ResponseEntity<ResponseObject> findById(@PathVariable Long id) {
        Optional<User> foundUser = repository.findById(id);
        return foundUser.isPresent() ?
                ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject("ok", "Query user successfully", foundUser)
                        //you can replace "ok" with your defined "error code"
                ) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject("failed", "Cannot find user with id = " + id, "")
                );
    }

//    update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ResponseObject> updateUser(@RequestBody SignupRequest signUpRequest, @PathVariable Long id) {
        if (repository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "Username already taken", "")
            );
        }

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "ROLE_MODERATOR":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }


            User updatedUser = repository.findById(id)
                    .map(user -> {
                        user.setEmail(signUpRequest.getEmail());
                        user.setRoles(roles);
                        return repository.save(user);
                    }).orElseGet(() -> {
                        User newUser = new User();
                        newUser.setId(id);
                        return repository.save(newUser);
                    });
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Update user successfully", updatedUser)
            );
        }
    //delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ResponseObject> deleteUser(@PathVariable Long id) {
        boolean exists = repository.existsById(id);
        if(exists) {
            repository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete user successfully", "")
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed", "Cannot find user to delete", "")
        );
    }

    //get total of users with each role
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getTotalUserStats() {
        Map<String, Long> stats = userDetailsService.getTotalUserStats();
        return ResponseEntity.ok(stats);
    }

}
