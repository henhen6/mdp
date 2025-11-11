/*
 * Copyright (c) 2024.  flash-sloth (244387066@qq.com).
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.mddata.codegen.config.front;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 代码生成器 搜索配置
 *
 * @author henhen6
 * @since 2021-08-01 16:04
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SearchConfig {
    /** 是否生成该字段 */
    private Boolean show;
    /** 是否隐藏字段 */
    private Boolean hidden;
    /** 对齐方式 */
    private String align;
    /** label 超出隐藏 */
    private String titleOverflow;
    /**
     * 组件类型
     */
    private String componentType;

    /**
     * 顺序 升序
     */
    private int sequence;

    public String getComponentType() {
        if (componentType == null || "".equals(componentType)) {
            return "Input";
        }
        return componentType;
    }
}
