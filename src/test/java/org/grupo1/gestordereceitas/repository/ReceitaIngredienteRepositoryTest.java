package org.grupo1.gestordereceitas.repository;

import org.grupo1.gestordereceitas.model.Categoria;
import org.grupo1.gestordereceitas.model.Ingrediente;
import org.grupo1.gestordereceitas.model.Receita;
import org.grupo1.gestordereceitas.model.ReceitaIngrediente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReceitaIngredienteRepositoryTest {

    @Autowired
    private ReceitaIngredienteRepository receitaIngredienteRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void deveSalvarReceitaIngredienteCorretamente() {
        // Cenário
        Receita receita = criarReceita();
        Ingrediente ingrediente = criarIngrediente("Queijo");

        ReceitaIngrediente recIngredienteASerSalva =
                new ReceitaIngrediente(null, receita, ingrediente, "200", "g");

        // Ação
        ReceitaIngrediente recIngredienteSalva = receitaIngredienteRepository.save(recIngredienteASerSalva);

        // Validação
        assertNotNull(recIngredienteSalva.getId());
        assertEquals(recIngredienteASerSalva.getReceita().getNome(), recIngredienteSalva.getReceita().getNome());
    }

    @Test
    void deveRetornarReceitaIngredienteQuandoBuscarPorIdExistente() {
        // Cenário
        Receita receita = criarReceita();
        Ingrediente ingrediente = criarIngrediente("Queijo");

        ReceitaIngrediente recIngredienteASerSalva =
                new ReceitaIngrediente(null, receita, ingrediente, "200", "g");
        ReceitaIngrediente recIngrediente = receitaIngredienteRepository.save(recIngredienteASerSalva);

        // Ação
        Optional<ReceitaIngrediente> recIngredienteRetornada =
                receitaIngredienteRepository.findById(recIngrediente.getId());

        // Validação
        assertTrue(recIngredienteRetornada.isPresent());
        assertEquals(recIngrediente.getReceita().getNome(), recIngredienteRetornada.get().getReceita().getNome());
    }

    @Test
    void deveRetornarOptionalVazioQuandoBuscarReceitaIngredientePorIdInexistente() {
        // Ação
        Optional<ReceitaIngrediente> recIngredienteRetornada = receitaIngredienteRepository.findById(11L);

        // Validação
        assertTrue(recIngredienteRetornada.isEmpty());
    }

    @Test
    void deveRetornarTodosAsReceitaIngredientesCorretamente() {
        // Cenário
        Receita receita = criarReceita();
        Ingrediente ingrediente1 = criarIngrediente("Queijo");
        Ingrediente ingrediente2 = criarIngrediente("Molho de Tomate");

        ReceitaIngrediente recIngrediente1 =
                new ReceitaIngrediente(null, receita, ingrediente1, "200", "g");
        ReceitaIngrediente recIngrediente2 =
                new ReceitaIngrediente(null, receita, ingrediente2, "200", "ml");
        receitaIngredienteRepository.save(recIngrediente1);
        receitaIngredienteRepository.save(recIngrediente2);

        // Ação
        List<ReceitaIngrediente> recIngredientesRetornadas = receitaIngredienteRepository.findAll();

        // Validação
        assertFalse(recIngredientesRetornadas.isEmpty());
        assertEquals(2, recIngredientesRetornadas.size());
    }

    @Test
    void deveRetornarListaVaziaSeNenhumaReceitaIngredienteExistir() {
        // Ação
        List<ReceitaIngrediente> recIngredientesRetornadas = receitaIngredienteRepository.findAll();

        // Validação
        assertTrue(recIngredientesRetornadas.isEmpty());
    }

    @Test
    void deveDeletarReceitaIngredienteComSucesso() {
        // Cenário
        Receita receita = criarReceita();
        Ingrediente ingrediente = criarIngrediente("Queijo");

        ReceitaIngrediente recIngrediente =
                new ReceitaIngrediente(null, receita, ingrediente, "200", "g");
        receitaIngredienteRepository.save(recIngrediente);

        // Ação
        receitaIngredienteRepository.deleteById(ingrediente.getId());
        Optional<ReceitaIngrediente> recIngredienteRetornada =
                receitaIngredienteRepository.findById(ingrediente.getId());

        // Validação
        assertTrue(recIngredienteRetornada.isEmpty());
    }

    private Receita criarReceita() {
        Categoria categoria = new Categoria(null, "Massas");
        categoriaRepository.save(categoria);

        Receita receita =
                new Receita(null, "Lasanha", "Lasanha caseira", 40, categoria, null);
        receita.setReceitaIngredientes(new ArrayList<>());

        return receitaRepository.save(receita);
    }

    private Ingrediente criarIngrediente(String nome) {
        Ingrediente ingrediente = new Ingrediente(null, nome);
        return ingredienteRepository.save(ingrediente);
    }
}
