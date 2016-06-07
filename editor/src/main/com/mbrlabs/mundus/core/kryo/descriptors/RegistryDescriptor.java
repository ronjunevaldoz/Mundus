/*
 * Copyright (c) 2015. See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mbrlabs.mundus.core.kryo.descriptors;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcus Brummer
 * @version 11-12-2015
 */
public class RegistryDescriptor {

    @Tag(0)
    public SettingsDescriptor settingsDescriptor;
    @Tag(1)
    public List<ProjectRef> projects;
    @Tag(2)
    public ProjectRef lastProject = null;

    public RegistryDescriptor() {
        projects = new ArrayList<>();
        settingsDescriptor = new SettingsDescriptor();
    }

    /**
     * Settings class
     */
    public static class SettingsDescriptor {
        @Tag(0)
        public String fbxConvBinary = "";

        @Tag(1)
        public KeyboardLayout keyboardLayout;
    }

    /**
     * Necessary, because of stupid GLFW keyocde mapping.
     */
    public enum KeyboardLayout {
        /** German layout. */
        QWERTZ,
        /** English layout*/
        QWERTY
    }


    /**
     *
     */
    public static class ProjectRef {
        @Tag(0)
        private String name;
        @Tag(1)
        private String path;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }

}