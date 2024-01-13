package org.vkartashov.log.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SaveLogRequest {

    private LogEntry entry;
    private int writeConcern;

}
