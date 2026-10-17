package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.ConnectionDTO;
import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarTokenEllevenOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConnectionsServiceTest {

    @Mock
    private WebClient webClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient.Builder webClientBuilder;

    @Mock
    private RecuperarTokenDoUsuarioIntegradorEllevenService tokenService;

    @InjectMocks
    private GetConnectionsService service;

    @BeforeEach
    void setUp() {
        when(webClient.mutate()).thenReturn(webClientBuilder);
        when(webClientBuilder.exchangeStrategies(any(ExchangeStrategies.class)).build()).thenReturn(webClient);
    }

    @Test
    void executarShouldReturnConnectionsWhenApiSucceeds() {
        // Arrange
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        EllevenSplitterResponseDTO<List<ConnectionDTO>> expectedResponse = new EllevenSplitterResponseDTO<>(true, null, List.of(mock(ConnectionDTO.class)), "type", null);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        // Act
        EllevenSplitterResponseDTO<List<ConnectionDTO>> result = service.executar();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.response().size());
        verify(tokenService).executar();
    }

    @Test
    void executarShouldThrowExceptionWhenApiFails() {
        // Arrange
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Simulating onStatus behavior that leads to Mono.error
        when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenAnswer(invocation -> {
            Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
            if (predicate.test(HttpStatusCode.valueOf(500))) {
                Function<ClientResponse, Mono<? extends Throwable>> errorHandler = invocation.getArgument(1);
                // We can't easily mock ClientResponse here, so we'll just mock bodyToMono directly to throw error
                return responseSpec;
            }
            return responseSpec;
        });
        
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("Falha na integração Elleven")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.executar());
    }
}
