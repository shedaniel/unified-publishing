/*
 * Copyright (C) 2022 shedaniel
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package me.shedaniel.unifiedpublishing;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class UnifiedPublishingPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.afterEvaluate(p -> {
            UnifiedPublishingExtension extension = p.getExtensions().getByType(UnifiedPublishingExtension.class);
            extension.onConfigure(p);
        });
        project.apply(ImmutableMap.of("plugin", "com.modrinth.minotaur"));
        project.apply(ImmutableMap.of("plugin", "com.matthewprenger.cursegradle"));
        project.getExtensions().create("unifiedPublishing", UnifiedPublishingExtension.class, project);
    }
}
