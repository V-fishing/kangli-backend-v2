package com.konli.qms.service.fia;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.fia.dto.CreateInspStdRequest;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.service.fia.dto.CtqItemVo;
import com.konli.qms.service.fia.dto.InspStdVo;

import java.util.List;

public interface InspStdService {

    List<FiaInspStd> list();

    /** 按关键字(编码/物料/料号)模糊查询标准,limit<=0 表示不限制条数 */
    List<FiaInspStd> listByKeyword(String keyword, int limit);

    PageResult<FiaInspStd> listPage(String keyword, int page, int size);

    InspStdVo get(String id);

    FiaInspStd create(FiaInspStd std, List<FiaInspStdItem> items);

    void update(String id, CreateInspStdRequest req);

    void delete(String id);

    /** 启用/停用标准(仅改 status:生效/停用),不删除明细与历史数据 */
    void changeStatus(String id, String status);

    /** 查询所有生效标准下的 CTQ 检验项(供 SPC 参数关联选择) */
    List<CtqItemVo> listCtqItems();
}
