-- ============================================================================
-- 康立 QMS 数据库结构基线 - 外键约束 (CONSTRAINTS)
-- ----------------------------------------------------------------------------
-- 内容 : 全部 ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY
-- 说明 : 放在所有建表文件(V002~V016)之后执行, 避免跨板块 FK 顺序依赖。
-- 生成 : 从各表 DDL 提取 FOREIGN KEY 约束汇总。
-- ============================================================================

ALTER TABLE ONLY ops.qms_capa_action
    ADD CONSTRAINT qms_capa_action_capa_id_fkey FOREIGN KEY (capa_id) REFERENCES ops.qms_capa(id);
ALTER TABLE ONLY ops.qms_capa_action
    ADD CONSTRAINT qms_capa_action_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_capa_trigger_rule
    ADD CONSTRAINT qms_capa_trigger_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_fmea_risk_track
    ADD CONSTRAINT qms_fmea_risk_track_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_fmea_risk_track
    ADD CONSTRAINT qms_fmea_risk_track_risk_id_fkey FOREIGN KEY (risk_id) REFERENCES ops.qms_fmea_risk(id);
ALTER TABLE ONLY ops.qms_fmea_template
    ADD CONSTRAINT qms_fmea_template_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_fmea_risk
    ADD CONSTRAINT qms_fmea_risk_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_8d_report
    ADD CONSTRAINT qms_8d_report_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_8d_stage_history
    ADD CONSTRAINT qms_8d_stage_history_d8_id_fkey FOREIGN KEY (d8_id) REFERENCES ops.qms_8d_report(id);
ALTER TABLE ONLY ops.qms_8d_stage_history
    ADD CONSTRAINT qms_8d_stage_history_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_8d_fishbone
    ADD CONSTRAINT qms_8d_fishbone_d8_id_fkey FOREIGN KEY (d8_id) REFERENCES ops.qms_8d_report(id);
ALTER TABLE ONLY ops.qms_8d_fishbone
    ADD CONSTRAINT qms_8d_fishbone_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_8d_stage_config
    ADD CONSTRAINT qms_8d_stage_config_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_capa
    ADD CONSTRAINT qms_capa_d8_id_fkey FOREIGN KEY (d8_id) REFERENCES ops.qms_8d_report(id);
ALTER TABLE ONLY ops.qms_capa
    ADD CONSTRAINT qms_capa_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_quality_goal
    ADD CONSTRAINT qms_quality_goal_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_internal_audit
    ADD CONSTRAINT qms_internal_audit_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_audit_nc
    ADD CONSTRAINT qms_audit_nc_audit_id_fkey FOREIGN KEY (audit_id) REFERENCES ops.qms_internal_audit(id);
ALTER TABLE ONLY ops.qms_audit_nc
    ADD CONSTRAINT qms_audit_nc_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_8d_stage_detail
    ADD CONSTRAINT qms_8d_stage_detail_d8_id_fkey FOREIGN KEY (d8_id) REFERENCES ops.qms_8d_report(id);
ALTER TABLE ONLY ops.qms_8d_stage_detail
    ADD CONSTRAINT qms_8d_stage_detail_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_8d_archived_report
    ADD CONSTRAINT qms_8d_archived_report_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.qms_8d_archived_report
    ADD CONSTRAINT qms_8d_archived_report_report_id_fkey FOREIGN KEY (report_id) REFERENCES ops.qms_8d_report(id);
ALTER TABLE ONLY ops.qms_adverse_event
    ADD CONSTRAINT qms_adverse_event_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.todo_item
    ADD CONSTRAINT todo_item_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sys_org
    ADD CONSTRAINT sys_org_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sys_user
    ADD CONSTRAINT sys_user_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sys_user_role
    ADD CONSTRAINT sys_user_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES ops.sys_role(id);
