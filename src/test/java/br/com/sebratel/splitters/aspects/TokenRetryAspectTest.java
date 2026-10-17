package br.com.sebratel.splitters.aspects;

import br.com.sebratel.splitters.annotations.TokenRetry;
import br.com.sebratel.splitters.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.splitters.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre o {@link TokenRetryAspect}, em especial a garantia de que a nova tentativa
 * apos um 401 recebe o token renovado.
 *
 * <p>Regressao coberta (finding F-14): o aspect chamava {@code joinPoint.proceed()}
 * sem argumentos, o que reexecutava o metodo com o token original. O retry existia
 * mas era inutil -- reenviava exatamente a credencial que acabara de ser recusada.
 */
@ExtendWith(MockitoExtension.class)
class TokenRetryAspectTest {

    private static final String TOKEN_EXPIRADO = "token-expirado";
    private static final String TOKEN_RENOVADO = "token-renovado";

    @Mock
    private RecuperarTokenDoUsuarioIntegradorEllevenService tokenService;

    private TokenRetryAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new TokenRetryAspect(tokenService);
    }

    /** Alvo minimo apenas para exercitar o aspect. */
    interface ChamadaExterna {
        String executar(String token);
    }

    /**
     * Falha com 401 nas primeiras {@code falhasIniciais} invocacoes e registra
     * todos os tokens recebidos, para que o teste possa inspecionar o retry.
     */
    static class ChamadaExternaFake implements ChamadaExterna {
        private final int falhasIniciais;
        private final List<String> tokensRecebidos = new ArrayList<>();

        ChamadaExternaFake(int falhasIniciais) {
            this.falhasIniciais = falhasIniciais;
        }

        @Override
        @TokenRetry(maxAttempts = 3, delay = 1)
        public String executar(String token) {
            tokensRecebidos.add(token);
            if (tokensRecebidos.size() <= falhasIniciais) {
                throw HttpClientErrorException.create(
                        HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null);
            }
            return "ok";
        }

        List<String> tokensRecebidos() {
            return tokensRecebidos;
        }
    }

    private ChamadaExterna comAspect(ChamadaExternaFake alvo) {
        AspectJProxyFactory factory = new AspectJProxyFactory(alvo);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    @Test
    @DisplayName("Apos um 401, a nova tentativa deve usar o token renovado")
    void deveReenviarComTokenRenovadoAposUnauthorized() {
        when(tokenService.executar())
                .thenReturn(new RecuperarTokenEllevenOutputDTO(TOKEN_RENOVADO, 3600, "Bearer", "scope"));

        ChamadaExternaFake alvo = new ChamadaExternaFake(1);

        String resultado = comAspect(alvo).executar(TOKEN_EXPIRADO);

        assertThat(resultado).isEqualTo("ok");
        assertThat(alvo.tokensRecebidos())
                .as("a segunda tentativa precisa carregar o token novo, nao o recusado")
                .containsExactly(TOKEN_EXPIRADO, TOKEN_RENOVADO);
        verify(tokenService).invalidateToken();
    }

    @Test
    @DisplayName("Deve respeitar maxAttempts e propagar o 401 quando o token novo tambem falha")
    void devePropagarQuandoEsgotaTentativas() {
        when(tokenService.executar())
                .thenReturn(new RecuperarTokenEllevenOutputDTO(TOKEN_RENOVADO, 3600, "Bearer", "scope"));

        ChamadaExternaFake alvo = new ChamadaExternaFake(Integer.MAX_VALUE);
        ChamadaExterna proxy = comAspect(alvo);

        assertThatThrownBy(() -> proxy.executar(TOKEN_EXPIRADO))
                .isInstanceOf(HttpClientErrorException.class);

        assertThat(alvo.tokensRecebidos()).hasSize(3);
    }

    @Test
    @DisplayName("Erro que nao seja 401 deve propagar imediatamente, sem renovar token")
    void naoDeveRenovarTokenParaOutrosErros() {
        ChamadaExternaFake alvo = new ChamadaExternaFake(0) {
            @Override
            @TokenRetry(maxAttempts = 3, delay = 1)
            public String executar(String token) {
                throw new IllegalStateException("erro de negocio");
            }
        };

        ChamadaExterna proxy = comAspect(alvo);

        assertThatThrownBy(() -> proxy.executar(TOKEN_EXPIRADO))
                .isInstanceOf(IllegalStateException.class);

        verify(tokenService, org.mockito.Mockito.never()).invalidateToken();
    }
}
