package com.ai.career.job.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobsFetchedEvent implements Serializable {
    private String source;
    private List<Long> jobIds;
    private int count;
}
