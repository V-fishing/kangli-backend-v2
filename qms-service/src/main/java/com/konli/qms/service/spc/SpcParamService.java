package com.konli.qms.service.spc;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.spc.entity.SpcParam;

import java.util.List;

/** SPC 参数主数据 CRUD(规格限/子组大小/图表类型)。spc.param.* */
public interface SpcParamService {

    /**
     * 查询全部 SPC 参数(并回填展示字段与产品关联)。
     * @param productName 可选产品名过滤:选中时返回「绑定该产品的参数 + 未绑定任何产品的通用参数」;为空不按产品过滤。
     * @param procName    可选工序名过滤:选中时仅返回工序名/工艺名匹配的参数;为空不按工序过滤。
     * @param paramSource 可选来源过滤:FIA_FIRST/MANUAL/SAMPLE/TOOLING;为空不按来源过滤。
     * @param srcWoNo     可选来源工单号过滤:选中时仅返回该工单下的参数(首件/抽样参数均带 srcWoNo 口径一致);为空不按工单过滤。
     * 各维度可独立使用,也可组合(取交集)。
     */
    List<SpcParam> list(String productName, String procName, String paramSource, String srcWoNo);

    /** 分页版本(内存过滤+分页,因 list 含复杂关联回填)。 */
    PageResult<SpcParam> listPage(String productName, String procName, String paramSource, String srcWoNo, String keyword, int page, int size);

    /**
     * 按 FIA 检验标准列出可联动的 SPC 参数(供创建首件任务时供用户勾选)。
     * 匹配优先级与 {@code FiaTaskServiceImpl.syncToSpc} 一致:
     * 1) SpcParam.fiaStdItemId = 标准检验项的 stdItemId 精确匹配;
     * 2) SpcParam 绑定的规格标准线(material+procName) = 该标准的 material+procName;
     * 3) 按 procName 兜底匹配。
     * 仅返回已激活参数,并回填 products 等展示字段。
     */
    List<SpcParam> listByStd(String stdId);

    /**
     * 由 FIA 检验任务一键生成 SPC 参数。
     *
     * <p>读取任务下各检验项(优先取关联标准项的结构化上下限,否则解析标准值/公差文本),
     * 仅对「可制图」(存在规格下限/上限/目标值其一)的项生成 {@code ops.spc_param} 并归属到对应工序;
     * 以 {@code src_item_id} 去重,重复调用不会重复建参。返回该任务对应的 SPC 参数列表。
     */
    List<SpcParam> ensureFromFiaTask(String taskId);

    SpcParam get(String id);

    SpcParam create(SpcParam param);

    void update(SpcParam param);

    void delete(String id);
}
