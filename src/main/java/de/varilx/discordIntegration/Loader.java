package de.varilx.discordIntegration;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class Loader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder builder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("derioo", "default", "https://reposilite.varilx.de/Varilx").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());


        resolver.addDependency(new Dependency(new DefaultArtifact("net.dv8tion:JDA:5.2.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("de.varilx:base-api:1.1.1"), null));


        builder.addLibrary(resolver);
    }
}
