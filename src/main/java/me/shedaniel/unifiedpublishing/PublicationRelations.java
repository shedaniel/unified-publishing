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

import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PublicationRelations {
    private final Project project;
    public final ListProperty<String> depends;
    public final ListProperty<String> includes;
    public final ListProperty<String> optionals;
    public final ListProperty<String> conflicts;
    
    @Inject
    public PublicationRelations(Project project, ProjectRelations relations, Function<ProjectRelation, Property<String>> extractor) {
        this.project = project;
        
        this.depends = project.getObjects().listProperty(String.class);
        this.depends.set(project.provider(() -> relations.depends.getOrElse(Collections.emptyList())
                .stream()
                .map(relation -> extractor.apply(relation).getOrNull())
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
        this.includes = project.getObjects().listProperty(String.class);
        this.includes.set(project.provider(() -> relations.includes.getOrElse(Collections.emptyList())
                .stream()
                .map(relation -> extractor.apply(relation).getOrNull())
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
        this.optionals = project.getObjects().listProperty(String.class);
        this.optionals.set(project.provider(() -> relations.optionals.getOrElse(Collections.emptyList())
                .stream()
                .map(relation -> extractor.apply(relation).getOrNull())
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
        this.conflicts = project.getObjects().listProperty(String.class);
        this.conflicts.set(project.provider(() -> relations.conflicts.getOrElse(Collections.emptyList())
                .stream()
                .map(relation -> extractor.apply(relation).getOrNull())
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
    }
    
    public void depends(String relationship) {
        this.depends.add(relationship);
    }
    
    public void includes(String relationship) {
        this.includes.add(relationship);
    }
    
    public void optional(String relationship) {
        this.optionals.add(relationship);
    }
    
    public void conflicts(String relationship) {
        this.conflicts.add(relationship);
    }
    
    public ListProperty<String> getDepends() {
        return depends;
    }
    
    public ListProperty<String> getIncludes() {
        return includes;
    }
    
    public ListProperty<String> getOptionals() {
        return optionals;
    }
    
    public ListProperty<String> getConflicts() {
        return conflicts;
    }
}
