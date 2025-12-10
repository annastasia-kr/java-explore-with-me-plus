package ru.practicum.explorewithme.moderation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.moderation.dto.ModerationSettings;
import ru.practicum.explorewithme.moderation.service.ModerationSettingsService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/moderation/settings")
public class ModerationSettingsController {

    private final ModerationSettingsService settingsService;

    @GetMapping
    public ModerationSettings getSettings() {
        log.info("GET /admin/moderation/settings");
        return settingsService.getSettings();
    }

    @PutMapping
    public ModerationSettings updateSettings(@Valid @RequestBody ModerationSettings settings) {
        log.info("PUT /admin/moderation/settings");
        return settingsService.updateSettings(settings);
    }

    @PostMapping("/reset")
    public ModerationSettings resetToDefaults() {
        log.info("POST /admin/moderation/settings/reset");
        return settingsService.updateSettings(settingsService.getDefaultSettings());
    }
}