package br.com.pedroonietoo.taskManagement.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.pedroonietoo.taskManagement.models.UserModel;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmail(String email);
}
