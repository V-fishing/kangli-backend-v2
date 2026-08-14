package com.konli.qms.service.ncm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.service.ncm.dto.AbnormalCapaLaunchRequest;
import com.konli.qms.service.ncm.dto.CapaVo;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;

import java.util.List;

/** CAPA:创建/查询/进度更新/关闭。ncm.capa.* */
public interface NcmCapaService {

    List<QmsCapa> list();

    PageResult<QmsCapa> listPage(String keyword, int page, int size);

    CapaVo get(String id);

    QmsCapa create(QmsCapa capa);

    /** 从来料异常单发起 CAPA,并回写异常单 capaId/rectifyType/status。
     *  支持在发起时指定负责人(ownerUserId)。 */
    QmsCapa launchFromAbnormal(AbnormalCapaLaunchRequest req);

    /** 独立事务创建 CAPA(联动场景:失败不回滚调用方主事务)。 */
    QmsCapa createInNewTx(QmsCapa capa);

    /** 更新进度,progress=100 时自动置为已验证。 */
    void updateProgress(String capaId, Short progress);

    /** 关闭 CAPA。 */
    void close(String capaId);

    /** SR-CAR:审批纠正/预防措施(progress>=60且status=待审批时可调用)。驳回时progress回退至50。 */
    void approve(String capaId, boolean approved, String comment);

    /** SR-CAR:效果验证无效->重新分析。progress=0,status=分析中。 */
    void reset(String capaId, String reason);

    /** 列表级改派责任人(更新 owner_user_id/owner + 推送被指派人任务中心)。 */
    void reassign(String capaId, DefectLaunchRequest req);
}
