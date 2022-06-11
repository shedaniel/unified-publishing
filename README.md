# Unified Publishing

### Usage

Most Simple Usage:

```groovy
unifiedPublishing {
    project {
        gameVersions = ["1.18.2"]
        gameLoaders = ["fabric"]

        mainPublication tasks.remapJar // Declares the publicated jar

        var cfToken = System.getenv("CF_TOKEN")
        if (cfToken != null) {
            curseforge {
                token = cfToken
                id = "000000" // Required, must be a string, ID of CurseForge project
            }
        }

        var mrToken = System.getenv("MODRINTH_TOKEN")
        if (mrToken != null) {
            modrinth {
                token = mrToken
                id = "P7dR8mSH" // Required, must be a string, ID of Modrinth project
            }
        }
    }
}
```

Full Usage:

```groovy
unifiedPublishing {
    project {
        displayName = "v1.0.0" // Optional, name of the file
        version = "1.0.0" // Optional, Inferred from project by default
        changelog = "I am the changelog" // Optional, in markdown format
        releaseType = "release" // Optional, use "release", "beta" or "alpha"
        gameVersions = ["1.18.2"]
        gameLoaders = ["fabric"]

        mainPublication tasks.remapJar // Declares the publicated jar

        relations {
            depends { // Mark as a required dependency
                curseforge = "fabric-api" // Optional, project slug
                modrinth = "fabric-api" // Optional, project slug or id
            }
            includes {} // Mark as an included dependency
            optional {} // Mark as an optional dependency
            conflicts {} // Mark as a conflicted dependency
        }

        curseforge {
            token = System.getenv("CF_TOKEN")
            id = "000000" // Required, must be a string, ID of CurseForge project

            displayName = "v1.0.0" // Optional, Inferred from the property above by default
            version = "1.0.0" // Optional, Inferred from the property above by default
            changelog = "I am the changelog" // Optional, Inferred from the property above by default
            releaseType = "release" // Optional, Inferred from the property above by default
            gameVersions = ["1.18.2"] // Optional, Inferred from the property above by default
            gameLoaders = ["fabric"] // Optional, Inferred from the property above by default

            mainPublication tasks.remapJar // Optional, Inferred from the property above by default

            relations { // Optional, Inferred from the relations above by default
                depends "fabric-api" // Mark as a required dependency
                includes "fabric-api" // Mark as an included dependency
                optional "fabric-api" // Mark as an optional dependency
                conflicts "fabric-api" // Mark as a conflicted dependency
            }
        }

        modrinth {
            token = System.getenv("MODRINTH_TOKEN")
            id = "P7dR8mSH" // Required, must be a string, ID of Modrinth project

            displayName = "v1.0.0" // Optional, Inferred from the property above by default
            version = "1.0.0" // Optional, Inferred from the property above by default
            changelog = "I am the changelog" // Optional, Inferred from the property above by default
            releaseType = "release" // Optional, Inferred from the property above by default
            gameVersions = ["1.18.2"] // Optional, Inferred from the property above by default
            gameLoaders = ["fabric"] // Optional, Inferred from the property above by default

            mainPublication tasks.remapJar // Optional, Inferred from the property above by default

            relations { // Optional, Inferred from the relations above by default
                depends "fabric-api" // Mark as a required dependency
                includes "fabric-api" // Mark as an included dependency
                optional "fabric-api" // Mark as an optional dependency
                conflicts "fabric-api" // Mark as a conflicted dependency
            }
        }
    }
}
```
