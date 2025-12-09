package org.project.project.repository;

import org.project.project.model.entity.Token;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenValue(String valorToken);
    List<Token> findByUsuarioAndTokenStatus(Usuario usuario, Token.EstadoToken estadoToken);
}
