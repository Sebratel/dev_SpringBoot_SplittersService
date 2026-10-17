package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.RecuperarSolicitacaoDeClienteOutputDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarTokenEllevenOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

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
class RecuperarSolicitacoesDeUmUsuarioServiceTest {

    @Mock
    private WebClient webClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient.Builder webClientBuilder;

    @Mock
    private RecuperarTokenDoUsuarioIntegradorEllevenService tokenService;

    @InjectMocks
    private RecuperarSolicitacoesDeUmUsuarioService service;

    @BeforeEach
    void setUp() {
        when(webClient.mutate()).thenReturn(webClientBuilder);
        when(webClientBuilder.exchangeStrategies(any(ExchangeStrategies.class)).build()).thenReturn(webClient);
    }

    @Test
    void executarShouldReturnSolicitationsWhenApiSucceeds() {
        // Arrange
        String clientId = "123";
        String token = "valid-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        RecuperarSolicitacaoDeClienteOutputDTO expectedResponse = new RecuperarSolicitacaoDeClienteOutputDTO(true, null, null, "type", null);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RecuperarSolicitacaoDeClienteOutputDTO.class)).thenReturn(Mono.just(expectedResponse));

        // Act
        RecuperarSolicitacaoDeClienteOutputDTO result = service.executar(clientId);

        // Assert
        assertNotNull(result);
        verify(tokenService).executar();
    }

    @Test
    void executarShouldPropagateExceptionWhenUnauthorized() {
        // Arrange
        String clientId = "123";
        String token = "expired-token";
        when(tokenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO(token, 3600, "bearer", "scope"));

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RecuperarSolicitacaoDeClienteOutputDTO.class))
                .thenReturn(Mono.error(WebClientResponseException.create(401, "Unauthorized", null, null, null)));

        // Act & Assert
        assertThrows(WebClientResponseException.Unauthorized.class, () -> service.executar(clientId));
    }
}
