/*
 * Copyright (C) 2022 shedaniel
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * , MA  02110-1301, USA.
 */

package me.shedaniel.unifiedpublishing;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class UnifiedPublishingPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        Task baseTask = project.getTasks().maybeCreate("publishUnified");
        baseTask.setDescription("Uploads all projects");
        baseTask.setGroup("upload");
        baseTask.getOutputs().upToDateWhen(task -> false);
        project.afterEvaluate(p -> {
            UnifiedPublishingExtension extension = p.getExtensions().getByType(UnifiedPublishingExtension.class);
            extension.onConfigure(p, baseTask);
        });
        if (project.getPluginManager().hasPlugin("com.matthewprenger.cursegradle")) {
            throw new IllegalStateException("CurseGradle is already applied! Please remove it!");
        }
        project.apply(ImmutableMap.of("plugin", "com.matthewprenger.cursegradle"));
        project.getExtensions().create("unifiedPublishing", UnifiedPublishingExtension.class, project);
    }
}
