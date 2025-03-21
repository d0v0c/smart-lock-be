DROP DATABASE IF EXISTS smart_door_lock;
CREATE DATABASE IF NOT EXISTS smart_door_lock;
USE smart_door_lock;

CREATE TABLE user
(
    username     VARCHAR(50)                        NOT NULL COMMENT '用户名',
    password     VARCHAR(60)                        NOT NULL COMMENT '登录密码',
    email        VARCHAR(100)                       NULL COMMENT '邮箱',
    phone        VARCHAR(20)                        NULL COMMENT '电话',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (username)
)
    COMMENT '后台用户表';

DROP TABLE IF EXISTS device;
CREATE TABLE device
(
    device_id    BIGINT AUTO_INCREMENT              NOT NULL COMMENT '设备ID',
    device_name  VARCHAR(255)                       NULL COMMENT '设备名',
    is_locked    BOOLEAN  DEFAULT FALSE             NULL COMMENT '门锁状态',
    is_connected BOOLEAN  DEFAULT FALSE             NOT NULL COMMENT '设备连接状态',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '设备创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL on update CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (device_id)
)
    COMMENT 'ESP32设备表';

DROP TABLE IF EXISTS user_device_merge;
CREATE TABLE user_device_merge
(
    username  VARCHAR(50) NOT NULL COMMENT '用户名',
    device_id BIGINT      NOT NULL COMMENT '设备号',
    FOREIGN KEY (username) REFERENCES user (username),
    FOREIGN KEY (device_id) REFERENCES device (device_id)
)
    comment '用户与门禁的多对多关系';

CREATE TABLE access_code
(
    code_id      BIGINT                             NOT NULL AUTO_INCREMENT COMMENT '密码ID',
    code         VARCHAR(10)                        NOT NULL COMMENT '密码',
    device_id    BIGINT                             NOT NULL COMMENT '关联设备ID',
    owner        VARCHAR(50)                        NOT NULL COMMENT '所有者ID',
    valid_from   DATETIME                           NOT NULL COMMENT '有效期起始时间',
    valid_to     DATETIME                           NULL COMMENT '有效期结束时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (code_id),
    FOREIGN KEY (device_id) REFERENCES device (device_id),
    FOREIGN KEY (owner) REFERENCES user (username)
)
    COMMENT '门锁密码表';

DROP TABLE IF EXISTS alert;
CREATE TABLE alert
(
    alert_id     BIGINT                             NOT NULL AUTO_INCREMENT COMMENT '警报ID',
    username     VARCHAR(50)                        NULL COMMENT '通知的用户',
    device_id    BIGINT                             NULL COMMENT '报警的设备',
    type         VARCHAR(50)                        NOT NULL COMMENT '警报类型',
    message      TEXT                               NULL COMMENT '警报内容',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (alert_id)
)
    COMMENT '警报记录表';

DROP TABLE IF EXISTS log;
CREATE TABLE log
(
    log_id             BIGINT                             NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    device_id          BIGINT                             NULL COMMENT '关联设备ID',
    user_id            VARCHAR(50)                        NULL COMMENT '关联用户ID',
    action_type        VARCHAR(50)                        NOT NULL COMMENT '操作类型',
    action_description TEXT                               NULL COMMENT '操作描述内容',
    created_time       DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (log_id),
    FOREIGN KEY (device_id) REFERENCES device (device_id),
    FOREIGN KEY (user_id) REFERENCES user (username)
)
    COMMENT '日志';
