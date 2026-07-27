package com.konli.qms.api.patrol.controller;

import com.konli.qms.api.patrol.dto.CreateRouteRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.patrol.entity.PatlCheckItem;
import com.konli.qms.domain.patrol.entity.PatlCheckpoint;
import com.konli.qms.domain.patrol.entity.PatlRoute;
import com.konli.qms.service.patrol.PatlRouteService;
import com.konli.qms.service.patrol.dto.CheckpointVo;
import com.konli.qms.service.patrol.dto.PatlRouteVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/** 巡检路线 CRUD(路线 + 点位 + 检查项)。patl.route.list / patl.route.create */
@RestController
@RequestMapping("/api/v1/patrol/routes")
@RequiredArgsConstructor
public class PatlRouteController {

    private final PatlRouteService patlRouteService;

    @GetMapping
    @PreAuthorize("hasAuthority('patl.route.list')")
    public R<List<PatlRoute>> list() {
        return R.ok(patlRouteService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('patl.route.list')")
    public R<PatlRouteVo> get(@PathVariable String id) {
        return R.ok(patlRouteService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('patl.route.create')")
    public R<PatlRoute> create(@RequestBody CreateRouteRequest req) {
        PatlRoute route = new PatlRoute();
        route.setOrgId(req.getOrgId());
        route.setRouteCode(req.getRouteCode());
        route.setRouteName(req.getRouteName());
        route.setProcName(req.getProcName());
        route.setFreq(req.getFreq());
        route.setStatus(req.getStatus());

        List<CheckpointVo> checkpoints = new ArrayList<>();
        if (req.getCheckpoints() != null) {
            for (CreateRouteRequest.CheckpointInput ci : req.getCheckpoints()) {
                PatlCheckpoint cp = new PatlCheckpoint();
                cp.setSeq(ci.getSeq());
                cp.setPointName(ci.getPointName());
                cp.setLocation(ci.getLocation());
                cp.setNeedPhoto(ci.getNeedPhoto());

                List<PatlCheckItem> items = new ArrayList<>();
                if (ci.getItems() != null) {
                    for (CreateRouteRequest.ItemInput ii : ci.getItems()) {
                        PatlCheckItem item = new PatlCheckItem();
                        item.setSeq(ii.getSeq());
                        item.setItemName(ii.getItemName());
                        item.setCheckType(ii.getCheckType());
                        item.setStdValue(ii.getStdValue());
                        item.setEnumValues(ii.getEnumValues());
                        item.setIsRequired(ii.getIsRequired());
                        items.add(item);
                    }
                }
                CheckpointVo cvo = new CheckpointVo();
                cvo.setCheckpoint(cp);
                cvo.setItems(items);
                checkpoints.add(cvo);
            }
        }
        return R.ok(patlRouteService.create(route, checkpoints));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('patl.route.create')")
    public R<Void> update(@PathVariable String id, @RequestBody PatlRoute route) {
        route.setId(id);
        patlRouteService.update(route);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('patl.route.delete')")
    public R<Void> delete(@PathVariable String id) {
        patlRouteService.delete(id);
        return R.ok();
    }
}
