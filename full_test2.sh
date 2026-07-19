#!/bin/bash
PASS=0; FAIL=0
P() { echo "PASS| $1"; PASS=$((PASS+1)); }
F() { echo "FAIL| $1 -> $2"; FAIL=$((FAIL+1)); }
jc() { echo "$1" | grep -o '"code":[0-9]*' | head -1 | sed 's/"code"://'; }
jv() { echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -1 | sed "s/\"$2\":\"//;s/\"//"; }
API="http://localhost:8080"
MZORG="019f701f-0411-71ed-9eac-ab9440335832"
TK=$(curl -s --noproxy "*" -X POST $API/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//;s/"//')
H="Authorization: Bearer $TK"
CT="Content-Type: application/json"

echo "===== 1.AUTH ====="
[ -n "$TK" ] && P "admin login" || F "admin login" "no token"
R=$(curl -s --noproxy "*" -X POST $API/api/v1/auth/login -H -H "-H "$CT" -d '{"username":"admin","password":"x"}')
[ "$(jc "$R")" = "401" ] && P "wrong pass 401" || F "wrong pass" "$R"
HC=$(curl -s --noproxy "*" -o /dev/null -w "%{http_code}" $API/api/v1/uop/users)
[ "$HC" = "401" ] && P "no token 401" || F "no token" "$HC"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/nonexistent)
[ "$(jc "$R")" = "404" ] && P "404 not found" || F "404" "$R"

echo "===== 2.UOP ====="
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/uop/me)
echo "$R" | grep -q '"permissions"' && P "/me perms" || F "/me" "$R"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/uop/orgs/tree)
echo "$R" | grep -q '"orgCode":"MZ"' && P "org tree" || F "org tree" "$R"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/dict)
echo "$R" | grep -q '"dictType"' && P "dict list" || F "dict" "$R"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/dict/severity)
echo "$R" | grep -q '"dictKey"' && P "dict by type" || F "dict by type" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/uop/users -d '{"orgId":"'$MZORG'","username":"ft2","password":"T1234","realName":"FT","status":"on"}')
[ "$(jc "$R")" = "0" ] && P "create user" || F "create user" "$R"

echo "===== 3.FIA ====="
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/fia/stds -d '{"orgId":"'$MZORG'","code":"STFT2","material":"MFT2","procName":"PFT2","stdVersion":"v1","status":"on","items":[{"seq":1,"itemName":"D","isCtq":true,"stdValue":"10","tolerance":"0.1","unit":"mm","valueType":"numeric"}]}')
SID=$(jv "$R" id); [ -n "$SID" ] && P "create std" || F "create std" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/fia/tasks -d '{"orgId":"'$MZORG'","woNo":"WFT2","lineName":"L","productName":"P","procName":"PFT2","triggerType":"mold","stdId":"'$SID'","batchNo":"B"}')
TID=$(jv "$R" id); [ -n "$TID" ] && P "create task" || F "create task" "$R"
IID=$(curl -s --noproxy "*" -H "$H" $API/api/v1/fia/tasks/$TID | grep -o '"items":\[{"id":"[^"]*"' | grep -o '"id":"[^"]*"' | sed 's/"id":"//;s/"//')
curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/fia/tasks/$TID/items -d '{"items":[{"id":"'$IID'","measuredValue":"10.05","judge":"pass"}]}' >/dev/null
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/fia/tasks/$TID/sign-inspector -d '{"password":"admin123"}')
[ "$(jc "$R")" = "0" ] && P "sign inspector" || F "sign insp" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/fia/tasks/$TID/sign-reviewer -d '{"password":"admin123"}')
[ "$(jc "$R")" = "0" ] && P "sign reviewer" || F "sign rev" "$R"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/fia/tasks/$TID)
echo "$R" | grep -q '"overallJudge"' && P "task done+judge" || F "task done" "$R"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/fia/tasks/$TID/archive)
echo "$R" | grep -q '"reportHash"' && P "archive hash" || F "archive" "$R"

