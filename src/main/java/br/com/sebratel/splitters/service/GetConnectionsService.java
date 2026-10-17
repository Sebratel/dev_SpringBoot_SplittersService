package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.annotations.TokenRetry;
import br.com.sebratel.splitters.dto.splitters.ConnectionDTO;
import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class GetConnectionsService {

    private final WebClient webClient;
    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;

    @Autowired
    public GetConnectionsService(WebClient webClient, RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService) {
        this.webClient = webClient;
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
    }

    @TokenRetry
    public EllevenSplitterResponseDTO<List<ConnectionDTO>> executar() {
        log.info("Listando todas as connections.");

        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
        String url = "https://erp.sebratel.net.br:45715/external/map/connection/all";
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                .build();

        return webClient.mutate()
                .exchangeStrategies(strategies)
                .build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleHttpError("listar splitter erro", response))
                .bodyToMono(new ParameterizedTypeReference<EllevenSplitterResponseDTO<List<ConnectionDTO>>>() {})
                .block();
    }

    private Mono<? extends Throwable> handleHttpError(String context, ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Erro HTTP na etapa {}: Status {} - Body: {}", context, response.statusCode(), body);
                    return Mono.error(new RuntimeException("Falha na integração Elleven: " + body));
                });
    }
}
