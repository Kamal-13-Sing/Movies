package com.movieflix.dto;

import java.util.List;

public record MoviePageResponse(List<MovieDto> movieDtos,
                                Integer pageNumber, Integer pageSize,
                                Integer total, Integer totalPages, boolean isLast) {


}
