package com.konli.qms.api.system.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyMessage;
import com.konli.qms.service.notify.NotifyCenterRow;
import com.konli.qms.service.notify.NotifyMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 系统管理 - 通知中心(点对点通知手动发起 + 发送记录查询)。 */
@RestController
@RequestMapping("/api/v1/system/notify-messages")
@RequiredArgsConstructor
public class NotifyMessageController {

    private final NotifyMessageService notifyMessageService;

    /** 手动发起通知(向多接收人按渠道点对点发送)。 */
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('system.notify.center')")
    public R<Integer> send(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> receiverIds = body.get("receiverIds") != null
                ? (List<String>) body.get("receiverIds") : List.of();
        @SuppressWarnings("unchecked")
        List<String> channels = body.get("channels") != null
                ? (List<String>) body.get("channels") : List.of();
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        String bizType = body.get("bizType") != null ? body.get("bizType").toString() : "MANUAL";
        String bizId = body.get("bizId") != null ? body.get("bizId").toString() : null;
        String bizNo = body.get("bizNo") != null ? body.get("bizNo").toString() : null;
        int n = notifyMessageService.send(title, content, receiverIds, channels, bizType, bizId, bizNo);
        return R.ok(n);
    }

    /** 发送记录分页查询(状态/渠道/关键字筛选)。 */
    @GetMapping
    @PreAuthorize("hasAuthority('system.notify.center')")
    public R<PageResult<NotifyMessage>> list(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String channel,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return R.ok(notifyMessageService.list(status, channel, keyword, page, size));
    }

    /** 点对点渠道列表(凭据已脱敏, 供通知中心发送时选择)。 */
    @GetMapping("/channels")
    @PreAuthorize("hasAuthority('system.notify.center')")
    public R<List<NotifyChannel>> channels() {
        return R.ok(notifyMessageService.listDirectChannels());
    }

    /** 通知中心统一视图(以通知为粒度, 聚合多渠道投递明细)。 */
    @GetMapping("/center/page")
    @PreAuthorize("hasAuthority('system.notify.center')")
    public R<PageResult<NotifyCenterRow>> centerPage(@RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String channel,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(notifyMessageService.centerPage(status, channel, keyword, page, size));
    }
}
