package com.ying.learneyjourney.dto.response;

import lombok.Data;

@Data
public class OverallProgressResponse {
    private long progress;
    private long completed;
    private long overall;
}
