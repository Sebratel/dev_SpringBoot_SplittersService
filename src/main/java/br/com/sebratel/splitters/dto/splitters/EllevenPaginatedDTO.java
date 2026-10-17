package br.com.sebratel.splitters.dto.splitters;

import lombok.Data;

@Data
public class EllevenPaginatedDTO<T> {
    private T data;
    private int totalRecords;
    private int page;
    private int pageSize;
    private Object totalizers;
    private int totalPages;
}
