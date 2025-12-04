package org.grupo1.gestordereceitas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo1.gestordereceitas.config.SecurityConfig;
import org.grupo1.gestordereceitas.exception.ResourceNotFoundException;
import org.grupo1.gestordereceitas.model.Ingrediente;
import org.grupo1.gestordereceitas.service.IngredienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngredienteController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
public class IngredienteControllerTest {

    @MockitoBean
    private IngredienteService ingredienteService;

    @Autowired // Injeta o MockMvc para realizar as requisições HTTP simuladas
    private MockMvc mockMvc;

    @Autowired // Injeta o ObjectMapper para converter objetos Java em JSON e vice-versa
    private ObjectMapper objectMapper;

    // --- Teste para listarTodas() ---
    @Test
    void deveRetornarStatus200EListaDeIngredientes() throws Exception {
        // Cenário
        Ingrediente ing1 = new Ingrediente(1L, "Farinha");
        Ingrediente ing2 = new Ingrediente(2L, "Ovo");
        List<Ingrediente> ingredientesEsperados = Arrays.asList(ing1, ing2);

        when(ingredienteService.listarTodos()).thenReturn(ingredientesEsperados);

        // Ação & Validação
        mockMvc.perform(get("/ingredientes")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(ingredientesEsperados.size())))
                .andExpect(jsonPath("$[0].nome", is(ingredientesEsperados.getFirst().getNome())));

        verify(ingredienteService, times(1)).listarTodos();
    }

    // --- Testes para buscarPorId() ---
    @Test
    void deveRetornarStatus200EIngredienteQuandoBuscarPorIdExistente() throws Exception {
        // Cenário
        Long idExistente = 1L;
        Ingrediente ingredienteEsperado = new Ingrediente(idExistente, "Farinha");

        when(ingredienteService.buscarPorId(idExistente)).thenReturn(ingredienteEsperado);

        // Ação & Validação
        mockMvc.perform(get("/ingredientes/{id}", idExistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(idExistente.intValue())))
                .andExpect(jsonPath("$.nome", is(ingredienteEsperado.getNome())));

        verify(ingredienteService, times(1)).buscarPorId(idExistente);
    }

    @Test
    void deveRetornarStatus404AoTentarBuscarIngredientePorIdInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;

        when(ingredienteService.buscarPorId(idInexistente))
                .thenThrow(new ResourceNotFoundException("Ingrediente com ID " + idInexistente + " não encontrado"));

        // Ação & Validação
        mockMvc.perform(get("/ingredientes/{id}", idInexistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound());

        verify(ingredienteService, times(1)).buscarPorId(idInexistente);
    }

    // --- Teste para criar/salvar() ---
    @Test
    void deveRetornarStatus201EIngredienteCriadoCorretamente() throws Exception {
        // Cenário
        Ingrediente ingredienteASerCriado = new Ingrediente(null, "Farinha");
        Ingrediente ingredienteCriado = new Ingrediente(1L, "Farinha");

        when(ingredienteService.salvar(any(Ingrediente.class))).thenReturn(ingredienteCriado);

        // Ação & Validação
        mockMvc.perform(post("/ingredientes")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        // Converte o objeto Java para o corpo da requisição JSON
                        .content(objectMapper.writeValueAsString(ingredienteASerCriado)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ingredienteCriado.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(ingredienteCriado.getNome())));

        verify(ingredienteService, times(1)).salvar(any(Ingrediente.class));
    }

    // --- Testes para atualizar() ---
    @Test
    void deveRetornarStatus200EIngredienteAtualizado() throws Exception {
        // Cenário
        Long ingredienteId = 1L;
        Ingrediente ingredienteASerAtualizado = new Ingrediente(null, "Ovo");
        Ingrediente ingredienteAtualizado = new Ingrediente(ingredienteId, "Ovo");

        when(ingredienteService.atualizar(eq(ingredienteId), any(Ingrediente.class))).thenReturn(ingredienteAtualizado);

        // Ação & Validação
        mockMvc.perform(put("/ingredientes/{id}", ingredienteId)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(ingredienteASerAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ingredienteAtualizado.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(ingredienteAtualizado.getNome())));

        verify(ingredienteService, times(1)).atualizar(eq(ingredienteId), any(Ingrediente.class));
    }

    @Test
    void deveRetornarStatus404AoTentarAtualizarIngredienteInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;
        Ingrediente ingredienteASerAtualizado = new Ingrediente(null, "Chocolate");

        when(ingredienteService.atualizar(eq(idInexistente), any(Ingrediente.class)))
                .thenThrow(new ResourceNotFoundException("Ingrediente com ID " + idInexistente + " não encontrado"));

        // Ação & Validação
        mockMvc.perform(put("/ingredientes/{id}", idInexistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(ingredienteASerAtualizado)))
                .andExpect(status().isNotFound());

        verify(ingredienteService, times(1)).atualizar(eq(idInexistente), any(Ingrediente.class));
    }

    // --- Testes para deletar() ---
    @Test
    void deveRetornarStatus204AoDeletarIngredienteComSucesso() throws Exception {
        // Cenário
        Long ingredienteId = 1L;

        doNothing().when(ingredienteService).deletar(ingredienteId);

        // Ação & Validação
        mockMvc.perform(delete("/ingredientes/{id}", ingredienteId))
                .andExpect(status().isNoContent());

        verify(ingredienteService, times(1)).deletar(ingredienteId);
    }

    @Test
    void deveRetornarStatus404AoTentarDeletarIngredienteInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;

        doThrow(new ResourceNotFoundException("Ingrediente com ID " + idInexistente + " não encontrado para exclusão"))
                .when(ingredienteService).deletar(idInexistente);

        // Ação & Validação
        mockMvc.perform(delete("/ingredientes/{id}", idInexistente))
                .andExpect(status().isNotFound());

        verify(ingredienteService, times(1)).deletar(idInexistente);
    }
}
