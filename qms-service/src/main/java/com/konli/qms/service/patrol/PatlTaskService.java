package com.konli.qms.service.patrol;

import com.konli.qms.domain.patrol.entity.PatlAbnormal;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.service.patrol.dto.PatlTaskVo;

import java.util.List;

/** 巡检任务:按路线生成/提交点位结果/关闭/异常处理。patl.task.list / patl.task.create */
public interface PatlTaskService {

    List<PatlTask> list();

    /** 详情:任务 + 记录。 */
    PatlTaskVo get(String id);

    /** 按路线生成任务(复制点位为空记录)。 */
    PatlTask create(String orgId, String routeId, String shift, String planTime);

    /** 提交一个点位的结果(更新 done_points/abnormal_count,异常则创建 patl_abnormal)。 */
    void submitRecord(String taskId, String checkpointId, String checkpointName, String result, String remark, String operatorId);

    /** 关闭任务。 */
    void close(String taskId);

    /** 异常列表。 */
    List<PatlAbnormal> listAbnormals();

    /** 关闭异常。 */
    void closeAbnormal(String id, String handleRemark);
}
