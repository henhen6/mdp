package top.mddata.gateway.sop.util;

import java.util.Objects;


public class ClassUtil {

    public static final String PREFIX_JAVA_LANG = "java.lang.";

    /**
     * Check if it is the basic data type
     *
     * @param type0 java class name
     * @return boolean
     */
    public static boolean isPrimitive(String type0) {
        if (Objects.isNull(type0)) {
            return true;
        }
        String type = type0.startsWith(PREFIX_JAVA_LANG) ? type0.substring(type0.lastIndexOf(".") + 1) : type0;
        type = type.toLowerCase();
        switch (type) {
            case "string":
            case "integer":
            case "int":
            case "object":
            case "void":
            case "long":
            case "double":
            case "float":
            case "short":
            case "bigdecimal":
            case "char":
            case "character":
            case "number":
            case "boolean":
            case "byte":
            case "uuid":
            case "biginteger":
            case "java.sql.timestamp":
            case "java.util.date":
            case "java.time.localdatetime":
            case "java.time.localtime":
            case "localtime":
            case "date":
            case "localdatetime":
            case "localdate":
            case "zoneddatetime":
            case "java.time.localdate":
            case "java.time.zoneddatetime":
            case "java.math.bigdecimal":
            case "java.math.biginteger":
            case "java.util.uuid":
            case "java.io.serializable":
                return true;
            default:
                return false;
        }
    }

}
