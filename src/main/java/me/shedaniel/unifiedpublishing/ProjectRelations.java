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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;

public class ProjectRelations {
    private final Project project;
    public final ListProperty<ProjectRelation> depends;
    public final ListProperty<ProjectRelation> includes;
    public final ListProperty<ProjectRelation> optionals;
    public final ListProperty<ProjectRelation> conflicts;
    
    @Inject
    public ProjectRelations(Project project) {
        this.project = project;
        
        this.depends = project.getObjects().listProperty(ProjectRelation.class).empty();
        this.includes = project.getObjects().listProperty(ProjectRelation.class).empty();
        this.optionals = project.getObjects().listProperty(ProjectRelation.class).empty();
        this.conflicts = project.getObjects().listProperty(ProjectRelation.class).empty();
    }
    
    public void depends(Action<ProjectRelation> action) {
        ProjectRelation relation = project.getObjects().newInstance(ProjectRelation.class, project);
        this.depends.add(relation);
        action.execute(relation);
    }
    
    public void includes(Action<ProjectRelation> action) {
        ProjectRelation relation = project.getObjects().newInstance(ProjectRelation.class, project);
        this.includes.add(relation);
        action.execute(relation);
    }
    
    public void optional(Action<ProjectRelation> action) {
        ProjectRelation relation = project.getObjects().newInstance(ProjectRelation.class, project);
        this.optionals.add(relation);
        action.execute(relation);
    }
    
    public void conflicts(Action<ProjectRelation> action) {
        ProjectRelation relation = project.getObjects().newInstance(ProjectRelation.class, project);
        this.conflicts.add(relation);
        action.execute(relation);
    }
    
    public ListProperty<ProjectRelation> getDepends() {
        return depends;
    }
    
    public ListProperty<ProjectRelation> getIncludes() {
        return includes;
    }
    
    public ListProperty<ProjectRelation> getOptionals() {
        return optionals;
    }
    
    public ListProperty<ProjectRelation> getConflicts() {
        return conflicts;
    }
}
