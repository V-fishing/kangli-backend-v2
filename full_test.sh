#!/bin/bash
# QMS 后端全流程全量测试
set -o pipefail
PASS=0; FAIL=0; SKIP=0
P() { echo "PASS| $1"; PASS=$((PASS+1)); }
F() { echo "FAIL| $1 -> $2"; FAIL=$((FAIL+1)); }
S() { echo "SKIP| $1 -> $2"; SKIP=$((SKIP+1)); }

API="http://localhost:8080"
MZORG="019f701f-0411-71ed-9eac-ab9440335832"

# 工具函数
login() { curl -s --noproxy "*" -X POST $API/api/v1/auth/login -H "Content-Type: application/json" -d "{\"username\":\"$1\",\"password\":\"$2\"}"; }
jval() { echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -1 | sed "s/\"$2\":\"//;s/\"//"; }
jcode() { echo "$1" | grep -o '"code":[0-9]*' | head -1 | sed 's/"code"://'; }
post() { curl -s --noproxy "*" -H "Authorization: Bearer $TK" -H "Content-Type: application/json" -X POST "$1" -d "$2"; }
get() { curl -s --noproxy "*" -H "Authorization: Bearer $TK" "$1"; }
put() { curl -s --noproxy "*" -H "Authorization: Bearer $TK" -H "Content-Type: application/json" -X PUT "$1" -d "$2"; }
del() { curl -s --noproxy "*" -H "Authorization: Bearer $TK" -X DELETE "$1"; }
hcode() { curl -s --noproxy "*" -o /dev/null -w "%{http_code}" "$@"; }

echo "========== 1. 认证 =========="
# 1.1 admin 登录
R=$(login admin admin123)
TK=$(jval "$R" accessToken)
[ -n "$TK" ] && P "admin 登录" || F "admin 登录" "无 token"

# 1.2 mzuser 登录
R=$(login mzuser user123)
[ "$(jcode "$R")" = "0" ] && P "mzuser 登录" || F "mzuser 登录" "$R"

# 1.3 错误密码
R=$(login admin wrong)
[ "$(jcode "$R")" = "401" ] && P "错误密码返回401" || F "错误密码" "$R"

# 1.4 无 token 访问受保护
HC=$(hcode -H "Authorization: Bearer" $API/api/v1/uop/users)
[ "$HC" = "401" ] && P "无token->401" || F "无token" "HTTP $HC"

# 1.5 不存在路径
R=$(get $API/api/v1/nonexistent)
[ "$(jcode "$R")" = "404" ] && P "不存在路径->404" || F "404" "$R"

echo "========== 2. UOP =========="
# 2.1 /me 权限码
R=$(get $API/api/v1/uop/me)
PERMS=$(echo "$R" | grep -o '"permissions":\[[^]]*\]')
[ -n "$PERMS" ] && P "/me 返回权限码" || F "/me" "$R"

# 2.2 用户列表
R=$(get $API/api/v1/uop/users)
CNT=$(echo "$R" | grep -o '"id":"[^"]*"' | wc -l)
[ "$CNT" -ge 2 ] && P "用户列表($CNT)" || F "用户列表" "$CNT"

# 2.3 组织树
R=$(get $API/api/v1/uop/orgs/tree)
echo "$R" | grep -q '"orgCode":"MZ"' && P "组织树含梅州" || F "组织树" "$R"

# 2.4 角色列表
R=$(get $API/api/v1/uop/roles)
echo "$R" | grep -q '"roleCode":"sysadmin"' && P "角色列表含sysadmin" || F "角色列表" "$R"

# 2.5 字典-全量
R=$(get $API/api/v1/dict)
CNT=$(echo "$R" | grep -o '"dictType"' | wc -l)
[ "$CNT" -ge 100 ] && P "字典全量($CNT)" || F "字典" "$CNT"

# 2.6 字典-按类型
R=$(get $API/api/v1/dict/severity)
echo "$R" | grep -q '"严重"' && P "字典severity含严重" || F "字典severity" "$R"

# 2.7 创建用户
R=$(post $API/api/v1/uop/users "{\"orgId\":\"$MZORG\",\"username\":\"testuser\",\"password\":\"Test1234\",\"realName\":\"测试\",\"status\":\"启用\"}")
[ "$(jcode "$R")" = "0" ] && P "创建用户" || F "创建用户" "$R"

# 2.8 重复用户名
R=$(post $API/api/v1/uop/users "{\"orgId\":\"$MZORG\",\"username\":\"testuser\",\"password\":\"Test1234\",\"realName\":\"测试\"}")
[ "$(jcode "$R")" = "400" ] && P "重复用户名->400" || F "重复用户名" "$R"

