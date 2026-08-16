package com.ai.career.browser.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoverFormRequest {

    @NotBlank(message = "URL is required")
    private String url;
}
