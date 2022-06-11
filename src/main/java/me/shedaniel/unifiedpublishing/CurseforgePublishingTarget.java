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

import com.matthewprenger.cursegradle.*;
import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CurseforgePublishingTarget extends BasePublishingTarget {
    private CurseProject curseProject;
    public final Property<String> token;
    public final Property<String> id;
    
    @Inject
    public CurseforgePublishingTarget(Project project, UnifiedPublishingProject extension) {
        super(project, extension, relation -> relation.curseforge);
        
        this.token = project.getObjects().property(String.class);
        this.id = project.getObjects().property(String.class);
        
        CurseExtension curseExtension = project.getExtensions().getByType(CurseExtension.class);
        curseExtension.project(new Closure(null) {
            public void doCall(CurseProject curseProject) {
                CurseforgePublishingTarget.this.curseProject = curseProject;
            }
        });
    }
    
    @Override
    public void configure(Project project, Task baseTask) {
        project.getTasks().getByName("curseforge", baseTask::dependsOn);
        CurseUploadTask uploadTask = this.curseProject.getUploadTask();
        
        String token = Objects.requireNonNull(this.token.getOrNull(), "CurseForge target has unspecified token!");
        String projectId = Objects.requireNonNull(this.id.getOrNull(), "CurseForge target has unspecified project ID!");
        String displayName = this.displayName.getOrNull();
        String releaseType = this.releaseType.get();
        String changelog = this.changelog.get();
        List<Object> versionStrings = Stream.of(this.gameVersions.get().stream(), this.gameLoaders.get().stream())
                .distinct().collect(Collectors.toList());
        
        CurseArtifact mainArtifact = new CurseArtifact();
        mainArtifact.setArtifact(this.mainPublication);
        if (displayName != null) mainArtifact.setDisplayName(displayName);
        mainArtifact.setReleaseType(releaseType);
        mainArtifact.setChangelog(changelog);
        mainArtifact.setGameVersionStrings(versionStrings);
        mainArtifact.relations(new Closure(null) {
            public void doCall(CurseRelation relations) {
                PublicationRelations publicationRelations = CurseforgePublishingTarget.this.relations;
                
                for (String relation : publicationRelations.depends.getOrElse(Collections.emptyList())) {
                    relations.requiredDependency(relation);
                }
                
                for (String relation : publicationRelations.includes.getOrElse(Collections.emptyList())) {
                    relations.embeddedLibrary(relation);
                }
                
                for (String relation : publicationRelations.optionals.getOrElse(Collections.emptyList())) {
                    relations.optionalDependency(relation);
                }
                
                for (String relation : publicationRelations.conflicts.getOrElse(Collections.emptyList())) {
                    relations.incompatible(relation);
                }
            }
        });
        
        if (uploadTask != null) {
            uploadTask.setProjectId(projectId);
            
            uploadTask.setMainArtifact(mainArtifact);
            uploadTask.dependsOn(mainArtifact);
            uploadTask.dependsOn(this.mainPublicationDependencies);
        } else {
            this.curseProject.setApiKey(token);
            this.curseProject.setId(projectId);
            this.curseProject.setReleaseType(releaseType);
            this.curseProject.setChangelog(changelog);
            this.curseProject.setGameVersionStrings(versionStrings);
            
            this.curseProject.setMainArtifact(mainArtifact);
            
            project.afterEvaluate($ -> {
                CurseUploadTask ut = Objects.requireNonNull(this.curseProject.getUploadTask(), "CurseForge target has unspecified upload task!");
                ut.dependsOn(mainArtifact);
                ut.dependsOn(this.mainPublicationDependencies);
            });
        }
    }
    
    public Property<String> getToken() {
        return token;
    }
    
    public Property<String> getId() {
        return id;
    }
}
