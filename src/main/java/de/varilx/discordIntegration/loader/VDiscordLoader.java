package de.varilx.discordIntegration.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings({
        "unused",
        "UnstableApiUsage"
})
public class VDiscordLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("papermc", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("sonatype-public", "default", "https://oss.sonatype.org/content/groups/public/").build());
        resolver.addRepository(new RemoteRepository.Builder("varilx-development", "default", "https://reposilite.varilx.de/Varilx").build());

        resolver.addDependency(new Dependency(new DefaultArtifact("de.varilx:base-api:1.3.3"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("net.dv8tion:JDA:5.5.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains:annotations:26.0.2"), null));


        classpathBuilder.addLibrary(resolver);
    }
}
