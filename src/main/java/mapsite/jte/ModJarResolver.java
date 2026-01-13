package mapsite.jte;

import gg.jte.TemplateNotFoundException;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.ModLoader;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ModJarResolver {
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

    public void write(String name, ServletOutputStream out) {
        try (InputStream is = modJar.getInputStream(getEntry(name))) {
            if (is == null) {
                return;
            }
            out.write(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Could not read resource from " + resourceRoot + name);
        }
    }

    private ZipEntry getEntry(String name) {
        ZipEntry entry = modJar.getEntry(resourceRoot + name);
        if (entry == null) {
            throw new TemplateNotFoundException(name + " not found (" + resourceRoot + name + ")");
        }
        return entry;
    }
}
