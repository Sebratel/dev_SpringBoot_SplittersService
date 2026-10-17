package br.com.sebratel.splitters.dto.splitters;


public record EllevenSplitterResponseDTO<T>(
        boolean success,
        Object messages,
        T response,
        String dataResponseType,
        Object elapsedTime
) {}