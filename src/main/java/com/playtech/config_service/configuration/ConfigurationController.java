package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.ConfigurationRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configurations")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping
    public Long createConfiguration(@RequestBody ConfigurationRequest configurationRequest) {
        return configurationService.createConfiguration(configurationRequest);
    }
}