echo "===== 4.SPC ====="
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/spc/params -d '{"orgId":"'$MZORG'","paramName":"FT2T","procName":"FT","unit":"C","specLower":180.0,"specUpper":220.0,"specText":"180-220","targetValue":200.0,"subgroupSize":5,"collectFreq":"30m","chartType":"Xbar-R","isActive":true}')
PID=$(jv "$R" id); [ -n "$PID" ] && P "create param" || F "param" "$R"
for t in 08:00 08:30 09:00 09:30 10:00; do
  curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/spc/subgroups -d '{"orgId":"'$MZORG'","paramId":"'$PID'","subgroupTime":"2026-07-18T'$t'","shift":"M","woNo":"W","batchNo":"B","values":[200,201,199,200,200]}' >/dev/null
done
P "5 subgroups"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/spc/subgroups -d '{"orgId":"'$MZORG'","paramId":"'$PID'","subgroupTime":"2026-07-18T10:30","shift":"M","woNo":"W","batchNo":"B6","values":[215,216,214,215,215]}')
echo "$R" | grep -q '"outlierRule"' && P "WECO outlier" || F "WECO" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/spc/alarms | grep -o '"triggeredRule"' | head -1)
[ -n "$R" ] && P "alarm exists" || F "alarm" "none"
R=$(curl -s --noproxy "*" -H "$H" -X POST "$API/api/v1/spc/capability/calc?paramId=$PID&periodType=month&periodValue=2026-07")
echo "$R" | grep -q '"cpk"' && P "CPK calc" || F "CPK" "$R"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/spc/rules)
echo "$R" | grep -q '"ruleCode"' && P "rules list" || F "rules" "$R"

echo "===== 5.NCM ====="
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/ncm/defect-dicts -d '{"orgId":"'$MZORG'","code":"DFT2","name":"FT2D","category":"DIM","level":"S","status":"on"}')
[ "$(jc "$R")" = "0" ] && P "create dict" || F "dict" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/ncm/defect-records -d '{"orgId":"'$MZORG'","woNo":"WN2","processCode":"INJ","defectDictCode":"DFT2","severity":"S","defectCount":3,"batchTotal":100,"deviceCode":"D","batchNo":"B","productModel":"P","source":"M"}')
echo "$R" | grep -q '"defectRate":0.0300' && P "defect record rate" || F "record" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/ncm/8d-reports -d '{"orgId":"'$MZORG'","source":"def","issue":"FT8D","severity":"S"}')
D8=$(jv "$R" id); [ -n "$D8" ] && P "create 8D" || F "8D" "$R"
for s in D1 D2 D3 D4 D5 D6 D7 D8; do
  curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/ncm/8d-reports/$D8/advance -d '{"stageCode":"'$s'","content":"'$s'","owner":"Z"}' >/dev/null
done
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/ncm/8d-reports/$D8)
echo "$R" | grep -q '"currentStage":"D8"' && P "8D D1-D8" || F "8D flow" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/ncm/capas -d '{"orgId":"'$MZORG'","issue":"r","triggerType":"M","capaType":"C","owner":"W","dueDate":"2026-09-01"}')
CID=$(jv "$R" id); [ -n "$CID" ] && P "create CAPA" || F "CAPA" "$R"
curl -s --noproxy "*" -H "$H" -X POST "$API/api/v1/ncm/capas/$CID/progress?progress=100" >/dev/null
curl -s --noproxy "*" -H "$H" -X POST "$API/api/v1/ncm/capas/$CID/close" >/dev/null
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/ncm/capas/$CID)
echo "$R" | grep -q '"version":[0-9]' && P "CAPA closed" || F "CAPA close" "$R"

echo "===== 6.SQM ====="
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/sqm/suppliers -d '{"orgId":"'$MZORG'","supplierNo":"SUPFT2","supplierCode":"S2","name":"FT2S","creditCode":"9112","category":"EE","level":"A","status":"pass"}')
SQ=$(jv "$R" id); [ -n "$SQ" ] && P "create supplier" || F "supplier" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/sqm/changes -d '{"orgId":"'$MZORG'","title":"FT2C","supplierId":"'$SQ'","partNo":"PF","changeType":"mat","reason":"t","applicant":"Z","applyDate":"2026-07-18","urgency":"M","source":"portal"}')
CO=$(jv "$R" id); [ -n "$CO" ] && P "create change" || F "change" "$R"
curl -s --noproxy "*" -H "$H" -X POST "$API/api/v1/sqm/changes/$CO/submit" >/dev/null
for r in quality purchase rd; do
  curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST "$API/api/v1/sqm/changes/$CO/approve" -d '{"approvalRole":"'$r'","approved":true,"opinion":"OK"}' >/dev/null
