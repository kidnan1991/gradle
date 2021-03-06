/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.language.nativeplatform.internal.incremental.sourceparser;

import org.gradle.language.nativeplatform.internal.Include;
import org.gradle.language.nativeplatform.internal.IncludeType;

public class DefaultInclude implements Include {
    private final String value;
    private final boolean isImport;
    private final IncludeType type;

    public DefaultInclude(String value, boolean isImport, IncludeType type) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.value = value;
        this.isImport = isImport;
        this.type = type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isImport() {
        return isImport;
    }

    @Override
    public IncludeType getType() {
        return type;
    }

    public static Include parse(String value, boolean isImport) {
        if (value.startsWith("<") && value.endsWith(">")) {
            return new DefaultInclude(strip(value), isImport, IncludeType.SYSTEM);
        } else if (value.startsWith("\"") && value.endsWith("\"")) {
            return new DefaultInclude(strip(value), isImport, IncludeType.QUOTED);
        } else {
            return new DefaultInclude(value, isImport, IncludeType.MACRO);
        }
    }

    private static String strip(String include) {
        return include.substring(1, include.length() - 1);
    }

    @Override
    public String toString() {
        return value.concat(":").concat(type.toString()).concat(":").concat(String.valueOf(isImport));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultInclude that = (DefaultInclude) o;

        if (isImport != that.isImport) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (isImport ? 1 : 0);
        result = 31 * result + type.hashCode();
        return result;
    }
}
