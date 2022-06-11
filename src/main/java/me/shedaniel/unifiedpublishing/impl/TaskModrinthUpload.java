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

package me.shedaniel.unifiedpublishing.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modrinth.minotaur.ModrinthExtension;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.request.VersionData;
import com.modrinth.minotaur.responses.ResponseError;
import com.modrinth.minotaur.responses.ResponseUpload;
import me.shedaniel.unifiedpublishing.ModrinthPublishingTarget;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A task used to communicate with Modrinth for the purpose of uploading build artifacts.
 */
public class TaskModrinthUpload extends DefaultTask {
    /**
     * Constant gson instance used for deserializing the API responses.
     */
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * The extension used for getting the data supplied in the buildscript.
     */
    private final ModrinthPublishingTarget target;
    
    /**
     * The response from the API when the file was uploaded successfully. Provided as a utility for those manually
     * creating their upload task.
     */
    @Nullable
    public ResponseUpload uploadInfo = null;
    
    /**
     * The response from the API when the file failed to upload. Provided as a utility for those manually creating their
     * upload task.
     */
    @Nullable
    public ResponseError errorInfo = null;
    
    /**
     * Internal initially empty List to add Dependencies to if any were added
     */
    private final List<Dependency> dependencies = new ArrayList<>();
    
    @Inject
    public TaskModrinthUpload(ModrinthPublishingTarget target) {
        this.target = target;
    }
    
    /**
     * Checks if the upload was successful or not. Provided as a utility for those manually creating their upload task.
     *
     * @return Whether the file was successfully uploaded.
     */
    @SuppressWarnings("unused")
    public boolean wasUploadSuccessful() {
        return this.uploadInfo != null && this.errorInfo == null;
    }
    
    /**
     * Defines what to do when the Modrinth upload task is invoked.
     * <ol>
     *   <li>Attempts to automatically resolve various metadata items if not specified, throwing an exception if some
     *   things still don't have anything set</li>
     *   <li>Resolves each file or task to be uploaded, ensuring they're all valid</li>
     *   <li>Uploads these files to the Modrinth API under a new version</li>
     * </ol>
     */
    @TaskAction
    public void apply() {
        this.getLogger().lifecycle("Minotaur: {}", this.getClass().getPackage().getImplementationVersion());
        if (target.getGameVersions().get().isEmpty()) {
            throw new GradleException("Cannot upload to Modrinth: no game versions specified!");
        }
        
        if (target.getGameLoaders().get().isEmpty()) {
            throw new GradleException("Cannot upload to Modrinth: no loaders specified!");
        }
        
        // Retrieves each DependencyContainer if any and adds a new Dependency based on if the projectId property is set 
        this.dependencies.addAll(target.createDependencies());
        
        List<File> filesToUpload = new ArrayList<>();
        
        final RegularFile primaryFile = target.getMainPublication().get();
        final File file = primaryFile.getAsFile();
        
        // Ensure the file actually exists before trying to upload it.
        if (file == null || !file.exists()) {
            this.getProject().getLogger().error("The upload file is missing or null. {}", primaryFile);
            throw new GradleException("The upload file is missing or null. " + primaryFile);
        }
        
        filesToUpload.add(file);
        
        for (Object fileObject : target.secondaryPublications.getFiles()) {
            final File resolvedFile = resolveFile(this.getProject(), fileObject);
            
            // Ensure the file actually exists before trying to upload it.
            if (resolvedFile == null || !resolvedFile.exists()) {
                this.getProject().getLogger().error("The upload file is missing or null. {}", fileObject);
                throw new GradleException("The upload file is missing or null. " + fileObject);
            }
            
            filesToUpload.add(resolvedFile);
        }
        
        try {
            this.upload(filesToUpload);
        } catch (final IOException e) {
            this.getProject().getLogger().error("Failed to upload the file!", e);
            throw new GradleException("Failed to upload the file!", e);
        }
    }
    
    /**
     * Uploads a file using the provided configuration.
     *
     * @param files The files to upload.
     * @throws IOException Whenever something goes wrong wit uploading the file.
     */
    public void upload(List<File> files) throws IOException {
        final HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()).build();
        final HttpPost post = new HttpPost(this.getUploadEndpoint());
        
