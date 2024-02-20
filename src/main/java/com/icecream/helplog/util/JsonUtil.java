package com.icecream.helplog.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author andre.lan
 */
public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    private static String dateTimeFormatPattern = "yyyy-MM-dd HH:mm:ss";

    private static String dateFormatPattern = "yyyy-MM-dd";

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormatPattern)));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(dateTimeFormatPattern)));
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(dateFormatPattern)));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(dateFormatPattern)));
        mapper.registerModule(module);
    }

    public static String toJson(Object o) {
        if (o == null) {
            return null;
        }

        String s = null;

        try {
            s = mapper.writeValueAsString(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }


    public static <T> T fromJson(String json, Class<T> c) {
        if (StringUtils.hasLength(json) == false) {
            return null;
        }

        T t = null;
        try {
            t = mapper.readValue(json, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, TypeReference<T> tr) {
        if (StringUtils.hasLength(json) == false) {
            return null;
        }
        T t = null;
        try {
            t = (T) mapper.readValue(json, tr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) t;
    }

    public static class TypeReference<T> extends com.fasterxml.jackson.core.type.TypeReference<T>
    {
        protected final Type type;

        // 带参数的方法，支持泛型类传递
        protected <E> TypeReference(Class<E> cls) {
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            ParameterizedTypeImpl clsInfo = ParameterizedTypeImpl.make(((ParameterizedTypeImpl) type).getRawType(), new Type[]{cls}, null);
            this.type = clsInfo;
        }

        protected TypeReference()
        {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof Class<?>) { // sanity check, should never happen
                throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
            }
            this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }

        @Override
        public Type getType() { return type; }
    }
}
