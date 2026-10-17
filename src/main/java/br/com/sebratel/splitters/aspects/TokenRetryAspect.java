package br.com.sebratel.splitters.aspects;

import br.com.sebratel.splitters.annotations.TokenRetry;
import br.com.sebratel.splitters.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Aspect
@Component
@Slf4j
public class TokenRetryAspect {


    private final RecuperarTokenDoUsuarioIntegradorEllevenService tokenService;
    @Autowired
    public TokenRetryAspect(RecuperarTokenDoUsuarioIntegradorEllevenService tokenService) {
        this.tokenService = tokenService;
    }

    @Around("@annotation(tokenRetry)")
    public Object handleTokenRetry(ProceedingJoinPoint joinPoint, TokenRetry tokenRetry) throws Throwable {
        int attempts = 0;
        long delay = tokenRetry.delay();
        int maxAttempts = tokenRetry.maxAttempts();
        Throwable lastException = null;
        // Os argumentos precisam ser mantidos fora do laco: apos um 401 o token e trocado
        // aqui dentro e a nova tentativa tem que receber o array atualizado. Chamar
        // proceed() sem argumentos reexecutaria o metodo com o token antigo.
        Object[] args = joinPoint.getArgs();

        while (attempts < maxAttempts) {
            log.info("Aspect try on call {} attempt", attempts);
            try {
                return joinPoint.proceed(args);
            } catch (Exception e) {
                attempts++;
                lastException = e;

                if (isUnauthorized(e)) {
                    log.warn("Tentativa {}/{} falhou com 401. Invalidando token em cache e tentando novamente...", attempts, maxAttempts);
                    if (attempts >= maxAttempts) {
                        log.error("Limite de retentativas atingido para o token.");
                        break;
                    }

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrompida durante o delay de retry", ie);
                    }

                    tokenService.invalidateToken();
                    String newToken = tokenService.executar().accessToken();
                    updateTokenInArgs(joinPoint, args, newToken);
                    continue;
                }
                throw e;
            }
        }
        assert lastException != null;
        throw lastException;
    }

    private boolean isUnauthorized(Throwable e) {
        if (e instanceof HttpStatusCodeException) {
            return ((HttpStatusCodeException) e).getStatusCode() == HttpStatus.UNAUTHORIZED;
        }
        return e.getClass().getSimpleName().contains("Unauthorized");
    }

    private void updateTokenInArgs(ProceedingJoinPoint joinPoint, Object[] args, String newToken) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            if ("token".equalsIgnoreCase(parameterNames[i])) {
                args[i] = newToken;
                break;
            }
        }
    }
}