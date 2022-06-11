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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.dependencies.DependencyType;
import com.modrinth.minotaur.dependencies.ModDependency;
import me.shedaniel.unifiedpublishing.impl.TaskModrinthUpload;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ModrinthPublishingTarget extends BasePublishingTarget {
    public final Property<String> token;
    public final Property<String> id;
    
    @Inject
    public ModrinthPublishingTarget(Project project, UnifiedPublishingProject extension) {
        super(project, extension, relation -> relation.modrinth);
        
        this.token = project.getObjects().property(String.class);
        this.id = project.getObjects().property(String.class);
    }
    
    @Override
    public void configure(Project project, Task baseTask) {
        String token = Objects.requireNonNull(this.token.getOrNull(), "CurseForge target has unspecified token!");
        String projectId = Objects.requireNonNull(this.id.getOrNull(), "CurseForge target has unspecified project ID!");
        String displayName = this.displayName.getOrNull();
        String releaseType = this.releaseType.get();
        String changelog = this.changelog.get();
        
        TaskModrinthUpload task = project.getTasks().create("modrinth" + projectId, TaskModrinthUpload.class, this);
        baseTask.dependsOn(task);
        task.dependsOn(this.secondaryPublications.getBuiltBy());
        task.dependsOn(this.publicationDependencies);
    }
    
    public Property<String> getToken() {
        return token;
    }
    
    public Property<String> getId() {
        return id;
    }
    
    public Collection<Dependency> createDependencies() {
        List<Dependency> dependencies = new ArrayList<>();
        
        PublicationRelations publicationRelations = this.relations;
        
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            for (String relation : publicationRelations.depends.getOrElse(Collections.emptyList())) {
                dependencies.add(new ModDependency(resolveId(client, relation), DependencyType.REQUIRED));
            }
            
            for (String relation : publicationRelations.optionals.getOrElse(Collections.emptyList())) {
                dependencies.add(new ModDependency(resolveId(client, relation), DependencyType.OPTIONAL));
            }
            
            for (String relation : publicationRelations.conflicts.getOrElse(Collections.emptyList())) {
                dependencies.add(new ModDependency(resolveId(client, relation), DependencyType.INCOMPATIBLE));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        
        return dependencies;
    }
    
    private String resolveId(CloseableHttpClient client, String relation) throws IOException {
        HttpGet get = new HttpGet("https://api.modrinth.com/v2/project/" + relation);
        CloseableHttpResponse response = client.execute(get);
        String returned = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.toString());
        JsonElement element = new JsonParser().parse(returned);
        if (!element.isJsonObject()) {
            throw new IllegalStateException("Invalid response from Modrinth, expected JsonObject: " + returned);
        }
        if (!element.getAsJsonObject().has("id")) {
            throw new IllegalStateException("Invalid response from Modrinth, expected id: " + returned);
        }
        return element.getAsJsonObject().get("id").getAsString();
    }
}
