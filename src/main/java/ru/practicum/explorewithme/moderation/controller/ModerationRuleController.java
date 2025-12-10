package ru.practicum.explorewithme.moderation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.moderation.dto.ModerationRuleDto;
import ru.practicum.explorewithme.moderation.service.ModerationRuleService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/moderation/rules")
public class ModerationRuleController {

    private final ModerationRuleService ruleService;

    @GetMapping
    public List<ModerationRuleDto> getAllRules(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Boolean activeOnly) {

        log.info("GET /admin/moderation/rules");
        return ruleService.getAllRules(entityType, activeOnly);
    }

    @GetMapping("/{ruleId}")
    public ModerationRuleDto getRule(@PathVariable Long ruleId) {
        log.info("GET /admin/moderation/rules/{}", ruleId);
        return ruleService.getRule(ruleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModerationRuleDto createRule(
            @Valid @RequestBody ModerationRuleDto ruleDto,
            @RequestParam Long createdById) {

        log.info("POST /admin/moderation/rules");
        return ruleService.createRule(ruleDto, createdById);
    }

    @PutMapping("/{ruleId}")
    public ModerationRuleDto updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody ModerationRuleDto ruleDto) {

        log.info("PUT /admin/moderation/rules/{}", ruleId);
        return ruleService.updateRule(ruleId, ruleDto);
    }

    @DeleteMapping("/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable Long ruleId) {
        log.info("DELETE /admin/moderation/rules/{}", ruleId);
        ruleService.deleteRule(ruleId);
    }

    @PatchMapping("/{ruleId}/toggle")
    public ModerationRuleDto toggleRule(@PathVariable Long ruleId) {
        log.info("PATCH /admin/moderation/rules/{}/toggle", ruleId);
        return ruleService.toggleRule(ruleId);
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.OK)
    public void applyRules() {
        log.info("POST /admin/moderation/rules/apply");
        ruleService.applyAllActiveRules();
    }
}