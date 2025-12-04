package org.grupo1.gestordereceitas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo1.gestordereceitas.config.SecurityConfig;
import org.grupo1.gestordereceitas.exception.ResourceNotFoundException;
import org.grupo1.gestordereceitas.model.Categoria;
import org.grupo1.gestordereceitas.service.CategoriaService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
public class CategoriaControllerTest {

    @MockitoBean
    private CategoriaService categoriaService;

    @Autowired // Injeta o MockMvc para realizar as requisições HTTP simuladas
    private MockMvc mockMvc;

    @Autowired // Injeta o ObjectMapper para converter objetos Java em JSON e vice-versa
    private ObjectMapper objectMapper;

    // --- Teste para listarTodas() ---
    @Test
    void deveRetornarStatus200EListaDeCategorias() throws Exception {
        // Cenário
        Categoria cat1 = new Categoria(1L, "Massas");
        Categoria cat2 = new Categoria(2L, "Carnes");
        List<Categoria> categoriasEsperadas = Arrays.asList(cat1, cat2);

        when(categoriaService.listarTodas()).thenReturn(categoriasEsperadas);

        // Ação & Validação
        mockMvc.perform(get("/categorias")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(categoriasEsperadas.size())))
                .andExpect(jsonPath("$[0].nome", is(categoriasEsperadas.getFirst().getNome())));

        verify(categoriaService, times(1)).listarTodas();
    }

    // --- Testes para buscarPorId() ---
    @Test
    void deveRetornarStatus200ECategoriaQuandoBuscarPorIdExistente() throws Exception {
        // Cenário
        Long idExistente = 1L;
        Categoria categoriaEsperada = new Categoria(idExistente, "Sobremesas");

        when(categoriaService.buscarPorId(idExistente)).thenReturn(categoriaEsperada);

        // Ação & Validação
        mockMvc.perform(get("/categorias/{id}", idExistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(idExistente.intValue())))
                .andExpect(jsonPath("$.nome", is(categoriaEsperada.getNome())));

        verify(categoriaService, times(1)).buscarPorId(idExistente);
    }

    @Test
    void deveRetornarStatus404AoTentarBuscarCategoriaPorIdInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;

        when(categoriaService.buscarPorId(idInexistente))
                .thenThrow(new ResourceNotFoundException("Categoria com ID " + idInexistente + " não encontrada"));

        // Ação & Validação
        mockMvc.perform(get("/categorias/{id}", idInexistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).buscarPorId(idInexistente);
    }

    // --- Teste para criar/salvar() ---
    @Test
    void deveRetornarStatus201ECategoriaCriadaCorretamente() throws Exception {
        // Cenário
        Categoria categoriaASerCriada = new Categoria(null, "Vegetariana");
        Categoria categoriaCriada = new Categoria(1L, "Vegetariana");

        when(categoriaService.salvar(any(Categoria.class))).thenReturn(categoriaCriada);

        // Ação & Validação
        mockMvc.perform(post("/categorias")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        // Converte o objeto Java para o corpo da requisição JSON
                        .content(objectMapper.writeValueAsString(categoriaASerCriada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(categoriaCriada.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(categoriaCriada.getNome())));

        verify(categoriaService, times(1)).salvar(any(Categoria.class));
    }

    // --- Testes para atualizar() ---
    @Test
    void deveRetornarStatus200ECategoriaAtualizada() throws Exception {
        // Cenário
        Long categoriaId = 1L;
        Categoria categoriaASerAtualizada = new Categoria(null, "Bebidas Quentes");
        Categoria categoriaAtualizada = new Categoria(categoriaId, "Bebidas Quentes");

        when(categoriaService.atualizar(eq(categoriaId), any(Categoria.class))).thenReturn(categoriaAtualizada);

        // Ação & Validação
        mockMvc.perform(put("/categorias/{id}", categoriaId)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(categoriaASerAtualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoriaAtualizada.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(categoriaAtualizada.getNome())));

        verify(categoriaService, times(1)).atualizar(eq(categoriaId), any(Categoria.class));
    }

    @Test
    void deveRetornarStatus404AoTentarAtualizarCategoriaInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;
        Categoria categoriaASerAtualizada = new Categoria(null, "Bebidas");

        when(categoriaService.atualizar(eq(idInexistente), any(Categoria.class)))
                .thenThrow(new ResourceNotFoundException("Categoria com ID " + idInexistente + " não encontrada"));

        // Ação & Validação
        mockMvc.perform(put("/categorias/{id}", idInexistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(categoriaASerAtualizada)))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).atualizar(eq(idInexistente), any(Categoria.class));
    }

    // --- Testes para deletar() ---
    @Test
    void deveRetornarStatus204AoDeletarCategoriaComSucesso() throws Exception {
        // Cenário
        Long categoriaId = 1L;

        doNothing().when(categoriaService).deletar(categoriaId);

        // Ação & Validação
        mockMvc.perform(delete("/categorias/{id}", categoriaId))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).deletar(categoriaId);
    }

    @Test
    void deveRetornarStatus404AoTentarDeletarCategoriaInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;

        doThrow(new ResourceNotFoundException("Categoria com ID " + idInexistente + " não encontrada para exclusão"))
                .when(categoriaService).deletar(idInexistente);

        // Ação & Validação
        mockMvc.perform(delete("/categorias/{id}", idInexistente))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).deletar(idInexistente);
    }
}
