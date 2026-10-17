package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.splitters.dto.splitters.NetworkComponentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ListarOltsService {
    private final WebClient webClient;
    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;

    @Autowired
    public ListarOltsService(WebClient webClient, RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService) {
        this.webClient = webClient;
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
    }

    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> executar() {
        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
        String url = "https://erp.sebratel.net.br:45715/external/map/olt/all";
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
                .bodyToMono(new ParameterizedTypeReference<EllevenSplitterResponseDTO<List<NetworkComponentDTO>>>(){})
                .block();
    }
}
