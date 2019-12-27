package org.winplus.serial

/**
 * Created by jess on 19-1-11.
 */

val FP_FRAME_HEAD = intArrayOf(0xf1, 0x1f,0xe2, 0x2e,0xb6,0x6b,0xa8,0x8a)
val FP_CHECK_PASSWORD = intArrayOf(0x00,0x00,0x00,0x00)

/**
 * 指令(命令, 命令发送的数据长度)
 */
val FP_ENROLL_CMD = intArrayOf(0x01, 0x11, 1)     //录入指纹
val FP_QUERY_ENROLL_CMD = intArrayOf(0x01, 0x12, 0) // 查询录入指纹结果

val FP_SAVE_FP_CMD = intArrayOf(0x01, 0x13, 2) // 保存指纹
val FP_QUERY_SAVE_FP_CMD = intArrayOf(0x01, 0x14, 0) // 查询保存指纹结果

val FP_CLEAR_FP_CMD = intArrayOf(0x01, 0x31, 3) // 清除指纹
val FP_QUERY_CLEAR_FP_CMD = intArrayOf(0x01, 0x32, 0) // 清除指纹

val FP_MATCH_FP_CMD = intArrayOf(0x01, 0x21, 0) // 匹配指纹
val FP_QUERY_MATCH_FP_CMD = intArrayOf(0x01, 0x22, 0) // 查询匹配指纹

val FP_QUERY_FINGER_ON_CMD = intArrayOf(0x01, 0x35, 0) // 查询手指在位状态

val FP_SLEEP_CMD = intArrayOf(0x02, 0x0C, 1)  // 睡眠模式

val FP_HEART_RATE_CMD = intArrayOf(0x03, 0x3, 0) // 获取心率

val FP_EXIST_CMD = intArrayOf(0x01, 0x33, 2)

/**
 * 响应码
 */
val FP_SUCCESS_CODE = intArrayOf(0x00,0x00,0x00,0x00)

val FP_ENROOL_NOT_DATA_CODE = intArrayOf(0x00,0x00,0x00,0x08) // 没有检测手指按压
val FP_ENROOL_FULL_CODE = intArrayOf(0x00,0x00,0x00,0x0B) // 存储空间已满
val FP_ENROOL_NOT_CLEAR_CODE = intArrayOf(0x00,0x00,0x00,0x0E) // 采集指纹不清晰
