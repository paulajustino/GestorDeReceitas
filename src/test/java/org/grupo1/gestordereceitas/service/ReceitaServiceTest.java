package org.grupo1.gestordereceitas.service;

import org.grupo1.gestordereceitas.dto.ReceitaIngredienteDTO;
import org.grupo1.gestordereceitas.dto.ReceitaRequestDTO;
import org.grupo1.gestordereceitas.dto.ReceitaResponseDTO;
import org.grupo1.gestordereceitas.exception.BusinessException;
import org.grupo1.gestordereceitas.exception.ResourceNotFoundException;
import org.grupo1.gestordereceitas.model.Categoria;
import org.grupo1.gestordereceitas.model.Ingrediente;
import org.grupo1.gestordereceitas.model.Receita;
import org.grupo1.gestordereceitas.model.ReceitaIngrediente;
import org.grupo1.gestordereceitas.repository.CategoriaRepository;
import org.grupo1.gestordereceitas.repository.IngredienteRepository;
import org.grupo1.gestordereceitas.repository.ReceitaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReceitaServiceTest {

    @InjectMocks
    private ReceitaService receitaService;

    @Mock
    private ReceitaRepository receitaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    // --- Teste para listarTodas() ---
    @Test
    void deveRetornarListaDeReceitasCorretamente() {
        // Cenário
        Categoria categoria = criarCategoria(1L, "Sobremesas");
        Receita rec1 = criarReceita(1L, "Bolo de Cenoura", categoria, emptyList());
        Receita rec2 = criarReceita(2L, "Sorvete de Banana", categoria, emptyList());
        List<Receita> receitasEsperadas = Arrays.asList(rec1, rec2);

        when(receitaRepository.findAll()).thenReturn(receitasEsperadas);

        // Ação
        List<ReceitaResponseDTO> receitasRetornadas = receitaService.listarTodas();

        // Validação
        assertNotNull(receitasRetornadas);
        assertEquals(receitasEsperadas.size(), receitasRetornadas.size());
        assertEquals(receitasEsperadas.getFirst().getId(), receitasRetornadas.getFirst().getId());

        verify(receitaRepository, times(1)).findAll();
    }

    // --- Testes para buscarPorId() ---
    @Test
    void deveRetornarReceitaQuandoBuscarPorIdExistente() {
        // Cenário
        Long idExistente = 1L;
        Categoria categoria = criarCategoria(idExistente, "Brasileira");
        Receita receitaEsperada = criarReceita(idExistente, "Feijoada", categoria, emptyList());

        when(receitaRepository.findById(idExistente)).thenReturn(Optional.of(receitaEsperada));

        // Ação
        ReceitaResponseDTO receitaRetornada = receitaService.buscarPorId(idExistente);

        // Validação
        assertNotNull(receitaRetornada);
        assertEquals(receitaEsperada.getId(), receitaRetornada.getId());
        assertEquals(receitaEsperada.getNome(), receitaRetornada.getNome());

        verify(receitaRepository, times(1)).findById(idExistente);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarReceitaPorIdInexistente() {
        // Cenário
        Long idInexistente = 11L;

        when(receitaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        ResourceNotFoundException exceptionEsperada = assertThrows(ResourceNotFoundException.class, () ->
                receitaService.buscarPorId(idInexistente)
        );

        assertEquals("Receita com ID " + idInexistente + " não encontrada", exceptionEsperada.getMessage());

        verify(receitaRepository, times(1)).findById(idInexistente);
    }

    // --- Testes para salvar() ---
    @Test
    void deveSalvarReceitaEIngredientesRelacionadosCorretamente() {
        // Cenário
        Long categoriaId = 1L;
        Categoria categoria = criarCategoria(categoriaId, "Salgados");

        Ingrediente ing1 = criarIngrediente(1L, "Farinha");
        Ingrediente ing2 = criarIngrediente(2L, "Ovo");
        List<Ingrediente> ingredientes = Arrays.asList(ing1, ing2);

        ReceitaIngredienteDTO ingDto1 =
                criarReceitaIngredienteDTO(1L, "Farinha", "130", "g");
        ReceitaIngredienteDTO ingDto2 =
                criarReceitaIngredienteDTO(2L, "Ovo", "2", "unidade");
        List<ReceitaIngredienteDTO> ingDtos = Arrays.asList(ingDto1, ingDto2);

        ReceitaRequestDTO receitaASerSalva = criarReceitaRequestDTO(categoriaId, "Torta de Frango", ingDtos);

        ReceitaIngrediente receitaIng1 =
                criarReceitaIngrediente(1L, ing1, "130", "g");
        ReceitaIngrediente receitaIng2 =
                criarReceitaIngrediente(2L, ing2, "2", "unidade");
        List<ReceitaIngrediente> receitaIngredientes = Arrays.asList(receitaIng1, receitaIng2);

        Receita receitaSalva = criarReceita(1L, "Torta de Frango", categoria, receitaIngredientes);

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));
        when(ingredienteRepository.findAll()).thenReturn(ingredientes);
        when(receitaRepository.save(any(Receita.class))).thenReturn(receitaSalva);

        // Ação
        ReceitaResponseDTO receitaRetornada = receitaService.salvar(receitaASerSalva);

        // Validação
        assertNotNull(receitaRetornada);
        assertEquals(1L, receitaRetornada.getId());
        assertEquals("Torta de Frango", receitaRetornada.getNome());

        verify(categoriaRepository, times(1)).findById(categoriaId);
        verify(ingredienteRepository, times(1)).findAll();
        verify(receitaRepository, times(1)).save(any(Receita.class));
    }

    @Test
    void deveLancarExcecaoAoTentarSalvarReceitaSeCategoriaNaoExistir() {
        // Cenário
        ReceitaIngredienteDTO ingDto1 =
                criarReceitaIngredienteDTO(1L, "Farinha", "130", "g");
        ReceitaIngredienteDTO ingDto2 =
                criarReceitaIngredienteDTO(2L, "Ovo", "2", "unidade");
        List<ReceitaIngredienteDTO> ingDtos = Arrays.asList(ingDto1, ingDto2);

        Long categoriaIdInexistente = 11L;
        ReceitaRequestDTO receitaASerSalva = criarReceitaRequestDTO(categoriaIdInexistente, "Massa Fresca", ingDtos);

        when(categoriaRepository.findById(categoriaIdInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        ResourceNotFoundException exceptionEsperada =
                assertThrows(ResourceNotFoundException.class, () -> receitaService.salvar(receitaASerSalva));

        assertEquals("Categoria com ID " + categoriaIdInexistente + " não encontrada", exceptionEsperada.getMessage());

        verify(ingredienteRepository, never()).findAll();
        verify(receitaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoTentarSalvarReceitaSeNenhumIngredienteForEncontrado() {
        // Cenário
        Long categoriaId = 1L;
        Categoria categoria = criarCategoria(categoriaId, "Sobremesas");

        ReceitaRequestDTO receitaSemIngredientes = criarReceitaRequestDTO(categoriaId, "Novo Prato", emptyList());

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));
        when(ingredienteRepository.findAll()).thenReturn(emptyList());

        // Ação && Validação
        BusinessException exceptionEsperada = assertThrows(BusinessException.class, () ->
                receitaService.salvar(receitaSemIngredientes)
        );

        assertEquals("Nenhum ingrediente cadastrado.", exceptionEsperada.getMessage());

        verify(receitaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoTentarSalvarReceitaSeIngredienteRequeridoNaoForEncontrado() {
        // Cenário
        Long categoriaId = 1L;
        Categoria categoria = criarCategoria(categoriaId, "Doces");

        ReceitaIngredienteDTO ingDto1 =
                criarReceitaIngredienteDTO(1L, "Farinha", "130", "g");
        ReceitaIngredienteDTO ingDto2 =
                criarReceitaIngredienteDTO(2L, "Ovo", "2", "unidade");
        List<ReceitaIngredienteDTO> ingDtos = Arrays.asList(ingDto1, ingDto2);

        ReceitaRequestDTO receitaASerSalva = criarReceitaRequestDTO(categoriaId, "Bolo", ingDtos);

        List<Ingrediente> ingredientesEncontrados = List.of(criarIngrediente(2L, "Ovo"));

        when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));
        when(ingredienteRepository.findAll()).thenReturn(ingredientesEncontrados);

        // Ação && Validação
        IllegalArgumentException exceptionEsperada = assertThrows(IllegalArgumentException.class, () ->
                receitaService.salvar(receitaASerSalva)
        );

        assertEquals("Ingrediente não encontrado: ID " + ingDtos.getFirst().getIngredienteId(),
                exceptionEsperada.getMessage());

        verify(receitaRepository, never()).save(any());
    }

    // --- Testes para atualizar() ---
    @Test
    void deveAtualizarReceitaComSucessoQuandoDadosForemValidos() {
        // Cenário
        Long categoriaAntigaId = 1L;
        Long categoriaAtualizadaId = 4L;
        Long receitaId = 5L;

        Categoria categoriaAntiga = criarCategoria(categoriaAntigaId, "Sobremesas");
        Categoria categoriaAtualizada = criarCategoria(categoriaAtualizadaId, "Doces");

        Ingrediente ing1 = criarIngrediente(1L, "Farinha");
        Ingrediente ing2 = criarIngrediente(2L, "Ovo");
        Ingrediente ing3 = criarIngrediente(3L, "Leite");
        List<Ingrediente> ingredientes = Arrays.asList(ing1, ing2, ing3);

        Receita receitaExistente = criarReceita(receitaId, "Bolo", categoriaAntiga, emptyList());

        ReceitaRequestDTO receitaASerAtualizada =
                criarReceitaRequestDTO(categoriaAtualizadaId, "Bolo de Laranja", emptyList());

        when(receitaRepository.findById(receitaId)).thenReturn(Optional.of(receitaExistente));
        when(categoriaRepository.findById(categoriaAtualizadaId)).thenReturn(Optional.of(categoriaAtualizada));
        when(ingredienteRepository.findAll()).thenReturn(ingredientes);
        when(receitaRepository.save(any(Receita.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ação
        ReceitaResponseDTO receitaAtualizada = receitaService.atualizar(receitaId, receitaASerAtualizada);

        // Validação
        assertNotNull(receitaAtualizada);
        assertEquals(receitaId, receitaAtualizada.getId());
        assertEquals(receitaASerAtualizada.getNome(), receitaAtualizada.getNome());

        verify(receitaRepository, times(1)).findById(receitaId);
        verify(categoriaRepository, times(1)).findById(categoriaAtualizadaId);
        verify(ingredienteRepository, times(1)).findAll();
        verify(receitaRepository, times(1)).save(any(Receita.class));
    }

    @Test
    void deveLancarExcecaoAoTentarAtualizarReceitaInexistente() {
        // Cenário
        Long idInexistente = 11L;
        ReceitaRequestDTO receitaASerAtualizada = criarReceitaRequestDTO(1L, "Assado", emptyList());

        when(receitaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        ResourceNotFoundException exceptionEsperada = assertThrows(ResourceNotFoundException.class, () ->
                receitaService.atualizar(idInexistente, receitaASerAtualizada)
        );

        assertEquals("Receita com ID " + idInexistente + " não encontrada", exceptionEsperada.getMessage());

        verify(receitaRepository, times(1)).findById(idInexistente);
        verify(categoriaRepository, never()).findById(anyLong());
        verify(ingredienteRepository, never()).findAll();
        verify(receitaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoTentarAtualizarComCategoriaInexistente() {
        // Cenário
        Long receitaId = 5L;
        Long categoriaId = 1L;
        Categoria categoria = criarCategoria(categoriaId, "Sobremesas");
        Long categoriaIdInexistente = 11L;

        ReceitaIngredienteDTO ingDto1 =
                criarReceitaIngredienteDTO(1L, "Farinha", "130", "g");
        ReceitaIngredienteDTO ingDto2 =
                criarReceitaIngredienteDTO(2L, "Ovo", "2", "unidade");
        List<ReceitaIngredienteDTO> ingDtos = Arrays.asList(ingDto1, ingDto2);

        ReceitaRequestDTO receitaASerAtualizada =
                criarReceitaRequestDTO(categoriaIdInexistente, "Bolo de Chocolate", ingDtos);

        Ingrediente ing1 = criarIngrediente(1L, "Farinha");
        Ingrediente ing2 = criarIngrediente(2L, "Ovo");

        ReceitaIngrediente receitaIng1 =
                criarReceitaIngrediente(1L, ing1, "130", "g");
        ReceitaIngrediente receitaIng2 =
                criarReceitaIngrediente(2L, ing2, "2", "unidade");
        List<ReceitaIngrediente> receitaIngredientes = Arrays.asList(receitaIng1, receitaIng2);

        Receita receitaExistente = criarReceita(receitaId, "Bolo", categoria, receitaIngredientes);

        when(receitaRepository.findById(receitaId)).thenReturn(Optional.of(receitaExistente));
        when(categoriaRepository.findById(categoriaIdInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        ResourceNotFoundException exceptionEsperada = assertThrows(ResourceNotFoundException.class, () ->
                receitaService.atualizar(receitaId, receitaASerAtualizada)
        );

        assertEquals("Categoria com ID " + categoriaIdInexistente + " não encontrada",
                exceptionEsperada.getMessage()
        );

        verify(receitaRepository, times(1)).findById(receitaId);
        verify(categoriaRepository, times(1)).findById(categoriaIdInexistente);
        verify(ingredienteRepository, never()).findAll();
        verify(receitaRepository, never()).save(any());
    }

    // --- Testes para atualizarParcial() ---
    @Test
    void deveAtualizarApenasOCampoNomeQuandoOutrosCamposEstiveremNulos() {
        // Cenário
        Long receitaId = 1L;
        Receita receitaExistente = criarReceita(receitaId, "Receita", null, null);

        ReceitaRequestDTO receitaASerAtualizada = new ReceitaRequestDTO();
        receitaASerAtualizada.setNome("Receita Atualizada Via PATCH");

        when(receitaRepository.findById(receitaId)).thenReturn(Optional.of(receitaExistente));
        when(receitaRepository.save(any(Receita.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ação
        ReceitaResponseDTO receitaAtualizada = receitaService.atualizarParcial(receitaId, receitaASerAtualizada);

        // Validação
        assertNotNull(receitaAtualizada);
        assertEquals(receitaId, receitaAtualizada.getId());
        assertEquals(receitaASerAtualizada.getNome(), receitaAtualizada.getNome());
        assertTrue(receitaAtualizada.getIngredientes().isEmpty());
        assertNull(receitaAtualizada.getDescricao());
        assertNull(receitaAtualizada.getCategoria());
        assertEquals(0, receitaAtualizada.getTempoDePreparo());

        verify(receitaRepository, times(1)).findById(receitaId);
        verify(categoriaRepository, never()).findById(anyLong());
        verify(ingredienteRepository, never()).findAll();
        verify(receitaRepository, times(1)).save(any(Receita.class));
    }

    @Test
    void deveLancarExcecaoAoTentarFazerPatchEmReceitaInexistente() {
        // Cenário
        Long idInexistente = 11L;
        ReceitaRequestDTO receitaASerAtualizada = new ReceitaRequestDTO();
        receitaASerAtualizada.setNome("Receita Atualizada Via PATCH");

        when(receitaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        assertThrows(ResourceNotFoundException.class, () ->
                receitaService.atualizarParcial(idInexistente, receitaASerAtualizada)
        );

        verify(receitaRepository, times(1)).findById(idInexistente);
        verify(categoriaRepository, never()).findById(anyLong());
        verify(receitaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoTentarFazerPatchEmReceitaComCategoriaInexistente() {
        // Cenário
        Long categoriaIdInexistente = 11L;

        Long receitaId = 1L;
        Receita receitaExistente = criarReceita(receitaId, "Receita", null, null);

        ReceitaRequestDTO receitaASerAtualizada = new ReceitaRequestDTO();
        receitaASerAtualizada.setNome("Receita Atualizada Via PATCH");
        receitaASerAtualizada.setCategoriaId(categoriaIdInexistente);

        when(receitaRepository.findById(receitaId)).thenReturn(Optional.of(receitaExistente));
        when(categoriaRepository.findById(categoriaIdInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        assertThrows(ResourceNotFoundException.class, () ->
                receitaService.atualizarParcial(receitaId, receitaASerAtualizada)
        );

        verify(receitaRepository, times(1)).findById(receitaId);
        verify(receitaRepository, never()).save(any());
    }

    // --- Testes para deletar() ---
    @Test
    void deveDeletarReceitaComSucessoQuandoIdExistir() {
        // Cenário
        Long idExistente = 1L;

        when(receitaRepository.existsById(idExistente)).thenReturn(true);
        doNothing().when(receitaRepository).deleteById(idExistente);

        // Ação && Validação
        assertDoesNotThrow(() -> receitaService.deletar(idExistente));

        verify(receitaRepository, times(1)).existsById(idExistente);
        verify(receitaRepository, times(1)).deleteById(idExistente);
    }

    @Test
    void deveLancarExcecaoAoTentarDeletarReceitaComIdInexistente() {
        // Cenário
        Long idInexistente = 11L;

        when(receitaRepository.existsById(idInexistente)).thenReturn(false);

        // Ação && Validação
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                receitaService.deletar(idInexistente)
        );

        assertEquals("Receita com ID " + idInexistente + " não encontrada para exclusão",
                exception.getMessage());

        verify(receitaRepository, times(1)).existsById(idInexistente);
        verify(receitaRepository, never()).deleteById(idInexistente);
    }

    private Categoria criarCategoria(Long id, String nome) {
        return new Categoria(id, nome);
    }

    private Ingrediente criarIngrediente(Long id, String nome) {
        return new Ingrediente(id, nome);
    }

    private Receita criarReceita(Long id, String nome, Categoria categoria, List<ReceitaIngrediente> ingredientes) {
        Receita receita = new Receita();
        receita.setId(id);
        receita.setNome(nome);
        receita.setCategoria(categoria);
        receita.setReceitaIngredientes(ingredientes);

        return receita;
    }

    private ReceitaIngrediente criarReceitaIngrediente(
            Long id, Ingrediente ingrediente, String quantidade, String unidadeMedida
    ) {
        ReceitaIngrediente receitaIngrediente = new ReceitaIngrediente();
        receitaIngrediente.setId(id);
        receitaIngrediente.setReceita(null);
        receitaIngrediente.setIngrediente(ingrediente);
        receitaIngrediente.setQuantidade(quantidade);
        receitaIngrediente.setUnidadeMedida(unidadeMedida);

        return receitaIngrediente;
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

    private ReceitaRequestDTO criarReceitaRequestDTO(
            Long categoriaId, String nome, List<ReceitaIngredienteDTO> ingredientesDtos
    ) {
        ReceitaRequestDTO dto = new ReceitaRequestDTO();
        dto.setNome(nome);
        dto.setCategoriaId(categoriaId);
        dto.setIngredientes(ingredientesDtos);

        return dto;
    }
}
