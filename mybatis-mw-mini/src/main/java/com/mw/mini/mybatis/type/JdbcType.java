package com.mw.mini.mybatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangyu
 * @description JDBC类型枚举
 */
public enum JdbcType {

    INTEGER(Types.INTEGER),

    BIGINT(Types.BIGINT),

    FLOAT(Types.FLOAT),

    DOUBLE(Types.DOUBLE),

    DECIMAL(Types.DECIMAL),

    VARCHAR(Types.VARCHAR),

    CHAR(Types.CHAR),

    TIMESTAMP(Types.TIMESTAMP);

    public final int TYPE_CODE;

    private static Map<Integer, JdbcType> codeLookup = new HashMap<>();

    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.TYPE_CODE, type);
        }
    }

    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    public static JdbcType forCode(int code) {
        return codeLookup.get(code);
    }


}