        post.addHeader("Authorization", target.getToken().get());
        
        final MultipartEntityBuilder form = MultipartEntityBuilder.create();
        
        List<String> fileParts = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            fileParts.add(String.valueOf(i));
        }
        
        final VersionData data = new VersionData();
        data.setProjectId(target.getId().get());
        data.setVersionNumber(target.getVersion().get());
        data.setVersionTitle(target.getDisplayName().get());
        data.setChangelog(target.getChangelog().get().replaceAll("\r\n", "\n"));
        data.setVersionType(target.getReleaseType().get().toLowerCase(Locale.ROOT));
        data.setGameVersions(target.getGameVersions().get());
        data.setLoaders(target.getGameLoaders().get());
        data.setDependencies(this.dependencies);
        data.setFileParts(fileParts);
        data.setPrimaryFile("0"); // The primary file will always be of the first index in the list
        
        form.addTextBody("data", GSON.toJson(data), ContentType.APPLICATION_JSON);
        
        for (int i = 0; i < files.size(); i++) {
            this.getProject().getLogger().debug("Uploading {} to {}.", files.get(i).getPath(), this.getUploadEndpoint());
            form.addBinaryBody(String.valueOf(i), files.get(i));
        }
        
        post.setEntity(form.build());
        
        try {
            final HttpResponse response = client.execute(post);
            final int status = response.getStatusLine().getStatusCode();
            
            if (status == 200) {
                this.uploadInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseUpload.class);
                this.getProject().getLogger().lifecycle("Successfully uploaded version {} to {} as version ID {}.", this.uploadInfo.getVersionNumber(), target.getId().get(), this.uploadInfo.getId());
            } else {
                this.errorInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseError.class);
                if (this.errorInfo == null) {
                    this.getProject().getLogger().error("Error info is null - this could potentially mean that you're reusing a version number?");
                    this.errorInfo = new ResponseError();
                }
                this.getProject().getLogger().error("Upload failed! Status: {} Error: {} Reason: {}", status, this.errorInfo.getError(), this.errorInfo.getDescription());
                throw new GradleException("Upload failed! Status: " + status + " Reason: " + this.errorInfo.getDescription());
            }
        } catch (final IOException e) {
            this.getProject().getLogger().error("Failure to upload files!", e);
            throw e;
        }
    }
    
    /**
     * Provides the upload API endpoint to use.
     *
     * @return The upload API endpoint.
     */
    private String getUploadEndpoint() {
        String apiUrl = ModrinthExtension.DEFAULT_API_URL;
        return apiUrl + (apiUrl.endsWith("/") ? "" : "/") + "version";
    }
    
    /**
     * Attempts to resolve a file using an arbitrary object provided by a user defined gradle
     * task.
     *
     * @param project The project instance. This is used as a last resort to resolve the file using Gradle's built-in
     *                handling.
     * @param in      The arbitrary input object from the user.
     * @return A file handle for the resolved input. If the input can not be resolved this will be null or the fallback.
     */
    @Nullable
    private static File resolveFile(Project project, Object in) {
        // If input or project is null we can't really do anything...
        if (in == null || project == null) {
            return null;
        }
        
        // If the file is a Java file handle no additional handling is needed.
        else if (in instanceof File) {
            return (File) in;
        }
        
        // Grabs the file from an archive task. Allows build scripts to do things like the jar task directly.
        else if (in instanceof AbstractArchiveTask) {
            return ((AbstractArchiveTask) in).getArchiveFile().get().getAsFile();
        }
        
        // Grabs the file from an archive task wrapped in a provider. Allows Kotlin DSL buildscripts to also specify
        // the jar task directly, rather than having to call #get() before running.
        else if (in instanceof TaskProvider<?>) {
            Object provided = ((TaskProvider<?>) in).get();
            
            // Check to see if the task provided is actually an AbstractArchiveTask.
            if (provided instanceof AbstractArchiveTask) {
                return ((AbstractArchiveTask) provided).getArchiveFile().get().getAsFile();
            }
        }
        
        // Fallback to Gradle's built-in file resolution mechanics.
        return project.file(in);
    }
}
