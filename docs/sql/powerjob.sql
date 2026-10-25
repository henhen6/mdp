INSERT INTO app_info (app_name, creator, current_server, extra, gmt_create, gmt_modified, modifier, namespace_id,
                      password, tags, title)
VALUES ('MDP', 1, '192.168.0.100:10010', NULL, '2026-04-21 09:35:50.359000', '2026-10-25 20:44:02.285000', 1, 1,
        'sys_encrypt_aes:gk8K/4v6ZzGWKB4z954VBNmYPuzPinwCYYFvenNPVlVxxg==', NULL, '主数据平台');


INSERT INTO job_info (advanced_runtime_config, alarm_config, app_id, concurrency, designated_workers, dispatch_strategy,
                      dispatch_strategy_config, execute_type, extra, gmt_create, gmt_modified, instance_retry_num,
                      instance_time_limit, job_description, job_name, job_params, lifecycle, log_config,
                      max_instance_num, max_worker_count, min_cpu_cores, min_disk_space, min_memory_space,
                      next_trigger_time, notify_user_ids, processor_info, processor_type, status, tag, task_retry_num,
                      time_expression, time_expression_type)
VALUES ('{}', '{"alertThreshold":3,"silenceWindowLen":0,"statisticWindowLen":0}', 1, 5, '', 1, NULL, 1, NULL,
        '2026-04-21 09:44:32.979000', '2026-10-25 20:48:46.161000', 0, 0, NULL, '第一个测试任务', NULL, '{}',
        '{"type":1}', 0, 0, 0.0, 0.0, 0.0, 1792932602000, '1', 'top.mddata.worker.processors.LogTestProcessor', 1, 1,
        NULL, 1, '2 * * * * ?', 2),
       ('{}', '{"alertThreshold":3,"silenceWindowLen":0,"statisticWindowLen":0}', 1, 5, '', 1, NULL, 1, NULL,
        '2026-10-25 20:47:29.011000', '2026-10-25 20:47:30.997000', 0, 0, NULL, '事件推送重试扫描', NULL, '{}',
        '{"type":1}', 0, 0, 0.0, 0.0, 0.0, 1792932960000, '1', 'top.mddata.worker.processors.EventPushRetryProcessor',
        1, 1, NULL, 1, '0 0/8 * * * ?', 2),
       ('{}', '{"alertThreshold":0,"silenceWindowLen":0,"statisticWindowLen":0}', 1, 5, '', 1, NULL, 1, NULL,
        '2026-10-25 20:48:08.924000', '2026-10-25 20:48:08.924000', 0, 0, NULL, '接口回调重试扫描', NULL, '{}',
        '{"type":1}', 0, 0, 0.0, 0.0, 0.0, 1792932600000, '1', 'top.mddata.worker.processors.NotifyInfoRetryProcessor',
        1, 1, NULL, 1, '0 0/10 * * * ?', 2);