echo "========== 3. FIA =========="
# 3.1 建标准
R=$(post $API/api/v1/fia/stds "{\"orgId\":\"$MZORG\",\"code\":\"STD-FULL\",\"material\":\"FULLMAT\",\"procName\":\"FULLPROC\",\"stdVersion\":\"v1\",\"status\":\"生效\",\"items\":[{\"seq\":1,\"itemName\":\"尺寸\",\"isCtq\":true,\"stdValue\":\"10\",\"tolerance\":\"0.1\",\"unit\":\"mm\",\"valueType\":\"numeric\"}]}")
STDID=$(jval "$R" id)
[ -n "$STDID" ] && P "建标准" || F "建标准" "$R"

# 3.2 查标准(含检测项)
R=$(get $API/api/v1/fia/stds/$STDID)
echo "$R" | grep -q '"items"' && P "查标准含检测项" || F "查标准" "$R"

# 3.3 建任务
R=$(post $API/api/v1/fia/tasks "{\"orgId\":\"$MZORG\",\"woNo\":\"WO-FULL\",\"lineName\":\"L1\",\"productName\":\"P\",\"procName\":\"FULLPROC\",\"triggerType\":\"换模具\",\"stdId\":\"$STDID\",\"batchNo\":\"BF\"}")
TASKID=$(jval "$R" id)
[ -n "$TASKID" ] && P "建任务" || F "建任务" "$R"

# 3.4 录入结果
ITEMID=$(get $API/api/v1/fia/tasks/$TASKID | grep -o '"items":\[{"id":"[^"]*"' | grep -o '"id":"[^"]*"' | sed 's/"id":"//;s/"//')
R=$(post $API/api/v1/fia/tasks/$TASKID/items "{\"items\":[{\"id\":\"$ITEMID\",\"measuredValue\":\"10.05\",\"judge\":\"合格\"}]}")
[ "$(jcode "$R")" = "0" ] && P "录入结果" || F "录入结果" "$R"

# 3.5 检验签名(密码)
R=$(post $API/api/v1/fia/tasks/$TASKID/sign-inspector "{\"password\":\"admin123\"}")
[ "$(jcode "$R")" = "0" ] && P "检验签名" || F "检验签名" "$R"

# 3.6 复核签名
R=$(post $API/api/v1/fia/tasks/$TASKID/sign-reviewer "{\"password\":\"admin123\"}")
[ "$(jcode "$R")" = "0" ] && P "复核签名" || F "复核签名" "$R"

# 3.7 终态
R=$(get $API/api/v1/fia/tasks/$TASKID)
echo "$R" | grep -q '"status":"已完成"' && P "任务已完成" || F "任务终态" "$R"
echo "$R" | grep -q '"overallJudge":"合格"' && P "overallJudge=合格" || F "overallJudge" "$R"

# 3.8 归档
R=$(get $API/api/v1/fia/tasks/$TASKID/archive)
echo "$R" | grep -q '"reportHash"' && P "归档报告含hash" || F "归档" "$R"

# 3.9 签名配置
R=$(get "$API/api/v1/fia/sign-config?orgId=$MZORG")
echo "$R" | grep -q '"signMethods"' && P "签名配置读取" || F "签名配置" "$R"