ALTER TABLE ONLY ops.sys_user_role
    ADD CONSTRAINT sys_user_role_user_id_fkey FOREIGN KEY (user_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.sys_role
    ADD CONSTRAINT sys_role_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sys_menu
    ADD CONSTRAINT sys_menu_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES ops.sys_menu(id);
ALTER TABLE ONLY ops.sys_button
    ADD CONSTRAINT sys_button_menu_id_fkey FOREIGN KEY (menu_id) REFERENCES ops.sys_menu(id);
ALTER TABLE ONLY ops.sys_role_menu
    ADD CONSTRAINT sys_role_menu_menu_id_fkey FOREIGN KEY (menu_id) REFERENCES ops.sys_menu(id);
ALTER TABLE ONLY ops.sys_role_menu
    ADD CONSTRAINT sys_role_menu_role_id_fkey FOREIGN KEY (role_id) REFERENCES ops.sys_role(id);
ALTER TABLE ONLY ops.sys_role_button
    ADD CONSTRAINT sys_role_button_button_id_fkey FOREIGN KEY (button_id) REFERENCES ops.sys_button(id);
ALTER TABLE ONLY ops.sys_role_button
    ADD CONSTRAINT sys_role_button_role_id_fkey FOREIGN KEY (role_id) REFERENCES ops.sys_role(id);
ALTER TABLE ONLY ops.sys_data_scope
    ADD CONSTRAINT sys_data_scope_menu_id_fkey FOREIGN KEY (menu_id) REFERENCES ops.sys_menu(id);
ALTER TABLE ONLY ops.sys_data_scope
    ADD CONSTRAINT sys_data_scope_role_id_fkey FOREIGN KEY (role_id) REFERENCES ops.sys_role(id);
ALTER TABLE ONLY ops.sys_delegation
    ADD CONSTRAINT sys_delegation_delegatee_id_fkey FOREIGN KEY (delegatee_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.sys_delegation
    ADD CONSTRAINT sys_delegation_delegator_id_fkey FOREIGN KEY (delegator_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.sys_delegation
    ADD CONSTRAINT sys_delegation_role_id_fkey FOREIGN KEY (role_id) REFERENCES ops.sys_role(id);
ALTER TABLE ONLY ops.audit_log
    ADD CONSTRAINT audit_log_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sys_attachment
    ADD CONSTRAINT sys_attachment_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.cs_work_order
    ADD CONSTRAINT cs_work_order_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.cs_feedback
    ADD CONSTRAINT cs_feedback_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_defect_record
    ADD CONSTRAINT ncm_defect_record_defect_dict_code_fkey FOREIGN KEY (defect_dict_code) REFERENCES ops.ncm_defect_dict(code);
ALTER TABLE ONLY ops.ncm_defect_record
    ADD CONSTRAINT ncm_defect_record_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_defect_dict
    ADD CONSTRAINT ncm_defect_dict_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_realtime_snapshot
    ADD CONSTRAINT ncm_realtime_snapshot_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_filter_scheme
    ADD CONSTRAINT ncm_filter_scheme_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_bi_report
    ADD CONSTRAINT ncm_bi_report_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_daily_report_config
    ADD CONSTRAINT ncm_daily_report_config_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_alert_escalation
    ADD CONSTRAINT ncm_alert_escalation_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_trend_alert
    ADD CONSTRAINT ncm_trend_alert_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.ncm_corrective_action
    ADD CONSTRAINT ncm_corrective_action_defect_no_fkey FOREIGN KEY (defect_no) REFERENCES ops.ncm_defect_record(defect_no);
ALTER TABLE ONLY ops.ncm_corrective_action
    ADD CONSTRAINT ncm_corrective_action_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_sample_task
    ADD CONSTRAINT fk_spctask_param FOREIGN KEY (param_id) REFERENCES ops.spc_param(id);
ALTER TABLE ops.spc_subgroup
    ADD CONSTRAINT spc_subgroup_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ops.spc_subgroup
    ADD CONSTRAINT spc_subgroup_param_id_fkey FOREIGN KEY (param_id) REFERENCES ops.spc_param(id);
ALTER TABLE ONLY ops.spc_param
    ADD CONSTRAINT spc_param_fia_std_item_id_fkey FOREIGN KEY (fia_std_item_id) REFERENCES ops.fia_insp_std_item(id);
ALTER TABLE ONLY ops.spc_param
    ADD CONSTRAINT spc_param_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_param
    ADD CONSTRAINT spc_param_process_id_fkey FOREIGN KEY (process_id) REFERENCES ops.spc_process(id);
ALTER TABLE ONLY ops.spc_param
    ADD CONSTRAINT spc_param_spec_standard_id_fkey FOREIGN KEY (spec_standard_id) REFERENCES ops.spc_spec_standard(id);
ALTER TABLE ONLY ops.spc_param
    ADD CONSTRAINT spc_param_src_item_id_fkey FOREIGN KEY (src_item_id) REFERENCES ops.fia_insp_item(id);
ALTER TABLE ONLY ops.spc_param
    ADD CONSTRAINT spc_param_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.spc_control_limit
    ADD CONSTRAINT spc_control_limit_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_control_limit
    ADD CONSTRAINT spc_control_limit_param_id_fkey FOREIGN KEY (param_id) REFERENCES ops.spc_param(id);
ALTER TABLE ONLY ops.spc_measurement
    ADD CONSTRAINT spc_measurement_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_alarm
    ADD CONSTRAINT spc_alarm_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_alarm
    ADD CONSTRAINT spc_alarm_param_id_fkey FOREIGN KEY (param_id) REFERENCES ops.spc_param(id);
ALTER TABLE ONLY ops.spc_import_log
    ADD CONSTRAINT spc_import_log_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_import_log
    ADD CONSTRAINT spc_import_log_param_id_fkey FOREIGN KEY (param_id) REFERENCES ops.spc_param(id);
ALTER TABLE ONLY ops.spc_supplier_capability
    ADD CONSTRAINT spc_supplier_capability_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_rule
    ADD CONSTRAINT spc_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_global_config
    ADD CONSTRAINT spc_global_config_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_notify_channel
    ADD CONSTRAINT spc_notify_channel_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_collect_task
    ADD CONSTRAINT spc_collect_task_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_collect_task
    ADD CONSTRAINT spc_collect_task_param_id_fkey FOREIGN KEY (param_id) REFERENCES ops.spc_param(id);
ALTER TABLE ONLY ops.spc_capability
    ADD CONSTRAINT spc_capability_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_capability
    ADD CONSTRAINT spc_capability_param_id_fkey FOREIGN KEY (param_id) REFERENCES ops.spc_param(id);
ALTER TABLE ONLY ops.spc_notify_record
    ADD CONSTRAINT spc_notify_record_alarm_id_fkey FOREIGN KEY (alarm_id) REFERENCES ops.spc_alarm(id);
ALTER TABLE ONLY ops.spc_notify_record
    ADD CONSTRAINT spc_notify_record_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_process
    ADD CONSTRAINT spc_process_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_param_product
    ADD CONSTRAINT spc_param_product_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.spc_param_product
    ADD CONSTRAINT spc_param_product_param_id_fkey FOREIGN KEY (param_id) REFERENCES ops.spc_param(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.notification_log
    ADD CONSTRAINT notification_log_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_insp_std
    ADD CONSTRAINT fia_insp_std_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_insp_std
    ADD CONSTRAINT fia_insp_std_prev_version_id_fkey FOREIGN KEY (prev_version_id) REFERENCES ops.fia_insp_std(id);
ALTER TABLE ONLY ops.fia_insp_std
    ADD CONSTRAINT fia_insp_std_spc_process_id_fkey FOREIGN KEY (spc_process_id) REFERENCES ops.spc_process(id);
ALTER TABLE ONLY ops.fia_insp_std
    ADD CONSTRAINT fia_insp_std_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.fia_insp_std_item
    ADD CONSTRAINT fia_insp_std_item_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_insp_std_item
    ADD CONSTRAINT fia_insp_std_item_std_id_fkey FOREIGN KEY (std_id) REFERENCES ops.fia_insp_std(id);
ALTER TABLE ONLY ops.fia_trigger_type
    ADD CONSTRAINT fia_trigger_type_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_insp_item
    ADD CONSTRAINT fia_insp_item_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_insp_item
    ADD CONSTRAINT fia_insp_item_std_item_id_fkey FOREIGN KEY (std_item_id) REFERENCES ops.fia_insp_std_item(id);
ALTER TABLE ONLY ops.fia_insp_item
    ADD CONSTRAINT fia_insp_item_task_id_fkey FOREIGN KEY (task_id) REFERENCES ops.fia_task(id);
ALTER TABLE ONLY ops.fia_task
    ADD CONSTRAINT fia_task_lot_id_fkey FOREIGN KEY (lot_id) REFERENCES ops.sqm_incoming_lot(id);
ALTER TABLE ONLY ops.fia_task
    ADD CONSTRAINT fia_task_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_task
    ADD CONSTRAINT fia_task_std_id_fkey FOREIGN KEY (std_id) REFERENCES ops.fia_insp_std(id);
ALTER TABLE ONLY ops.fia_task
    ADD CONSTRAINT fia_task_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.fia_approval
    ADD CONSTRAINT fia_approval_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_approval
    ADD CONSTRAINT fia_approval_task_id_fkey FOREIGN KEY (task_id) REFERENCES ops.fia_task(id);
ALTER TABLE ONLY ops.fia_archived_report
    ADD CONSTRAINT fia_archived_report_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_archived_report
    ADD CONSTRAINT fia_archived_report_task_id_fkey FOREIGN KEY (task_id) REFERENCES ops.fia_task(id);
ALTER TABLE ONLY ops.fia_task_log
    ADD CONSTRAINT fia_task_log_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_task_log
    ADD CONSTRAINT fia_task_log_task_id_fkey FOREIGN KEY (task_id) REFERENCES ops.fia_task(id);
ALTER TABLE ONLY ops.fia_intercept_config
    ADD CONSTRAINT fia_intercept_config_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_sign_config
    ADD CONSTRAINT fia_sign_config_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_wo_lock
    ADD CONSTRAINT fia_wo_lock_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_insp_plan
    ADD CONSTRAINT fia_insp_plan_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.fia_insp_plan
    ADD CONSTRAINT fia_insp_plan_std_id_fkey FOREIGN KEY (std_id) REFERENCES ops.fia_insp_std(id);
ALTER TABLE ONLY ops.fia_insp_plan
    ADD CONSTRAINT fia_insp_plan_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_audit_workflow_log
    ADD CONSTRAINT sqm_audit_workflow_log_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_workflow_log
    ADD CONSTRAINT sqm_audit_workflow_log_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES ops.sqm_audit_plan(id);
ALTER TABLE ONLY ops.sqm_audit_plan
    ADD CONSTRAINT sqm_audit_plan_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_plan
    ADD CONSTRAINT sqm_audit_plan_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_audit_record
    ADD CONSTRAINT sqm_audit_record_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_record
    ADD CONSTRAINT sqm_audit_record_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES ops.sqm_audit_plan(id);
ALTER TABLE ONLY ops.sqm_audit_record
    ADD CONSTRAINT sqm_audit_record_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_audit_checklist_item
    ADD CONSTRAINT sqm_audit_checklist_item_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_checklist_item
    ADD CONSTRAINT sqm_audit_checklist_item_record_id_fkey FOREIGN KEY (record_id) REFERENCES ops.sqm_audit_record(id);
ALTER TABLE ONLY ops.sqm_audit_nc
    ADD CONSTRAINT sqm_audit_nc_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_nc
    ADD CONSTRAINT sqm_audit_nc_record_id_fkey FOREIGN KEY (record_id) REFERENCES ops.sqm_audit_record(id);
ALTER TABLE ONLY ops.sqm_audit_nc
    ADD CONSTRAINT sqm_audit_nc_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_audit_photo
    ADD CONSTRAINT sqm_audit_photo_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_photo
    ADD CONSTRAINT sqm_audit_photo_record_id_fkey FOREIGN KEY (record_id) REFERENCES ops.sqm_audit_record(id);
ALTER TABLE ONLY ops.sqm_audit_report_archive
    ADD CONSTRAINT sqm_audit_report_archive_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_report_archive
    ADD CONSTRAINT sqm_audit_report_archive_record_id_fkey FOREIGN KEY (record_id) REFERENCES ops.sqm_audit_record(id);
ALTER TABLE ONLY ops.sqm_audit_freq_rule
    ADD CONSTRAINT sqm_audit_freq_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_audit_approval
    ADD CONSTRAINT sqm_audit_approval_audit_id_fkey FOREIGN KEY (audit_id) REFERENCES ops.sqm_audit_plan(id);
ALTER TABLE ONLY ops.sqm_audit_approval
    ADD CONSTRAINT sqm_audit_approval_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_perf_metric_cfg
    ADD CONSTRAINT sqm_perf_metric_cfg_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_supplier
    ADD CONSTRAINT sqm_supplier_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_supplier_cert
    ADD CONSTRAINT sqm_supplier_cert_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_supplier_cert
    ADD CONSTRAINT sqm_supplier_cert_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_supplier_performance
    ADD CONSTRAINT sqm_supplier_performance_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_supplier_performance
    ADD CONSTRAINT sqm_supplier_performance_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_supplier_escalation
    ADD CONSTRAINT sqm_supplier_escalation_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_supplier_escalation
    ADD CONSTRAINT sqm_supplier_escalation_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_supplier_share
    ADD CONSTRAINT sqm_supplier_share_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_supplier_share
    ADD CONSTRAINT sqm_supplier_share_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_key_part_sn
    ADD CONSTRAINT sqm_key_part_sn_lot_id_fkey FOREIGN KEY (lot_id) REFERENCES ops.sqm_incoming_lot(id);
ALTER TABLE ONLY ops.sqm_key_part_sn
    ADD CONSTRAINT sqm_key_part_sn_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_key_part_sn
    ADD CONSTRAINT sqm_key_part_sn_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_incoming_lot
    ADD CONSTRAINT sqm_incoming_lot_change_id_fkey FOREIGN KEY (change_id) REFERENCES ops.sqm_change_order(id);
ALTER TABLE ONLY ops.sqm_incoming_lot
    ADD CONSTRAINT sqm_incoming_lot_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_incoming_lot
    ADD CONSTRAINT sqm_incoming_lot_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_incoming_abnormal
    ADD CONSTRAINT sqm_incoming_abnormal_capa_id_fkey FOREIGN KEY (capa_id) REFERENCES ops.qms_capa(id);
ALTER TABLE ONLY ops.sqm_incoming_abnormal
    ADD CONSTRAINT sqm_incoming_abnormal_d8_id_fkey FOREIGN KEY (d8_id) REFERENCES ops.qms_8d_report(id);
ALTER TABLE ONLY ops.sqm_incoming_abnormal
    ADD CONSTRAINT sqm_incoming_abnormal_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_incoming_abnormal
    ADD CONSTRAINT sqm_incoming_abnormal_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_supplier_measure
    ADD CONSTRAINT sqm_supplier_measure_abnormal_id_fkey FOREIGN KEY (abnormal_id) REFERENCES ops.sqm_incoming_abnormal(id);
ALTER TABLE ONLY ops.sqm_supplier_measure
    ADD CONSTRAINT sqm_supplier_measure_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_supplier_measure
    ADD CONSTRAINT sqm_supplier_measure_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_supplier_grade_rule
    ADD CONSTRAINT sqm_supplier_grade_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_material
    ADD CONSTRAINT sqm_material_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_customer
    ADD CONSTRAINT sqm_customer_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_repair
    ADD CONSTRAINT tlm_repair_approver_id_fkey FOREIGN KEY (approver_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.tlm_repair
    ADD CONSTRAINT tlm_repair_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_repair
    ADD CONSTRAINT tlm_repair_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.tlm_scrap
    ADD CONSTRAINT tlm_scrap_approver_id_fkey FOREIGN KEY (approver_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.tlm_scrap
    ADD CONSTRAINT tlm_scrap_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_scrap
    ADD CONSTRAINT tlm_scrap_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.tlm_maint_plan
    ADD CONSTRAINT tlm_maint_plan_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_maint_plan
    ADD CONSTRAINT tlm_maint_plan_responsible_id_fkey FOREIGN KEY (responsible_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.tlm_maint_plan
    ADD CONSTRAINT tlm_maint_plan_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.tlm_maint_record
    ADD CONSTRAINT tlm_maint_record_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_maint_record
    ADD CONSTRAINT tlm_maint_record_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES ops.tlm_maint_plan(id) ON DELETE SET NULL;
ALTER TABLE ONLY ops.tlm_maint_record
    ADD CONSTRAINT tlm_maint_record_responsible_id_fkey FOREIGN KEY (responsible_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.tlm_maint_record
    ADD CONSTRAINT tlm_maint_record_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.tlm_tool_product
    ADD CONSTRAINT tlm_tool_product_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_tool_product
    ADD CONSTRAINT tlm_tool_product_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.tlm_tool_wo_bind
    ADD CONSTRAINT tlm_tool_wo_bind_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_tool_wo_bind
    ADD CONSTRAINT tlm_tool_wo_bind_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.tlm_tool_version
    ADD CONSTRAINT tlm_tool_version_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_tool_version
    ADD CONSTRAINT tlm_tool_version_tool_id_fkey FOREIGN KEY (tool_id) REFERENCES ops.tlm_tooling(id);
ALTER TABLE ONLY ops.tlm_tooling
    ADD CONSTRAINT tlm_tooling_admin_id_fkey FOREIGN KEY (admin_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.tlm_tooling
    ADD CONSTRAINT tlm_tooling_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.tlm_tooling
    ADD CONSTRAINT tlm_tooling_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES ops.sys_user(id);
ALTER TABLE ONLY ops.tlm_tooling
    ADD CONSTRAINT tlm_tooling_process_id_fkey FOREIGN KEY (process_id) REFERENCES ops.spc_process(id);
ALTER TABLE ONLY ops.tlm_tooling
    ADD CONSTRAINT tlm_tooling_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.tlm_scrap_archive
    ADD CONSTRAINT tlm_scrap_archive_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_strict_inspect
    ADD CONSTRAINT sqm_change_strict_inspect_change_id_fkey FOREIGN KEY (change_id) REFERENCES ops.sqm_change_order(id);
ALTER TABLE ONLY ops.sqm_change_strict_inspect
    ADD CONSTRAINT sqm_change_strict_inspect_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_doc
    ADD CONSTRAINT sqm_change_doc_change_id_fkey FOREIGN KEY (change_id) REFERENCES ops.sqm_change_order(id);
ALTER TABLE ONLY ops.sqm_change_doc
    ADD CONSTRAINT sqm_change_doc_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_approval
    ADD CONSTRAINT sqm_change_approval_change_id_fkey FOREIGN KEY (change_id) REFERENCES ops.sqm_change_order(id);
ALTER TABLE ONLY ops.sqm_change_approval
    ADD CONSTRAINT sqm_change_approval_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_workflow_log
    ADD CONSTRAINT sqm_change_workflow_log_change_id_fkey FOREIGN KEY (change_id) REFERENCES ops.sqm_change_order(id);
ALTER TABLE ONLY ops.sqm_change_workflow_log
    ADD CONSTRAINT sqm_change_workflow_log_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_impact
    ADD CONSTRAINT sqm_change_impact_change_id_fkey FOREIGN KEY (change_id) REFERENCES ops.sqm_change_order(id);
ALTER TABLE ONLY ops.sqm_change_impact
    ADD CONSTRAINT sqm_change_impact_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_order
    ADD CONSTRAINT sqm_change_order_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_order
    ADD CONSTRAINT sqm_change_order_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_change_sop_notice
    ADD CONSTRAINT sqm_change_sop_notice_change_id_fkey FOREIGN KEY (change_id) REFERENCES ops.sqm_change_order(id);
ALTER TABLE ONLY ops.sqm_change_sop_notice
    ADD CONSTRAINT sqm_change_sop_notice_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_change_risk_rule
    ADD CONSTRAINT sqm_change_risk_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_trace_node
    ADD CONSTRAINT sqm_trace_node_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_trace_node
    ADD CONSTRAINT sqm_trace_node_parent_node_id_fkey FOREIGN KEY (parent_node_id) REFERENCES ops.sqm_trace_node(id);
ALTER TABLE ONLY ops.sqm_trace_node
    ADD CONSTRAINT sqm_trace_node_root_node_id_fkey FOREIGN KEY (root_node_id) REFERENCES ops.sqm_trace_node(id);
ALTER TABLE ONLY ops.sqm_trace_node
    ADD CONSTRAINT sqm_trace_node_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_trace_raw_detail
    ADD CONSTRAINT sqm_trace_raw_detail_node_id_fkey FOREIGN KEY (node_id) REFERENCES ops.sqm_trace_node(id);
ALTER TABLE ONLY ops.sqm_trace_raw_detail
    ADD CONSTRAINT sqm_trace_raw_detail_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_trace_product_detail
    ADD CONSTRAINT sqm_trace_product_detail_node_id_fkey FOREIGN KEY (node_id) REFERENCES ops.sqm_trace_node(id);
ALTER TABLE ONLY ops.sqm_trace_product_detail
    ADD CONSTRAINT sqm_trace_product_detail_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_trace_customer_detail
    ADD CONSTRAINT sqm_trace_customer_detail_node_id_fkey FOREIGN KEY (node_id) REFERENCES ops.sqm_trace_node(id);
ALTER TABLE ONLY ops.sqm_trace_customer_detail
    ADD CONSTRAINT sqm_trace_customer_detail_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_rectify_notice
    ADD CONSTRAINT sqm_rectify_notice_abnormal_id_fkey FOREIGN KEY (abnormal_id) REFERENCES ops.sqm_incoming_abnormal(id);
ALTER TABLE ONLY ops.sqm_rectify_notice
    ADD CONSTRAINT sqm_rectify_notice_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_rectify_notice
    ADD CONSTRAINT sqm_rectify_notice_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES ops.sqm_supplier(id);
ALTER TABLE ONLY ops.sqm_sqe_verification
    ADD CONSTRAINT sqm_sqe_verification_abnormal_id_fkey FOREIGN KEY (abnormal_id) REFERENCES ops.sqm_incoming_abnormal(id);
ALTER TABLE ONLY ops.sqm_sqe_verification
    ADD CONSTRAINT sqm_sqe_verification_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_rectify_batch_verify
    ADD CONSTRAINT sqm_rectify_batch_verify_abnormal_id_fkey FOREIGN KEY (abnormal_id) REFERENCES ops.sqm_incoming_abnormal(id);
ALTER TABLE ONLY ops.sqm_rectify_batch_verify
    ADD CONSTRAINT sqm_rectify_batch_verify_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_abnormal_trigger_rule
    ADD CONSTRAINT sqm_abnormal_trigger_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_rectify_sla_rule
    ADD CONSTRAINT sqm_rectify_sla_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_repeat_problem_rule
    ADD CONSTRAINT sqm_repeat_problem_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_escalation_action_rule
    ADD CONSTRAINT sqm_escalation_action_rule_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_abnormal_measure
    ADD CONSTRAINT sqm_abnormal_measure_abnormal_id_fkey FOREIGN KEY (abnormal_id) REFERENCES ops.sqm_incoming_abnormal(id);
ALTER TABLE ONLY ops.sqm_abnormal_measure
    ADD CONSTRAINT sqm_abnormal_measure_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.sqm_abnormal_batch_verify
    ADD CONSTRAINT sqm_abnormal_batch_verify_abnormal_id_fkey FOREIGN KEY (abnormal_id) REFERENCES ops.sqm_incoming_abnormal(id);
ALTER TABLE ONLY ops.sqm_abnormal_batch_verify
    ADD CONSTRAINT sqm_abnormal_batch_verify_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_record
    ADD CONSTRAINT patl_record_checkpoint_id_fkey FOREIGN KEY (checkpoint_id) REFERENCES ops.patl_checkpoint(id);
ALTER TABLE ONLY ops.patl_record
    ADD CONSTRAINT patl_record_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_record
    ADD CONSTRAINT patl_record_task_id_fkey FOREIGN KEY (task_id) REFERENCES ops.patl_task(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.patl_route
    ADD CONSTRAINT patl_route_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_checkpoint
    ADD CONSTRAINT patl_checkpoint_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_checkpoint
    ADD CONSTRAINT patl_checkpoint_route_id_fkey FOREIGN KEY (route_id) REFERENCES ops.patl_route(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.patl_check_item
    ADD CONSTRAINT patl_check_item_checkpoint_id_fkey FOREIGN KEY (checkpoint_id) REFERENCES ops.patl_checkpoint(id) ON DELETE CASCADE;
ALTER TABLE ONLY ops.patl_check_item
    ADD CONSTRAINT patl_check_item_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_task
    ADD CONSTRAINT patl_task_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_task
    ADD CONSTRAINT patl_task_route_id_fkey FOREIGN KEY (route_id) REFERENCES ops.patl_route(id);
ALTER TABLE ONLY ops.patl_abnormal
    ADD CONSTRAINT patl_abnormal_d8_id_fkey FOREIGN KEY (d8_id) REFERENCES ops.qms_8d_report(id);
ALTER TABLE ONLY ops.patl_abnormal
    ADD CONSTRAINT patl_abnormal_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_abnormal
    ADD CONSTRAINT patl_abnormal_record_id_fkey FOREIGN KEY (record_id) REFERENCES ops.patl_record(id);
ALTER TABLE ONLY ops.patl_abnormal
    ADD CONSTRAINT patl_abnormal_task_id_fkey FOREIGN KEY (task_id) REFERENCES ops.patl_task(id);
ALTER TABLE ONLY ops.patl_archived_report
    ADD CONSTRAINT patl_archived_report_org_id_fkey FOREIGN KEY (org_id) REFERENCES ops.sys_org(id);
ALTER TABLE ONLY ops.patl_archived_report
    ADD CONSTRAINT patl_archived_report_task_id_fkey FOREIGN KEY (task_id) REFERENCES ops.patl_task(id);
