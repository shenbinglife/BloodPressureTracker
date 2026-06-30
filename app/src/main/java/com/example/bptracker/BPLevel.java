package com.example.bptracker;

/**
 * 血压分级 - 依据《中国高血压防治指南(2024年修订版)》
 * 当收缩压和舒张压分属于不同级别时，以较高的分级为准。
 */
public class BPLevel {
    public static final int NORMAL = 0;       // 正常血压
    public static final int HIGH_NORMAL = 1;  // 正常高值
    public static final int STAGE1 = 2;       // 1级高血压(轻度)
    public static final int STAGE2 = 3;       // 2级高血压(中度)
    public static final int STAGE3 = 4;       // 3级高血压(重度)

    /**
     * 根据 2024 中国高血压防治指南判定血压等级
     */
    public static int getLevel(int systolic, int diastolic) {
        // 3级: SBP≥180 或 DBP≥110
        if (systolic >= 180 || diastolic >= 110) return STAGE3;
        // 2级: SBP 160-179 或 DBP 100-109
        if (systolic >= 160 || diastolic >= 100) return STAGE2;
        // 1级: SBP 140-159 或 DBP 90-99
        if (systolic >= 140 || diastolic >= 90) return STAGE1;
        // 正常高值: SBP 120-139 或 DBP 80-89
        if (systolic >= 120 || diastolic >= 80) return HIGH_NORMAL;
        // 正常血压: SBP<120 且 DBP<80
        return NORMAL;
    }

    public static String getLevelName(int level) {
        switch (level) {
            case NORMAL:      return "正常血压";
            case HIGH_NORMAL: return "正常高值";
            case STAGE1:      return "1级高血压";
            case STAGE2:      return "2级高血压";
            case STAGE3:      return "3级高血压";
            default:          return "未知";
        }
    }

    public static int getLevelColor(int level) {
        switch (level) {
            case NORMAL:      return 0xFF4CAF50; // 绿色
            case HIGH_NORMAL: return 0xFFFFC107; // 黄色
            case STAGE1:      return 0xFFFF9800; // 橙色
            case STAGE2:      return 0xFFF44336; // 红色
            case STAGE3:      return 0xFFB71C1C; // 深红色
            default:          return 0xFF9E9E9E;
        }
    }
}