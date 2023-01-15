package minicraft.util.resource.reloader;

import java.io.IOException;

import minicraft.core.io.Sound;
import minicraft.util.Logging;
import minicraft.util.resource.Resource;
import minicraft.util.resource.ResourceManager;
import minicraft.util.resource.SyncReloadableResourceManager;

public class SoundReloader implements SyncReloadableResourceManager.SyncReloader {
	@Override
	public void reload(ResourceManager manager) {
		for (Resource resource : manager.getResources("assets/sounds/", p -> p.endsWith(".wav"))) {
			String name = resource.getName();
			try {
				Sound.loadSound(name.substring(0, name.length() - 4), resource.getAsInputStream(), resource.getResourcePackName());
			} catch (IOException e) {
				Logging.RESOURCEHANDLER_LOCALIZATION.debug(e, "Unable to load audio: {} in pack : {}", name, resource.getResourcePackName());
			}
		}
	}
}