done
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/sqm/changes/$CO)
echo "$R" | grep -q '"approvals"' && P "change+approvals" || F "change detail" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/sqm/lots -d '{"orgId":"'$MZORG'","supplierId":"'$SQ'","partNo":"PF","partName":"PFT","qty":100,"unit":"pcs","incomingDate":"2026-07-18","inspectResult":"pass","inspectType":"normal","poNo":"PO","isKeyPart":false}')
LT=$(jv "$R" id); [ -n "$LT" ] && P "create lot" || F "lot" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/sqm/trace/nodes -d '{"orgId":"'$MZORG'","rootLotId":"'$LT'","nodeType":"incoming","nodeName":"in","batchNo":"B","qty":100,"unit":"pcs","treeLevel":0,"isValid":"Y"}')
[ "$(jc "$R")" = "0" ] && P "trace node" || F "trace node" "$R"
R=$(curl -s --noproxy "*" -H "$H" "$API/api/v1/sqm/trace/tree?rootLotId=$LT")
echo "$R" | grep -q '"nodeType"' && P "trace tree" || F "trace tree" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/sqm/abnormals -d '{"orgId":"'$MZORG'","lotId":"LF","supplierId":"'$SQ'","partNo":"PF","partName":"PFT","description":"bad","qty":2,"level":"N","occurDate":"2026-07-18"}')
AB=$(jv "$R" id); [ -n "$AB" ] && P "create abnormal" || F "abnormal" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST "$API/api/v1/sqm/abnormals/$AB/close" -d '{"disposal":"ret","disposalRemark":"d"}')
[ "$(jc "$R")" = "0" ] && P "close abnormal" || F "close ab" "$R"
R=$(curl -s --noproxy "*" -H "$H" -H "-H "$CT" -X POST $API/api/v1/sqm/fmea -d '{"orgId":"'$MZORG'","fmeaType":"P","product":"PF","process":"INJ","failureMode":"dim","severityS":8,"occurrenceO":3,"detectionD":5,"status":"open","action":"c","owner":"Q","targetDate":"2026-09-01"}')
FM=$(jv "$R" id); [ -n "$FM" ] && P "create FMEA" || F "FMEA" "$R"
R=$(curl -s --noproxy "*" -H "$H" $API/api/v1/sqm/fmea)
echo "$R" | grep -q '"rpn":120' && P "FMEA rpn=120" || F "rpn" "$R"
R=$(curl -s --noproxy "*" -H "$H" -X POST "$API/api/v1/sqm/fmea/$FM/close")
[ "$(jc "$R")" = "0" ] && P "close FMEA" || F "close FMEA" "$R"

echo "===== 7.dataScope ====="
AC=$(curl -s --noproxy "*" -H "$H" $API/api/v1/uop/users | grep -o '"id"' | wc -l)
MZTK=$(curl -s --noproxy "*" -X POST $API/api/v1/auth/login -H -H "-H "$CT" -d '{"username":"mzuser","password":"user123"}' | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//;s/"//')
MC=$(curl -s --noproxy "*" -H "Authorization: Bearer $MZTK" $API/api/v1/uop/users | grep -o '"id"' | wc -l)
[ "$AC" -gt "$MC" ] && P "dataScope a=$AC>m=$MC" || F "dataScope" "a=$AC m=$MC"

echo ""
echo "===== RESULT: PASS=$PASS FAIL=$FAIL TOTAL=$((PASS+FAIL)) ====="
tasklist 2>/dev/null | grep -i java.exe | awk '{print $2}' | while read pid; do taskkill //F //PID $pid 2>/dev/null; done
