package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public List<Configuration> getConfigurations(@RequestParam String serviceName,
                                                 @RequestParam(required = false) String environment,
                                                 @RequestParam(required = false) String key) {
        return configurationService.getConfigurations(serviceName, environment, key);
    }
}
