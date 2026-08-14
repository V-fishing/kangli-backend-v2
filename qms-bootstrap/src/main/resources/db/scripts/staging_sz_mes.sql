-- 临时 staging 建表(仅用于导入 SZ-MES 源数据, V90 脚本从中映射)
-- 列全用 TEXT 以吸收源数据任意格式, 类型转换在 V90 内完成
CREATE SCHEMA IF NOT EXISTS qms;

DROP TABLE IF EXISTS qms.material_inspection;
CREATE TABLE qms.material_inspection (
    process_no TEXT, form_version TEXT, is_customer_supplied TEXT, memo TEXT,
    material_category TEXT, is_valid TEXT, review_status TEXT, signature_status TEXT,
    is_urgent TEXT, data_record_flag TEXT, is_invalid TEXT, report_generated TEXT,
    record_no TEXT, purchase_order TEXT, inbound_no TEXT, inspection_request_no TEXT,
    mes_inspection_no TEXT, inspection_date TEXT, judgement_date TEXT, inspector TEXT,
    inspection_result TEXT, supplier_name TEXT, material_code TEXT, material_name TEXT,
    spec_model TEXT, material_batch_no TEXT, qualified_qty TEXT, unqualified_qty TEXT,
    submitted_qty TEXT, loss_qty TEXT, unit TEXT, defect_desc TEXT, handling_method TEXT,
    unqualified_final_status TEXT, unqualified_review TEXT, unqualified_review_no TEXT,
    inspection_category TEXT, arrival_date TEXT, receiving_no TEXT, po_line_no TEXT,
    receiving_line_no TEXT, shelf_life_days TEXT, reinspect_remark TEXT, judge TEXT,
    inspection_end_date TEXT, reviewer TEXT, review_date TEXT, submitter TEXT,
    submit_date TEXT, supplier_code TEXT, remark TEXT, ext_id TEXT, last_modified_by TEXT,
    signature_user TEXT, signature_time TEXT, signature_reason TEXT, plant_code TEXT,
    plant_name TEXT, created_by TEXT, updated_by TEXT, is_deleted TEXT, "version" TEXT,
    created_at TEXT, updated_at TEXT, material_barcode TEXT
);

DROP TABLE IF EXISTS qms.finished_goods_inspection;
CREATE TABLE qms.finished_goods_inspection (
    process_no TEXT, form_version TEXT, is_customer_supplied TEXT, memo TEXT,
    material_category TEXT, is_valid TEXT, review_status TEXT, signature_status TEXT,
    is_urgent TEXT, data_record_flag TEXT, is_invalid TEXT, report_generated TEXT,
    record_no TEXT, production_order_no TEXT, inspection_request_no TEXT, inspection_date TEXT,
    judgement_date TEXT, inspector TEXT, inspection_result TEXT, customer TEXT,
    material_code TEXT, product_name TEXT, model_spec TEXT, product_batch_or_sn TEXT,
    inspection_category TEXT, inspected_qty TEXT, qualified_qty TEXT, unqualified_qty TEXT,
    unit TEXT, defect_desc TEXT, handling_method TEXT, unqualified_final_status TEXT,
    unqualified_review TEXT, unqualified_review_no TEXT, production_date TEXT, expiry_date TEXT,
    receiving_no TEXT, po_line_no TEXT, receiving_line_no TEXT, shelf_life_days TEXT,
    reinspect_remark TEXT, judge TEXT, inspection_end_date TEXT, reviewer TEXT,
    review_date TEXT, submitter TEXT, submit_date TEXT, supplier_code TEXT, remark TEXT,
    ext_id TEXT, last_modified_by TEXT, signature_user TEXT, signature_time TEXT,
    signature_reason TEXT, plant_code TEXT, plant_name TEXT, qc_review TEXT, mgr_approval TEXT,
    report_no TEXT, inspector_name TEXT, category TEXT, created_by TEXT, updated_by TEXT,
    is_deleted TEXT, "version" TEXT, created_at TEXT, updated_at TEXT
);

DROP TABLE IF EXISTS qms.critical_material_binding;
CREATE TABLE qms.critical_material_binding (
    process_no TEXT, form_version TEXT, is_customer_supplied TEXT, memo TEXT,
    material_category TEXT, is_valid TEXT, review_status TEXT, signature_status TEXT,
    is_urgent TEXT, data_record_flag TEXT, is_invalid TEXT, report_generated TEXT,
    record_no TEXT, work_order_no TEXT, category TEXT, material_code TEXT, material_name TEXT,
    spec_model TEXT, material_barcode TEXT, product_barcode TEXT, scanner TEXT, scan_time TEXT,
    is_active TEXT, process_code TEXT, process_name TEXT, plant_code TEXT, plant_name TEXT,
    is_deleted TEXT, "version" TEXT, created_at TEXT, updated_at TEXT
);
