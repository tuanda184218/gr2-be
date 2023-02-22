package com.example.myproject.security.services;

import com.example.myproject.models.User;
import com.example.myproject.repository.ProductRepository;
import com.example.myproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    public Map<String, Long> getTotalUserStats() {
        Long totalUsers = userRepository.countTotalUser();
        Long roleUsers = userRepository.countUsersWithRoleUser();
        Long roleModerators = userRepository.countModeratorsWithRoleModerator();
        Long roleAdmins = userRepository.countAdminsWithRoleAdmin();
        Long totalProducts = userRepository.countProducts();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("roleUsers", roleUsers);
        stats.put("roleModerators", roleModerators);
        stats.put("roleAdmins", roleAdmins);
        stats.put("totalProducts", totalProducts);

        return stats;
    }

}

