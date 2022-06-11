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
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class ProjectRelation {
    public final Property<String> curseforge;
    public final Property<String> modrinth;
    
    @Inject
    public ProjectRelation(Project project) {
        this.curseforge = project.getObjects().property(String.class);
        this.modrinth = project.getObjects().property(String.class);
    }
    
    public Property<String> getCurseforge() {
        return curseforge;
    }
    
    public Property<String> getModrinth() {
        return modrinth;
    }
}
