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
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.util.function.Function;

public class BasePublishingTarget {
    public final Property<String> displayName;
    public final Property<String> version;
    public final Property<String> changelog;
    public final Property<String> releaseType;
    public final ListProperty<String> gameVersions;
    public final ListProperty<String> gameLoaders;
    
    public final PublicationRelations relations;
    
    public final RegularFileProperty mainPublication;
    public final ListProperty<Task> mainPublicationDependencies;
    
    public BasePublishingTarget(Project project, UnifiedPublishingProject extension, Function<ProjectRelation, Property<String>> extractor) {
        this.displayName = project.getObjects().property(String.class)
                .convention(extension.displayName);
        this.version = project.getObjects().property(String.class)
                .convention(extension.version);
        this.changelog = project.getObjects().property(String.class)
                .convention(extension.changelog);
        this.releaseType = project.getObjects().property(String.class)
                .convention(extension.releaseType);
        this.gameVersions = project.getObjects().listProperty(String.class)
                .convention(extension.gameVersions);
        this.gameLoaders = project.getObjects().listProperty(String.class)
                .convention(extension.gameLoaders);
        
        this.relations = project.getObjects().newInstance(PublicationRelations.class, project, extension.relations, extractor);
        
        this.mainPublication = project.getObjects().fileProperty()
                .convention(extension.mainPublication);
        this.mainPublicationDependencies = project.getObjects().listProperty(Task.class)
                .convention(extension.mainPublicationDependencies);
    }
    
    public void mainPublication(Task task) {
        if (task instanceof AbstractArchiveTask) {
            this.mainPublication.set(task.getProject().provider(() -> ((AbstractArchiveTask) task).getArchiveFile().get()));
        } else {
            throw new IllegalArgumentException("Task must be an AbstractArchiveTask");
        }
    }
    
    public void mainPublication(Provider<RegularFile> file) {
        this.mainPublication.set(file);
    }
    
    public void mainPublicationDepends(Task... tasks) {
        this.mainPublicationDependencies.addAll(tasks);
    }
    
    public void relations(Action<PublicationRelations> action) {
        action.execute(relations);
    }
    
    public void configure(Project project, Task baseTask) {}
    
    public Property<String> getDisplayName() {
        return displayName;
    }
    
    public Property<String> getVersion() {
        return version;
    }
    
    public Property<String> getChangelog() {
        return changelog;
    }
    
    public Property<String> getReleaseType() {
        return releaseType;
    }
    
    public ListProperty<String> getGameVersions() {
        return gameVersions;
    }
    
    public ListProperty<String> getGameLoaders() {
        return gameLoaders;
    }
    
    public RegularFileProperty getMainPublication() {
        return mainPublication;
    }
    
    public ListProperty<Task> getMainPublicationDependencies() {
        return mainPublicationDependencies;
    }
}
