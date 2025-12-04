package org.grupo1.gestordereceitas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupo1.gestordereceitas.config.SecurityConfig;
import org.grupo1.gestordereceitas.dto.ReceitaIngredienteDTO;
import org.grupo1.gestordereceitas.dto.ReceitaRequestDTO;
import org.grupo1.gestordereceitas.dto.ReceitaResponseDTO;
import org.grupo1.gestordereceitas.exception.ResourceNotFoundException;
import org.grupo1.gestordereceitas.service.ReceitaService;
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

@WebMvcTest(ReceitaController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
public class ReceitaControllerTest {

    @MockitoBean
    private ReceitaService receitaService;

    @Autowired // Injeta o MockMvc para realizar as requisições HTTP simuladas
    private MockMvc mockMvc;

    @Autowired // Injeta o ObjectMapper para converter objetos Java em JSON e vice-versa
    private ObjectMapper objectMapper;

    // --- Teste para listarTodas() ---
    @Test
    void deveRetornarStatus200EListaDeReceitas() throws Exception {
        // Cenário
        ReceitaResponseDTO receita1 = criarReceitaResponseDTO(1L, "Lasanha");
        ReceitaResponseDTO receita2 = criarReceitaResponseDTO(2L, "Mousse");
        List<ReceitaResponseDTO> receitasEsperadas = Arrays.asList(receita1, receita2);

        when(receitaService.listarTodas()).thenReturn(receitasEsperadas);

        // Ação & Validação
        mockMvc.perform(get("/receitas")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(receitasEsperadas.size())))
                .andExpect(jsonPath("$[0].nome", is(receitasEsperadas.getFirst().getNome())));

        verify(receitaService, times(1)).listarTodas();
    }

    // --- Testes para buscarPorId() ---
    @Test
    void deveRetornarStatus200EReceitaQuandoBuscarPorIdExistente() throws Exception {
        // Cenário
        Long idExistente = 1L;
        ReceitaResponseDTO receitaEsperada = criarReceitaResponseDTO(idExistente, "Feijoada");

        when(receitaService.buscarPorId(idExistente)).thenReturn(receitaEsperada);

        // Ação & Validação
        mockMvc.perform(get("/receitas/{id}", idExistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(idExistente.intValue())))
                .andExpect(jsonPath("$.nome", is(receitaEsperada.getNome())));

        verify(receitaService, times(1)).buscarPorId(idExistente);
    }

    @Test
    void deveRetornarStatus404AoTentarBuscarReceitaPorIdInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;

        when(receitaService.buscarPorId(idInexistente))
                .thenThrow(new ResourceNotFoundException("Receita com ID " + idInexistente + " não encontrada"));

        // Ação & Validação
        mockMvc.perform(get("/receitas/{id}", idInexistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound());

        verify(receitaService, times(1)).buscarPorId(idInexistente);
    }

    // --- Teste para criar/salvar() ---
    @Test
    void deveRetornarStatus201EReceitaCriadaCorretamente() throws Exception {
        // Cenário
        ReceitaIngredienteDTO ingDto =
                criarReceitaIngredienteDTO(1L, "Ovo", "2", "unidades");
        List<ReceitaIngredienteDTO> ingredienteDtos = List.of(ingDto);

        ReceitaRequestDTO receitaASerCriada =
                criarReceitaRequestDTO("Bolo de Chocolate", 1L, ingredienteDtos);
        ReceitaResponseDTO receitaCriada = criarReceitaResponseDTO(1L, "Bolo de Chocolate");

        when(receitaService.salvar(any(ReceitaRequestDTO.class))).thenReturn(receitaCriada);

        // Ação & Validação
        mockMvc.perform(post("/receitas")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(receitaASerCriada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(receitaCriada.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(receitaCriada.getNome())));

        verify(receitaService, times(1)).salvar(any(ReceitaRequestDTO.class));
    }

    // --- Testes para atualizarReceita() // PUT ---
    @Test
    void deveRetornarStatus200EReceitaAtualizada() throws Exception {
        // Cenário
        Long idExistente = 1L;
        ReceitaIngredienteDTO ingDto =
                criarReceitaIngredienteDTO(2L, "Farinha", "130", "g");
        List<ReceitaIngredienteDTO> ingredienteDtos = List.of(ingDto);
        ReceitaRequestDTO receitaASerAtualizada = criarReceitaRequestDTO("Bolo", 2L, ingredienteDtos);
        ReceitaResponseDTO receitaAtualizada = criarReceitaResponseDTO(idExistente, "Bolo de Chocolate");

        when(receitaService.atualizar(eq(idExistente), any(ReceitaRequestDTO.class))).thenReturn(receitaAtualizada);

        // Ação & Validação
        mockMvc.perform(put("/receitas/{id}", idExistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(receitaASerAtualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(receitaAtualizada.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(receitaAtualizada.getNome())));

        verify(receitaService, times(1)).atualizar(eq(idExistente), any(ReceitaRequestDTO.class));
    }

    @Test
    void deveRetornarStatus404AoTentarAtualizarReceitaInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;
        ReceitaIngredienteDTO ingDto =
                criarReceitaIngredienteDTO(3L, "Leite", "2", "xícaras");
        List<ReceitaIngredienteDTO> ingredienteDtos = List.of(ingDto);
        ReceitaRequestDTO receitaASerAtualizada =
                criarReceitaRequestDTO("Bolo de Chocolate", 1L, ingredienteDtos);

        when(receitaService.atualizar(eq(idInexistente), any(ReceitaRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Receita com ID " + idInexistente + " não encontrada"));

        // Ação & Validação
        mockMvc.perform(put("/receitas/{id}", idInexistente)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(receitaASerAtualizada)))
                .andExpect(status().isNotFound());

        verify(receitaService, times(1)).atualizar(eq(idInexistente), any(ReceitaRequestDTO.class));
    }

    // --- Testes para deletar() ---
    @Test
    void deveRetornarStatus204AoDeletarReceitaComSucesso() throws Exception {
        // Cenário
        Long receitaId = 1L;

        doNothing().when(receitaService).deletar(receitaId);

        // Ação & Validação
        mockMvc.perform(delete("/receitas/{id}", receitaId))
                .andExpect(status().isNoContent());

        verify(receitaService, times(1)).deletar(receitaId);
    }

    @Test
    void deveRetornarStatus404AoTentarDeletarReceitaInexistente() throws Exception {
        // Cenário
        Long idInexistente = 11L;

        doThrow(new ResourceNotFoundException("Receita com ID " + idInexistente + " não encontrada para exclusão"))
                .when(receitaService).deletar(idInexistente);

        // Ação & Validação
        mockMvc.perform(delete("/receitas/{id}", idInexistente))
                .andExpect(status().isNotFound());

        verify(receitaService, times(1)).deletar(idInexistente);
    }

    private ReceitaResponseDTO criarReceitaResponseDTO(Long id, String nome) {
        ReceitaResponseDTO dto = new ReceitaResponseDTO();
        dto.setId(id);
        dto.setNome(nome);

        return dto;
    }

    private ReceitaRequestDTO criarReceitaRequestDTO(
            String nome, Long categoriaId, List<ReceitaIngredienteDTO> ingredientes
    ) {
        ReceitaRequestDTO dto = new ReceitaRequestDTO();
        dto.setNome(nome);
        dto.setCategoriaId(categoriaId);
        dto.setIngredientes(ingredientes);

        return dto;
    }

    private ReceitaIngredienteDTO criarReceitaIngredienteDTO(
            Long ingredienteId, String nome, String quantidade, String unidade
    ) {
        ReceitaIngredienteDTO dto = new ReceitaIngredienteDTO();
        dto.setIngredienteId(ingredienteId);
        dto.setNome(nome);
        dto.setQuantidade(quantidade);
        dto.setUnidade(unidade);

        return dto;
    }
}
