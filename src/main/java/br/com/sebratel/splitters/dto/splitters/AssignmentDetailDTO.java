package br.com.sebratel.splitters.dto.splitters;

public record AssignmentDetailDTO(
        Long assignmentId,
        String title,
        Long protocol,
        String status,
        String team,
        String sectorArea,
        String beginningData,
        String finalData
) {}