package com.example.myproject.repository;

import com.example.myproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import static org.hibernate.loader.Loader.SELECT;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query(value = "SELECT COUNT(*) FROM users",nativeQuery = true)
    Long countTotalUser();

    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_USER'")
    Long countUsersWithRoleUser();

    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_MODERATOR'")
    Long countModeratorsWithRoleModerator();

    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN'")
    Long countAdminsWithRoleAdmin();

    @Query(value = "SELECT COUNT(*) FROM products",nativeQuery = true)
    Long countProducts();
}
