package br.com.sebratel.splitters.dto.splitters;

import java.util.List;

public record AssignmentResponseDataDTO(
        String status,
        Integer count,
        List<AssignmentDetailDTO> data
) {}