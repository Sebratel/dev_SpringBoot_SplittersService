package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.splitters.dto.splitters.NetworkComponentDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarTokenEllevenOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

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
class ListarOltsServiceTest {

    @Mock
    private WebClient webClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient.Builder webClientBuilder;

    @Mock
    private RecuperarTokenDoUsuarioIntegradorEllevenService tokenService;

    @InjectMocks
    private ListarOltsService service;

    @BeforeEach
    void setUp() {
        when(webClient.mutate()).thenReturn(webClientBuilder);
        when(webClientBuilder.exchangeStrategies(any(ExchangeStrategies.class)).build()).thenReturn(webClient);
    }

    @Test
    void executarShouldReturnOltsWhenApiSucceeds() {
        // Arrange
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
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        // Act
        EllevenSplitterResponseDTO<List<NetworkComponentDTO>> result = service.executar();

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
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("API Error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.executar());
    }
}
