package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.RecuperarSolicitacaoDeClienteOutputDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class RecuperarSolicitacoesDeUmUsuarioService {

    private final WebClient webClient;
    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;


    @Autowired
    public RecuperarSolicitacoesDeUmUsuarioService(WebClient webClient, RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService) {
        this.webClient = webClient;
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
    }

    public RecuperarSolicitacaoDeClienteOutputDTO executar(String clientId) {

        String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
        String url = "https://erp.sebratel.net.br:45715/external/integrations/thirdparty/solicitationlist/" + clientId + "?allAssignments=false";
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                .build();

        try {
            return webClient.mutate()
                    .exchangeStrategies(strategies)
                    .build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(RecuperarSolicitacaoDeClienteOutputDTO.class)
                    .block();

        } catch (WebClientResponseException.Unauthorized e) {
            log.warn("Erro 401 na API Elleven ao recuperar solicitações do cliente. Invalidando token em cache.");
            recuperarTokenDoUsuarioIntegradorEllevenService.invalidateToken();
            throw e;
        }
    }
}
