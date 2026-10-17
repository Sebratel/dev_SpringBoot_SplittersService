package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.annotations.TokenRetry;
import br.com.sebratel.splitters.dto.splitters.EllevenPaginatedDTO;
import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.splitters.dto.splitters.NetworkComponentDTO;
import br.com.sebratel.splitters.exceptions.ResourceNotFoundException;
import br.com.sebratel.splitters.repository.erp.projections.SplitterProjection;
import br.com.sebratel.splitters.repository.erp.splitters.SplitterRepository;
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

@Service
@Slf4j
public class ListarSplittersService {

    private final WebClient webClient;
    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;
    private final SplitterRepository splitterRepository;

    @Autowired
    public ListarSplittersService(WebClient webClient, RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService, SplitterRepository splitterRepository) {
        this.webClient = webClient;
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
        this.splitterRepository = splitterRepository;
    }

    @TokenRetry
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> executar() {
        log.info("Listando todos os splitters.");
        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
        String url = "https://erp.sebratel.net.br:45715/external/map/splitter/all";
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
                .bodyToMono(new ParameterizedTypeReference<EllevenSplitterResponseDTO<List<NetworkComponentDTO>>>() {})
                .block();
    }

    @TokenRetry
    public EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> executar(int inicio, int quantidade) {
        log.info("Listando splitters paginados. Início: {}, Quantidade: {}", inicio, quantidade);
        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                .build();

        return webClient.mutate()
                .exchangeStrategies(strategies)
                .baseUrl("https://erp.sebratel.net.br:45715")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/external/map/splitter/all/paged")
                        .queryParam("page", inicio)
                        .queryParam("pageSize", quantidade)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleHttpError("listar splitter erro", response))
                .bodyToMono(new ParameterizedTypeReference<EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>>>() {})
                .block();
    }

    @TokenRetry
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> executar(Long splitterId) {
        log.info("Listando Splitter {}", splitterId);
        SplitterProjection splitterProjection = splitterRepository.getSplitterById(splitterId)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi encontrado splitter. {}"));
        return new EllevenSplitterResponseDTO<List<NetworkComponentDTO>>(
                true,
                null,
                List.of(new NetworkComponentDTO(
                        splitterId,
                        null,
                        null,
                        true,
                        null,
                        null,
                        0,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )),
                "NetworkComponentDTO",
                null
        );
    }


    private Mono<? extends Throwable> handleHttpError(String context, ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Erro HTTP na etapa {}: Status {} - Body: {}", context, response.statusCode(), body);
                    return Mono.error(new RuntimeException("Falha na integração Elleven: " + body));
                });
    }
}