# 3.10 错密码签名
R=$(post $API/api/v1/fia/tasks "{\"orgId\":\"$MZORG\",\"woNo\":\"WO-ERR\",\"lineName\":\"L\",\"productName\":\"P\",\"procName\":\"FULLPROC\",\"triggerType\":\"换模具\",\"stdId\":\"$STDID\"}")
T2=$(jval "$R" id)
post $API/api/v1/fia/tasks/$T2/items "{\"items\":[{\"id\":\"$(get $API/api/v1/fia/tasks/$T2 | grep -o '"id":"[^"]*"' | head -1 | sed 's/"id":"//;s/"//')\",\"measuredValue\":\"10\",\"judge\":\"合格\"}]}" >/dev/null
R=$(post $API/api/v1/fia/tasks/$T2/sign-inspector "{\"password\":\"wrong\"}")
[ "$(jcode "$R")" = "401" ] && P "错密码签名->401" || F "错密码" "$R"

echo "========== 4. SPC =========="
# 4.1 建参数
R=$(post $API/api/v1/spc/params "{\"orgId\":\"$MZORG\",\"paramName\":\"FULL测试\",\"procName\":\"测试\",\"unit\":\"℃\",\"specLower\":180.0,\"specUpper\":220.0,\"specText\":\"180-220\",\"targetValue\":200.0,\"subgroupSize\":5,\"collectFreq\":\"1次/30min\",\"chartType\":\"Xbar-R\",\"isActive\":true}")
PID=$(jval "$R" id)
[ -n "$PID" ] && P "建SPC参数" || F "建参数" "$R"

# 4.2 录子组(5组正常)
for t in 08:00 08:30 09:00 09:30 10:00; do
  post $API/api/v1/spc/subgroups "{\"orgId\":\"$MZORG\",\"paramId\":\"$PID\",\"subgroupTime\":\"2026-07-18T$t\",\"shift\":\"早班\",\"woNo\":\"W1\",\"batchNo\":\"B1\",\"values\":[200,201,199,200,200]}" >/dev/null
done
R=$(get $API/api/v1/spc/subgroups)
CNT=$(echo "$R" | grep -o '"xbar"' | wc -l)
[ "$CNT" -ge 5 ] && P "5组子组录入" || F "子组录入" "$CNT"

# 4.3 验证 xbar
R=$(get $API/api/v1/spc/subgroups)
echo "$R" | grep -q '"xbar":200.0000' && P "xbar=200(自动算)" || F "xbar" "$R"

# 4.4 录异常子组(触发WECO)
R=$(post $API/api/v1/spc/subgroups "{\"orgId\":\"$MZORG\",\"paramId\":\"$PID\",\"subgroupTime\":\"2026-07-18T10:30\",\"shift\":\"早班\",\"woNo\":\"W1\",\"batchNo\":\"B6\",\"values\":[215,216,214,215,215]}")
echo "$R" | grep -q '"judge":"异常"' && P "WECO判异(异常)" || F "WECO" "$R"
echo "$R" | grep -q '"outlierRule":"①"' && P "outlierRule=①" || F "outlierRule" "$R"

# 4.5 告警
R=$(get $API/api/v1/spc/alarms)
echo "$R" | grep -q '"triggeredRule":"①"' && P "告警含①" || F "告警" "$R"

# 4.6 CPK
R=$(post "$API/api/v1/spc/capability/calc?paramId=$PID&periodType=month&periodValue=2026-07")
echo "$R" | grep -q '"cpk"' && P "CPK计算" || F "CPK" "$R"

# 4.7 规则列表
R=$(get $API/api/v1/spc/rules)
echo "$R" | grep -q '"ruleCode":"①"' && P "规则列表" || F "规则" "$R"

echo "========== 5. NCM =========="
# 5.1 不良字典
R=$(post $API/api/v1/ncm/defect-dicts "{\"orgId\":\"$MZORG\",\"code\":\"DFULL\",\"name\":\"全量测试不良\",\"category\":\"尺寸类\",\"level\":\"严重\",\"status\":\"启用\"}")
[ "$(jcode "$R")" = "0" ] && P "建不良字典" || F "不良字典" "$R"

# 5.2 不良记录
R=$(post $API/api/v1/ncm/defect-records "{\"orgId\":\"$MZORG\",\"woNo\":\"WO-NCM-F\",\"processCode\":\"注塑\",\"defectDictCode\":\"DFULL\",\"severity\":\"严重\",\"defectCount\":3,\"batchTotal\":100,\"deviceCode\":\"D1\",\"batchNo\":\"BN\",\"productModel\":\"PM\",\"source\":\"手动\"}")
echo "$R" | grep -q '"defectNo":"DF-' && P "不良记录(自动编号)" || F "不良记录" "$R"
echo "$R" | grep -q '"defectRate":0.0300' && P "defectRate=0.03(自动算)" || F "defectRate" "$R"

# 5.3 8D D1->D8
R=$(post $API/api/v1/ncm/8d-reports "{\"orgId\":\"$MZORG\",\"source\":\"不良记录\",\"issue\":\"全量测试8D\",\"severity\":\"严重\"}")
D8ID=$(jval "$R" id)
[ -n "$D8ID" ] && P "建8D报告" || F "8D" "$R"
ALLOK=1
for stage in D1 D2 D3 D4 D5 D6 D7 D8; do
  R=$(post $API/api/v1/ncm/8d-reports/$D8ID/advance "{\"stageCode\":\"$stage\",\"content\":\"$stage\",\"owner\":\"张三\"}")
  [ "$(jcode "$R")" = "0" ] || { ALLOK=0; F "8D $stage" "$R"; }
done
[ $ALLOK = 1 ] && P "8D D1->D8 全流转" || F "8D流转" "部分失败"

R=$(get $API/api/v1/ncm/8d-reports/$D8ID)
echo "$R" | grep -q '"currentStage":"D8"' && P "8D currentStage=D8" || F "8D终态" "$R"
echo "$R" | grep -q '"status":"已闭环"' && P "8D 已闭环" || F "8D闭环" "$R"

# 5.4 CAPA
R=$(post $API/api/v1/ncm/capas "{\"orgId\":\"$MZORG\",\"issue\":\"根因\",\"triggerType\":\"手动\",\"capaType\":\"纠正\",\"owner\":\"王五\",\"dueDate\":\"2026-09-01\"}")
CID=$(jval "$R" id)
[ -n "$CID" ] && P "建CAPA" || F "CAPA" "$R"
post "$API/api/v1/ncm/capas/$CID/progress?progress=100" >/dev/null
post "$API/api/v1/ncm/capas/$CID/close" >/dev/null
R=$(get $API/api/v1/ncm/capas/$CID)
echo "$R" | grep -q '"status":"已关闭"' && P "CAPA 已关闭" || F "CAPA关闭" "$R"

echo "========== 6. SQM =========="
# 6.1 供应商
R=$(post $API/api/v1/sqm/suppliers "{\"orgId\":\"$MZORG\",\"supplierNo\":\"SUP-FULL\",\"supplierCode\":\"SF\",\"name\":\"全量供应商\",\"creditCode\":\"9111FULL\",\"category\":\"电子元器件\",\"level\":\"A\",\"status\":\"合格\"}")
SQID=$(jval "$R" id)
[ -n "$SQID" ] && P "建供应商" || F "供应商" "$R"

# 6.2 审核计划
R=$(post $API/api/v1/sqm/audits/plans "{\"orgId\":\"$MZORG\",\"supplierId\":\"$SQID\",\"auditType\":\"年度复审\",\"planDate\":\"2026-08-01\",\"auditLead\":\"审\",\"scope\":\"全\",\"riskLevel\":\"中\",\"status\":\"计划中\"}")
[ "$(jcode "$R")" = "0" ] && P "建审核计划" || F "审核计划" "$R"

# 6.3 审核记录
R=$(post $API/api/v1/sqm/audits/records "{\"orgId\":\"$MZORG\",\"planId\":\"$(jval "$R" id)\",\"supplierId\":\"$SQID\",\"auditType\":\"年度复审\",\"auditDate\":\"2026-08-01\",\"auditLead\":\"审\",\"result\":\"通过\",\"score\":90,\"ncCount\":0,\"conclusion\":\"符合\",\"status\":\"执行中\"}")
[ "$(jcode "$R")" = "0" ] && P "建审核记录" || F "审核记录" "$R"

# 6.4 变更三方会签
R=$(post $API/api/v1/sqm/changes "{\"orgId\":\"$MZORG\",\"title\":\"全量变更\",\"supplierId\":\"$SQID\",\"partNo\":\"PF\",\"changeType\":\"材料升级\",\"reason\":\"测试\",\"applicant\":\"赵\",\"applyDate\":\"2026-07-18\",\"urgency\":\"中\",\"source\":\"门户提报\"}")
COID=$(jval "$R" id)
[ -n "$COID" ] && P "建变更单" || F "变更单" "$R"
post $API/api/v1/sqm/changes/$COID/submit >/dev/null
AOK=1
for role in quality purchase rd; do
  R=$(post $API/api/v1/sqm/changes/$COID/approve "{\"approvalRole\":\"$role\",\"approved\":true,\"opinion\":\"同意\"}")
  [ "$(jcode "$R")" = "0" ] || { AOK=0; F "变更审批 $role" "$R"; }
done
[ $AOK = 1 ] && P "变更三方会签全通过" || F "变更会签" "部分失败"
R=$(get $API/api/v1/sqm/changes/$COID)
echo "$R" | grep -q '"status":"已批准"' && P "变更状态=已批准" || F "变更状态" "$R"

# 6.5 来料+追溯
R=$(post $API/api/v1/sqm/lots "{\"orgId\":\"$MZORG\",\"supplierId\":\"$SQID\",\"partNo\":\"PF\",\"partName\":\"零件F\",\"qty\":100,\"unit\":\"pcs\",\"incomingDate\":\"2026-07-18\",\"inspectResult\":\"合格\",\"inspectType\":\"正常\",\"poNo\":\"POF\",\"isKeyPart\":false}")
LOTID=$(jval "$R" id)
[ -n "$LOTID" ] && P "来料批次" || F "来料" "$R"
R=$(post $API/api/v1/sqm/trace/nodes "{\"orgId\":\"$MZORG\",\"rootLotId\":\"$LOTID\",\"nodeType\":\"incoming\",\"nodeName\":\"来料\",\"batchNo\":\"BF\",\"qty\":100,\"unit\":\"pcs\",\"treeLevel\":0,\"isValid\":\"是\"}")
[ "$(jcode "$R")" = "0" ] && P "追溯节点" || F "追溯节点" "$R"
R=$(get "$API/api/v1/sqm/trace/tree?rootLotId=$LOTID")
echo "$R" | grep -q '"nodeType":"incoming"' && P "追溯树" || F "追溯树" "$R"

# 6.6 异常
R=$(post $API/api/v1/sqm/abnormals "{\"orgId\":\"$MZORG\",\"lotId\":\"LOT-FULL\",\"supplierId\":\"$SQID\",\"partNo\":\"PF\",\"partName\":\"零件F\",\"description\":\"不良\",\"qty\":2,\"level\":\"一般\",\"occurDate\":\"2026-07-18\"}")
ABID=$(jval "$R" id)
[ -n "$ABID" ] && P "建异常" || F "异常" "$R"
R=$(post $API/api/v1/sqm/abnormals/$ABID/close "{\"disposal\":\"退货\",\"disposalRemark\":\"已退\"}")
[ "$(jcode "$R")" = "0" ] && P "关闭异常" || F "关闭异常" "$R"

# 6.7 FMEA
R=$(post $API/api/v1/sqm/fmea "{\"orgId\":\"$MZORG\",\"fmeaType\":\"PFMEA\",\"product\":\"产品F\",\"process\":\"注塑\",\"failureMode\":\"尺寸超差\",\"severityS\":8,\"occurrenceO\":3,\"detectionD\":5,\"status\":\"待闭环\",\"action\":\"检测\",\"owner\":\"钱\",\"targetDate\":\"2026-09-01\"}")
FMID=$(jval "$R" id)
[ -n "$FMID" ] && P "建FMEA" || F "FMEA" "$R"
R=$(get $API/api/v1/sqm/fmea)
echo "$R" | grep -q '"rpn":120' && P "FMEA RPN=120(8x3x5)" || F "RPN" "$R"
echo "$R" | grep -q '"highRiskFlag":true' && P "FMEA highRiskFlag=true" || F "highRisk" "$R"
R=$(post $API/api/v1/sqm/fmea/$FMID/close)
[ "$(jcode "$R")" = "0" ] && P "关闭FMEA" || F "关闭FMEA" "$R"

echo "========== 7. dataScope =========="
# admin 看全部用户
R=$(get $API/api/v1/uop/users)
ACNT=$(echo "$R" | grep -o '"id":"[^"]*"' | wc -l)
# mzuser 看本公司用户
MZTK=$(jval "$(login mzuser user123)" accessToken)
R=$(curl -s --noproxy "*" -H "Authorization: Bearer $MZTK" $API/api/v1/uop/users)
MCNT=$(echo "$R" | grep -o '"id":"[^"]*"' | wc -l)
[ "$ACNT" -gt "$MCNT" ] && P "dataScope: admin($ACNT) > mzuser($MCNT)" || F "dataScope" "admin=$ACNT mzuser=$MCNT"

echo "========== 8. 权限 =========="
# 列出所有权限码
R=$(get $API/api/v1/uop/me)
PERMS=$(echo "$R" | grep -o '"permissions":\[[^]]*\]' | sed 's/"permissions"://;s/\[//;s/\]//;s/"//g')
echo "  admin 权限码: $(echo $PERMS | tr ',' ' ' | wc -w) 个"
echo "$PERMS" | grep -q "fia.task.submit" && P "含fia.task.submit" || F "缺fia权限" ""
echo "$PERMS" | grep -q "spc.subgroup.create" && P "含spc.subgroup.create" || F "缺spc权限" ""
echo "$PERMS" | grep -q "ncm.8d.create" && P "含ncm.8d.create" || F "缺ncm权限" ""
echo "$PERMS" | grep -q "sqm.change.create" && P "含sqm.change.create" || F "缺sqm权限" ""

echo ""
echo "========== 汇总 =========="
echo "PASS: $PASS  FAIL: $FAIL  SKIP: $SKIP"
echo "TOTAL: $((PASS+FAIL+SKIP))"
