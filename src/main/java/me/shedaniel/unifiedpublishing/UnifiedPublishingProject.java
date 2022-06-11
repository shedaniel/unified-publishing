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

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

public class UnifiedPublishingProject {
    private final Project project;
    
    public final Property<String> displayName;
    public final Property<String> version;
    public final Property<String> changelog;
    public final Property<String> releaseType;
    public final ListProperty<String> gameVersions;
    public final ListProperty<String> gameLoaders;
    
    public final RegularFileProperty mainPublication;
    public final ListProperty<Task> mainPublicationDependencies;
    
    public final ProjectRelations relations;
    
    @Nullable
    public CurseforgePublishingTarget curseforge;
    @Nullable
    public ModrinthPublishingTarget modrinth;
    
    @Inject
    public UnifiedPublishingProject(Project project) {
        this.project = project;
        
        this.version = project.getObjects().property(String.class)
                .convention(project.provider(() -> project.getVersion().toString()));
        this.displayName = project.getObjects().property(String.class)
                .convention(this.version);
        this.changelog = project.getObjects().property(String.class)
                .convention("");
        this.releaseType = project.getObjects().property(String.class)
                .convention("release");
        this.gameVersions = project.getObjects().listProperty(String.class)
                .convention(new ArrayList<>());
        this.gameLoaders = project.getObjects().listProperty(String.class)
                .convention(new ArrayList<>());
        
        this.mainPublication = project.getObjects().fileProperty();
        this.mainPublicationDependencies = project.getObjects().listProperty(Task.class);
        
        this.relations = project.getObjects().newInstance(ProjectRelations.class, project);
    }
    
    public void onConfigure(Project project, Task baseTask) {
        Stream.of(this.curseforge, this.modrinth)
                .filter(Objects::nonNull)
                .forEach(target -> {
                    target.configure(project, baseTask);
                });
    }
    
    public void curseforge(Action<CurseforgePublishingTarget> action) {
        if (curseforge == null) this.curseforge = project.getObjects().newInstance(CurseforgePublishingTarget.class, project, this);
        action.execute(curseforge);
    }
    
    public void modrinth(Action<ModrinthPublishingTarget> action) {
        if (modrinth == null) this.modrinth = project.getObjects().newInstance(ModrinthPublishingTarget.class, project, this);
        action.execute(modrinth);
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
    
    public void relations(Action<ProjectRelations> action) {
        action.execute(relations);
    }
    
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
