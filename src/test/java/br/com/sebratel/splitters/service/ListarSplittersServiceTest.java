package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.EllevenPaginatedDTO;
import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.splitters.dto.splitters.NetworkComponentDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.splitters.exceptions.ResourceNotFoundException;
import br.com.sebratel.splitters.repository.erp.projections.SplitterProjection;
import br.com.sebratel.splitters.repository.erp.splitters.SplitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarSplittersServiceTest {

    @Mock
    private WebClient webClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient.Builder webClientBuilder;

    @Mock
    private RecuperarTokenDoUsuarioIntegradorEllevenService tokenService;

    @Mock
    private SplitterRepository repository;

    @InjectMocks
    private ListarSplittersService service;

    @BeforeEach
    void setUp() {
        stubWebClient();
    }

    private void stubWebClient() {
        lenient().when(webClient.mutate()).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.exchangeStrategies(any(ExchangeStrategies.class))).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void executarShouldReturnSplittersWhenApiSucceeds() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        EllevenSplitterResponseDTO<List<NetworkComponentDTO>> expectedResponse = new EllevenSplitterResponseDTO<>(true, null, List.of(mock(NetworkComponentDTO.class)), "type", null);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        EllevenSplitterResponseDTO<List<NetworkComponentDTO>> result = service.executar();

        assertNotNull(result);
        assertEquals(1, result.response().size());
    }

    @Test
    void executarPaginadoShouldReturnSplittersWhenApiSucceeds() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> expectedResponse = new EllevenSplitterResponseDTO<>(true, null, null, "type", null);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> result = service.executar(0, 10);

        assertNotNull(result);
        assertTrue(result.success());
    }

    @Test
    void executarPaginadoShouldThrowExceptionWhenApiFails() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<? extends Throwable>> errorHandler = invocation.getArgument(1);
            ClientResponse response = mock(ClientResponse.class);
            lenient().when(response.statusCode()).thenReturn(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            lenient().when(response.bodyToMono(String.class)).thenReturn(Mono.just("API Error"));
            
            Mono<? extends Throwable> errorMono = errorHandler.apply(response);
            assertNotNull(errorMono);
            return responseSpec;
        });
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("Falha na integração Elleven: API Error")));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.executar(0, 10));
        assertTrue(exception.getMessage().contains("Falha na integração Elleven"));
    }

    @Test
    void executarShouldThrowExceptionWhenApiFails() {
        stubWebClient();
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<? extends Throwable>> errorHandler = invocation.getArgument(1);
            ClientResponse response = mock(ClientResponse.class);
            lenient().when(response.statusCode()).thenReturn(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            lenient().when(response.bodyToMono(String.class)).thenReturn(Mono.just("API Error"));
            
            Mono<? extends Throwable> errorMono = errorHandler.apply(response);
            assertNotNull(errorMono);
            return responseSpec;
        });
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("Falha na integração Elleven: API Error")));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.executar());
        assertTrue(exception.getMessage().contains("Falha na integração Elleven"));
    }
    @Test
    void executarPorIdShouldReturnSplitterWhenFound() {
        SplitterProjection projection = mock(SplitterProjection.class);
        when(repository.getSplitterById(1L)).thenReturn(Optional.of(projection));

        EllevenSplitterResponseDTO<List<NetworkComponentDTO>> result = service.executar(1L);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(1, result.response().size());
        assertEquals(1L, result.response().get(0).id());
    }

    @Test
    void executarPorIdShouldThrowExceptionWhenNotFound() {
        when(repository.getSplitterById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.executar(1L));
    }
}
