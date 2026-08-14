package com.konli.qms.api.system.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyConfig;
import com.konli.qms.service.notify.NotifyConfigService;
import com.konli.qms.service.uop.UserService;
import com.konli.qms.service.uop.dto.UserSelectVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/** 系统管理 - 统一通知配置读写。 */
@RestController
@RequestMapping("/api/v1/system/notify-config")
@RequiredArgsConstructor
public class NotifyConfigController {

    private final NotifyConfigService notifyConfigService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('system.notify.config')")
    public R<List<NotifyConfig>> list() {
        return R.ok(notifyConfigService.listAll());
    }

    /** 启用用户下拉(通知配置页"指定具体接收人"用, 无需 system.user.list 权限)。 */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('system.notify.config')")
    public R<List<UserSelectVo>> users() {
        return R.ok(userService.listForSelect());
    }

    @GetMapping("/channels")
    @PreAuthorize("hasAuthority('system.notify.config')")
    public R<List<NotifyChannel>> channels() {
        return R.ok(notifyConfigService.listChannels());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system.notify.config')")
    public R<Void> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String roleCodes = body.get("roleCodes") != null ? body.get("roleCodes").toString() : null;
        String receiverIds = body.get("receiverIds") != null ? body.get("receiverIds").toString() : null;
        String channels = body.get("channels") != null ? body.get("channels").toString() : null;
        Boolean enabled = body.get("enabled") != null ? Boolean.valueOf(body.get("enabled").toString()) : null;
        notifyConfigService.update(id, roleCodes, receiverIds, channels, enabled);
        return R.ok();
    }

    @PutMapping("/channels/{id}")
    @PreAuthorize("hasAuthority('system.notify.config')")
    public R<Void> updateChannel(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String webhook = body.get("webhookUrl") != null ? body.get("webhookUrl").toString() : null;
        Boolean enabled = body.get("enabled") != null ? Boolean.valueOf(body.get("enabled").toString()) : null;
        String channelType = body.get("channelType") != null ? body.get("channelType").toString() : null;
        String configJson = null;
        Object cfg = body.get("configJson");
        if (cfg != null) {
            if (cfg instanceof String s) {
                configJson = s;
            } else {
                try {
                    configJson = om.writeValueAsString(cfg);
                } catch (Exception e) {
                    return R.fail(400, "configJson 格式错误");
                }
            }
        }
        notifyConfigService.updateChannel(id, webhook, enabled, channelType, configJson);
        return R.ok();
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
}
