package com.example.WalletAppReal.repostiory;

import com.example.WalletAppReal.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
