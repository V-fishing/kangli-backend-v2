package com.konli.qms.service.my;

import com.konli.qms.common.dto.MyTaskDTO;

import java.util.List;

/**
 * 跨模块"我的任务"聚合服务:按当前登录用户聚合被指派/作为负责人且未闭环的任务。
 */
public interface MyTaskService {

    /**
     * 当前登录用户被指派的任务(跨 FIA / NCM 8D·CAPA·CA / 巡检 聚合)。
     *
     * @param limit         可选上限:仅返回前 limit 条(服务端截断),不传或 <=0 则返回全部。
     * @param includeClosed 是否包含已闭环任务。默认 false 仅返回未闭环(保持原有工作台行为);
     *                      true 时返回该用户全部任务(含已闭环),用于"历史任务"视图。
     */
    List<MyTaskDTO> myTasks(Integer limit, Boolean includeClosed);
}
