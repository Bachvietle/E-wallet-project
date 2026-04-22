package com.BachLe.E_Wallet.domain.user.repository;

import com.BachLe.E_Wallet.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    public Optional<User> findByEmail(String email);
}
