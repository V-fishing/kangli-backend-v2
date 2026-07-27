package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.dto.CapaVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** CAPA:创建/查询/进度更新/关闭。ncm.capa.list / ncm.capa.create */
@RestController
@RequestMapping("/api/v1/ncm/capas")
@RequiredArgsConstructor
public class NcmCapaController {

    private final NcmCapaService ncmCapaService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.capa.list')")
    public R<List<QmsCapa>> list() {
        return R.ok(ncmCapaService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.capa.list')")
    public R<CapaVo> get(@PathVariable String id) {
        return R.ok(ncmCapaService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.capa.create')")
    public R<QmsCapa> create(@RequestBody QmsCapa capa) {
        return R.ok(ncmCapaService.create(capa));
    }

    /** 从来料异常单发起 CAPA(回写异常单 capaId/rectifyType/status)。 */
    @PostMapping("/launch")
    @PreAuthorize("hasAuthority('ncm.capa.create')")
    public R<QmsCapa> launchFromAbnormal(@RequestBody QmsCapa capa) {
        return R.ok(ncmCapaService.launchFromAbnormal(capa));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('ncm.capa.create')")
    public R<Void> updateProgress(@PathVariable String id, @RequestParam Short progress) {
        ncmCapaService.updateProgress(id, progress);
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('ncm.capa.close')")
    public R<Void> close(@PathVariable String id) {
        ncmCapaService.close(id);
        return R.ok();
    }

    /** SR-CAR:审批纠正/预防措施(progress>=60触发待审批关卡,通过->实施中,驳回->退回到分析中) */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ncm.capa.approve')")
    public R<Void> approve(@PathVariable String id, @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.parseBoolean(String.valueOf(body.getOrDefault("approved", true)));
        String comment = body.get("comment") == null ? "" : String.valueOf(body.get("comment"));
        ncmCapaService.approve(id, approved, comment);
        return R.ok();
    }

    /** SR-CAR:效果验证无效->重新分析,progress=0,status=分析中 */
    @PostMapping("/{id}/reset")
    @PreAuthorize("hasAuthority('ncm.capa.reset')")
    public R<Void> reset(@PathVariable String id) {
        ncmCapaService.reset(id, null);
        return R.ok();
    }
}
