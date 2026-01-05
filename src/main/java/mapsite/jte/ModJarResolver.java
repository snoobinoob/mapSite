package mapsite.jte;

import gg.jte.CodeResolver;
import gg.jte.TemplateNotFoundException;
import gg.jte.compiler.IoUtils;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.ModLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ModJarResolver implements CodeResolver {
    private final JarFile modJar;
    private final String resourceRoot;

    public ModJarResolver(String modId, String resourceRoot) {
        Optional<LoadedMod> mod = ModLoader.getEnabledMods().stream().filter(m -> m.id.equals(modId)).findFirst();
        if (mod.isEmpty()) {
            throw new RuntimeException("Could not find mod with ID: " + modId);
        }
        modJar = mod.get().jarFile;
        this.resourceRoot = resourceRoot;
    }

    @Override
    public String resolve(String name) {
        try {
            return resolveRequired(name);
        } catch (TemplateNotFoundException e) {
            return null;
        }
    }

    @Override
    public String resolveRequired(String name) throws TemplateNotFoundException {
        try (InputStream is = modJar.getInputStream(getEntry(name))) {
            return is == null ? null : IoUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException("Could not read resource from " + resourceRoot + name);
        }
    }

    @Override
    public long getLastModified(String name) {
        return getEntry(name).getTime();
    }

    @Override
    public List<String> resolveAllTemplateNames() {
        return null;
    }

    @Override
    public boolean exists(String name) {
        try {
            getEntry(name);
        } catch (TemplateNotFoundException e) {
            return false;
        }
        return true;
    }

    private ZipEntry getEntry(String name) {
        ZipEntry entry = modJar.getEntry(resourceRoot + name);
        if (entry == null) {
            throw new TemplateNotFoundException(name + " not found (" + resourceRoot + name + ")");
        }
        return entry;
    }
}
