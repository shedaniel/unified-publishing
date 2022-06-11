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

import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class UnifiedPublishingExtension {
    private final List<UnifiedPublishingProject> projects = new ArrayList<>();
    private final Project project;
    
    @Inject
    public UnifiedPublishingExtension(Project project) {
        this.project = project;
    }
    
    public void project(Action<UnifiedPublishingProject> action) {
        UnifiedPublishingProject project = this.project.getObjects().newInstance(UnifiedPublishingProject.class, this.project);
        this.projects.add(project);
        action.execute(project);
    }
    
    public void onConfigure(Project project) {
        for (UnifiedPublishingProject publishingProject : this.projects) {
            publishingProject.onConfigure(project);
        }
    }
}
