package minicraft.core.io;

import com.studiohartman.jamepad.*;
import minicraft.util.Logging;
import org.tinylog.Logger;

public class ControllerHandler extends ControllerManager {
	private ControllerManager controllerManager = new ControllerManager();
	private ControllerIndex controllerIndex;

	public ControllerHandler() {
		controllerManager.initSDLGamepad();
		controllerIndex = controllerManager.getControllerIndex(0);
		controllerManager.update();
		try {
			Logging.CONTROLLER.debug("Controller Detected: " + controllerManager.getControllerIndex(0).getName());
		} catch (ControllerUnpluggedException e) {
			Logging.CONTROLLER.debug("No Controllers Detected, moving on.");
		}

	}

	public boolean buttonPressed(ControllerButton button) {
		try {
			return controllerIndex.isButtonJustPressed(button);
		} catch (ControllerUnpluggedException e) {
			throw new RuntimeException(e);
		}
	}
}
