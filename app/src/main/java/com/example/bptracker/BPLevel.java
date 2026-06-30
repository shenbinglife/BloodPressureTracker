package com.example.bptracker;

public class BPLevel {
    public static final int NORMAL = 0;
    public static final int ELEVATED = 1;
    public static final int HIGH_STAGE1 = 2;
    public static final int HIGH_STAGE2 = 3;
    public static final int CRISIS = 4;

    public static int getLevel(int systolic, int diastolic) {
        if (systolic > 180 || diastolic > 120) return CRISIS;
        if (systolic >= 140 || diastolic >= 90) return HIGH_STAGE2;
        if (systolic >= 130 || diastolic >= 80) return HIGH_STAGE1;
        if (systolic >= 120 && diastolic < 80) return ELEVATED;
        return NORMAL;
    }

    public static String getLevelName(int level) {
        switch (level) {
            case NORMAL: return "正常";
            case ELEVATED: return "偏高";
            case HIGH_STAGE1: return "高血压 1级";
            case HIGH_STAGE2: return "高血压 2级";
            case CRISIS: return "危险";
            default: return "未知";
        }
    }

    public static int getLevelColor(int level) {
        switch (level) {
            case NORMAL: return 0xFF4CAF50;
            case ELEVATED: return 0xFFFFC107;
            case HIGH_STAGE1: return 0xFFFF9800;
            case HIGH_STAGE2: return 0xFFF44336;
            case CRISIS: return 0xFFB71C1C;
            default: return 0xFF9E9E9E;
        }
    }
}