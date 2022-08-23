package minicraft.core.io;

import com.studiohartman.jamepad.ControllerButton;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import minicraft.util.Logging;

import java.util.HashMap;

public class ControllerHandler extends ControllerManager {
	private final ControllerIndex controllerIndex;

	public ControllerHandler() {
		ControllerManager controllerManager = new ControllerManager(1,"resources/util/gamecontrollerdb.txt");
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

	private final HashMap<String, ControllerButton> buttonMap = new HashMap<>();
	private void initButtonMap() {
		buttonMap.put("MOVE-UP", ControllerButton.DPAD_UP);
		buttonMap.put("MOVE-DOWN", ControllerButton.DPAD_DOWN);
		buttonMap.put("MOVE-LEFT", ControllerButton.DPAD_LEFT);
		buttonMap.put("MOVE-RIGHT", ControllerButton.DPAD_RIGHT);

		buttonMap.put("CURSOR-UP", ControllerButton.DPAD_UP);
		buttonMap.put("CURSOR-DOWN", ControllerButton.DPAD_DOWN);
		buttonMap.put("CURSOR-LEFT", ControllerButton.DPAD_LEFT);
		buttonMap.put("CURSOR-RIGHT", ControllerButton.DPAD_RIGHT);

		buttonMap.put("SELECT", ControllerButton.A);
		buttonMap.put("EXIT", ControllerButton.B);

		buttonMap.put("ATTACK", ControllerButton.A);
		buttonMap.put("MENU", ControllerButton.X);
		buttonMap.put("CRAFT", ControllerButton.Y);
		buttonMap.put("PICKUP", ControllerButton.LEFTBUMPER);

		buttonMap.put("PAUSE", ControllerButton.START);

		buttonMap.put("DROP-ONE", ControllerButton.RIGHTBUMPER);
		buttonMap.put("DROP-STACK", ControllerButton.RIGHTSTICK);
	}

	public boolean buttonPressed(ControllerButton button) {
		try {
			return controllerIndex.isButtonJustPressed(button);
		} catch (ControllerUnpluggedException e) {
			return false;
		}
	}

	public boolean buttonDown(ControllerButton button) {
		try {
			return controllerIndex.isButtonPressed(button);
		} catch (ControllerUnpluggedException e){
			return false;
		}
	}

	public boolean isKeyPressed(String key) {
		return buttonPressed(buttonMap.get(key.toUpperCase()));
	}

	public boolean isKeyDown(String key) {
		return buttonDown(buttonMap.get(key.toUpperCase()));
	}
}
