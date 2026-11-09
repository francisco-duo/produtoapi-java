package com.example.produtoapi.repository;

import com.example.produtoapi.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Não é necessário implementar os métodos de CRUD.
    // O Spring Data JPA já disponibiliza
    /*
    Método	            Funcionamento
    ____________________________________________
    save()	            Salva ou atualiza
    findAll()	        Busca todos
    findById()	        Busca por ID
    deleteById()	    Exclui por ID
    existsById()	    Verifica existência
    */
}
