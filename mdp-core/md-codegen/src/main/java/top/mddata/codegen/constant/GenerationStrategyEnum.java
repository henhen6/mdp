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

package top.mddata.codegen.constant;

/**
 * 生成策略
 *
 * @author henhen6
 * @since 2024年06月15日00:03:49
 */
public enum GenerationStrategyEnum {

    /** 覆盖已经存在的文件 */
    OVERWRITE,
    /** 忽略文件 */
    IGNORE,
    /** 若存在文件，备份已经存在的文件 */
    BACKUPS,
    /** 若存在文件，则新生成一个 .add.java 结尾的文件 */
    ADD,
    /** 若存在文件，则不生成文件；不存在时生成文件 */
    EXIST_IGNORE;
}
