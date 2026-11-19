package com.example.produtoapi.repository;

import com.example.produtoapi.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>{

    /*
    O Spring Data JPA consegue interpretar esse nome e gerar uma consulta
    para verificar se já existe uma categoria com aquele nome.
    */
    boolean existsByNome(String nome);
}
