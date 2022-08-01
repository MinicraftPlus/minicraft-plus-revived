package minicraft.core.io;

import com.studiohartman.jamepad.*;
import minicraft.util.Logging;
import org.tinylog.Logger;

import java.util.HashMap;

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

		initButtonMap();
	}

	private HashMap<String, ControllerButton> buttonmap = new HashMap<>();
	private void initButtonMap() {
		buttonmap.put("MOVE-UP", ControllerButton.DPAD_UP);
		buttonmap.put("MOVE-DOWN", ControllerButton.DPAD_DOWN);
		buttonmap.put("MOVE-LEFT", ControllerButton.DPAD_LEFT);
		buttonmap.put("MOVE-RIGHT", ControllerButton.DPAD_RIGHT);

		buttonmap.put("CURSOR-UP", ControllerButton.DPAD_UP);
		buttonmap.put("CURSOR-DOWN", ControllerButton.DPAD_DOWN);
		buttonmap.put("CURSOR-LEFT", ControllerButton.DPAD_LEFT);
		buttonmap.put("CURSOR-RIGHT", ControllerButton.DPAD_RIGHT);

		buttonmap.put("SELECT", ControllerButton.A);
		buttonmap.put("EXIT", ControllerButton.B);

		buttonmap.put("ATTACK", ControllerButton.A);
		buttonmap.put("MENU", ControllerButton.X);
		buttonmap.put("CRAFT", ControllerButton.Y);
		buttonmap.put("PICKUP", ControllerButton.LEFTBUMPER);

		buttonmap.put("PAUSE", ControllerButton.START);
	}

	public boolean buttonPressed(ControllerButton button) {
		try {
			return controllerIndex.isButtonJustPressed(button);
		} catch (ControllerUnpluggedException e) {
			return false;
		}
	}
}
