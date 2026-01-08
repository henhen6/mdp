/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.mddata.sdk.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 六如
 */
public class ClassUtil {

    private static final Map<String, Class<?>> classGenricTypeCache = new HashMap<>(16);

    /**
     * 返回定义类时的泛型参数的类型. <br>
     * 如:定义一个BookManager类<br>
     * <code>{@literal public BookManager extends GenricManager<Book,Address>}{...} </code>
     * <br>
     * 调用getSuperClassGenricType(getClass(),0)将返回Book的Class类型<br>
     * 调用getSuperClassGenricType(getClass(),1)将返回Address的Class类型
     *
     * @param clazz 从哪个类中获取
     * @param index 泛型参数索引,从0开始
     */
    public static Class<?> getSuperClassGenricType(Class<?> clazz, int index) throws IndexOutOfBoundsException {
        String cacheKey = clazz.getName() + index;
        Class<?> cachedClass = classGenricTypeCache.get(cacheKey);
        if (cachedClass != null) {
            return cachedClass;
        }

        // 第一步：处理直接传入参数化类型Class的场景（如PageParams<User>.class）
        Type currentType = clazz.getGenericSuperclass();
        // 如果当前类本身就是参数化类型（直接传入带泛型的Class）
        if (clazz.getTypeParameters().length > 0 || currentType instanceof ParameterizedType) {
            // 先尝试获取当前类的原始类型
            Type genType = clazz.getGenericSuperclass();
            if (!(genType instanceof ParameterizedType)) {
                // 如果父类不是参数化类型，直接返回当前类的原始类型
                cachedClass = clazz;
                classGenricTypeCache.put(cacheKey, cachedClass);
                return cachedClass;
            }
        }

        // 第二步：兼容原有逻辑 - 处理父类泛型
        Type genType = clazz.getGenericSuperclass();

        // 没有泛型参数
        if (!(genType instanceof ParameterizedType)) {
            // 优化：如果没有父类泛型，但当前类是参数化类型，返回当前类本身
            if (clazz.getTypeParameters().length > 0) {
                cachedClass = clazz;
                classGenricTypeCache.put(cacheKey, cachedClass);
                return cachedClass;
            }
            throw new RuntimeException("class " + clazz.getName() + " 没有指定父类泛型，且自身也不是参数化类型");
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

            if (index >= params.length || index < 0) {
                throw new RuntimeException("泛型索引不正确，index:" + index);
            }

            Type cls = params[index];
            // 处理集合类型的泛型（如List<User>）
            if (cls instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) cls;
                Type rawType = parameterizedType.getRawType();
                if (Collection.class.isAssignableFrom((Class<?>) rawType)) {
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    cachedClass = (Class<?>) actualTypeArgument;
                } else {
                    cachedClass = (Class<?>) rawType; // 改为返回原始类型而非参数化类型
                }
            } else if (!(cls instanceof Class)) {
                throw new RuntimeException(params[index] + "不是Class类型");
            } else {
                cachedClass = (Class<?>) cls;
            }

            // 加入缓存并返回
            classGenricTypeCache.put(cacheKey, cachedClass);
            return cachedClass;
        }
    }

    // 新增工具方法：专门获取参数化类型的原始类型（如PageParams<User> -> PageParams）
    public static Class<?> getRawType(Class<?> parameterizedClass) {
        if (parameterizedClass == null) {
            return null;
        }
        // 如果是参数化类型，直接返回其原始类型
        if (parameterizedClass.getTypeParameters().length > 0) {
            return parameterizedClass;
        }
        // 处理通过父类继承的泛型
        Type genType = parameterizedClass.getGenericSuperclass();
        if (genType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genType).getRawType();
        }
        return parameterizedClass;
    }

    public static boolean isArray(Class<?> clazz, int index) {
        Type genType = clazz.getGenericSuperclass();

        // 没有泛型参数
        if (!(genType instanceof ParameterizedType)) {
            throw new RuntimeException("class " + clazz.getName() + " 没有指定父类泛型");
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

            if (index >= params.length || index < 0) {
                throw new RuntimeException("泛型索引不正确，index:" + index);
            }
            Type cls = params[index];
            if (cls instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) cls;
                Type rawType = parameterizedType.getRawType();
                return Collection.class.isAssignableFrom((Class<?>) rawType);
            }
            return false;
        }
    }

}
