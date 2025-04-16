package com.notes.securenotesapp.repository;

import com.notes.securenotesapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public  interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}