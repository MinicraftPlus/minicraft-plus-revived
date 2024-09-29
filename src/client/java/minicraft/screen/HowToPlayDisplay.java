package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class HowToPlayDisplay extends Display {
	public HowToPlayDisplay() {
		super(true, new Menu.Builder(false, 1, RelPos.CENTER,
			new SelectEntry(Localization.getStaticDisplay("minicraft.displays.how_to_play.general"), () ->
				Game.setDisplay(new PopupDisplay(null, true,
					StringEntry.useLines(Color.WHITE, false,
						Localization.getLocalized("minicraft.displays.how_to_play.general.display.0",
							Game.input.getMapping("CURSOR-UP"), Game.input.getMapping("CURSOR-DOWN"),
							Game.input.getMapping("CURSOR-LEFT"), Game.input.getMapping("CURSOR-RIGHT")),
						Localization.getLocalized("minicraft.displays.how_to_play.general.display.1",
							Game.input.getMapping("SELECT")),
						Localization.getLocalized("minicraft.displays.how_to_play.general.display.2",
							Game.input.getMapping("EXIT")),
						Localization.getLocalized("minicraft.displays.how_to_play.general.display.3",
							Game.input.getMapping("CURSOR-LEFT"), Game.input.getMapping("CURSOR-RIGHT")),
						Localization.getLocalized("minicraft.displays.how_to_play.general.display.4",
							Game.input.getMapping("FULLSCREEN")),
						Localization.getLocalized("minicraft.displays.how_to_play.general.display.5",
							Game.input.getMapping("SEARCHER-BAR")),
						Localization.getLocalized("minicraft.displays.how_to_play.general.display.6",
							Game.input.getMapping("PAGE-UP"), Game.input.getMapping("PAGE-DOWN")))))),
			new SelectEntry(Localization.getStaticDisplay("minicraft.displays.how_to_play.general_controller"),
				() -> Game.setDisplay(new MessageDisplay(true,
					"minicraft.displays.how_to_play.general_controller.display.0",
					"minicraft.displays.how_to_play.general_controller.display.1",
					"minicraft.displays.how_to_play.general_controller.display.2",
					"minicraft.displays.how_to_play.general_controller.display.3",
					"minicraft.displays.how_to_play.general_controller.display.4",
					"minicraft.displays.how_to_play.general_controller.display.5",
					"minicraft.displays.how_to_play.general_controller.display.6"))),
			new SelectEntry(Localization.getStaticDisplay("minicraft.displays.how_to_play.in_game"), () ->
				Game.setDisplay(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER,
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game.movement"), () ->
						Game.setDisplay(new PopupDisplay(null, true,
							StringEntry.useLines(Color.WHITE, false,
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.movement.display.0",
									Game.input.getMapping("MOVE-UP"), Game.input.getMapping("MOVE-DOWN"),
									Game.input.getMapping("MOVE-LEFT"), Game.input.getMapping("MOVE-RIGHT")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.movement.display.1"))))),
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game.world_interaction"), () ->
						Game.setDisplay(new PopupDisplay(null, true,
							StringEntry.useLines(Color.WHITE, false,
								Localization.getLocalized(
									"minicraft.displays.how_to_play.in_game.world_interaction.display.0",
									Game.input.getMapping("ATTACK")),
								Localization.getLocalized(
									"minicraft.displays.how_to_play.in_game.world_interaction.display.1",
									Game.input.getMapping("PICKUP")))))),
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game.interface"), () ->
						Game.setDisplay(new PopupDisplay(null, true,
							StringEntry.useLines(Color.WHITE, false,
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.0",
									Game.input.getMapping("PAUSE")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.1",
									Game.input.getMapping("POTIONEFFECTS")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.2",
									Game.input.getMapping("SIMPPOTIONEFFECTS")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.3",
									Game.input.getMapping("EXPANDQUESTDISPLAY")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.4",
									Game.input.getMapping("TUGGLEHUD")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.5",
									Game.input.getMapping("SCREENSHOT")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.6",
									Game.input.getMapping("INFO")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.interface.display.7",
									Game.input.getMapping("QUICKSAVE")))))),
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game.inventory"), () ->
						Game.setDisplay(new PopupDisplay(null, true,
							StringEntry.useLines(Color.WHITE, false,
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.inventory.display.0",
									Game.input.getMapping("MENU")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.inventory.display.1",
									Game.input.getMapping("CRAFT")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.inventory.display.2",
									Game.input.getMapping("DROP-ONE"), Game.input.getMapping("DROP-STACK")),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.inventory.display.3"),
								Localization.getLocalized("minicraft.displays.how_to_play.in_game.inventory.display.4")))))
				).setTitle(Localization.getStaticDisplay("minicraft.displays.how_to_play.in_game")).createMenu()))),
			new SelectEntry(Localization.getStaticDisplay(
				"minicraft.displays.how_to_play.in_game_controller"), () ->
				Game.setDisplay(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER,
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game_controller.movement"), () ->
						Game.setDisplay(new MessageDisplay(true,
							"minicraft.displays.how_to_play.in_game_controller.movement.display.0",
							"minicraft.displays.how_to_play.in_game_controller.movement.display.1"))),
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game_controller.world_interaction"), () ->
						Game.setDisplay(new MessageDisplay(true,
							"minicraft.displays.how_to_play.in_game_controller.world_interaction.display.0",
							"minicraft.displays.how_to_play.in_game_controller.world_interaction.display.1"))),
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game_controller.interface"), () ->
						Game.setDisplay(new MessageDisplay(true,
							"minicraft.displays.how_to_play.in_game_controller.interface.display.0"))),
					new SelectEntry(Localization.getStaticDisplay(
						"minicraft.displays.how_to_play.in_game_controller.inventory"), () ->
						Game.setDisplay(new MessageDisplay(true,
							"minicraft.displays.how_to_play.in_game_controller.inventory.display.0",
							"minicraft.displays.how_to_play.in_game_controller.inventory.display.1",
							"minicraft.displays.how_to_play.in_game_controller.inventory.display.2",
							"minicraft.displays.how_to_play.in_game_controller.inventory.display.3",
							"minicraft.displays.how_to_play.in_game_controller.inventory.display.4")))
				).setTitle(Localization.getStaticDisplay("minicraft.displays.how_to_play.in_game_controller"))
					.createMenu()))),
			new SelectEntry(Localization.getStaticDisplay("minicraft.displays.how_to_play.game_goal"), () ->
				Game.setDisplay(new MessageDisplay(true,
					"minicraft.displays.how_to_play.game_goal.description")))
		).setTitle(Localization.getStaticDisplay("minicraft.displays.how_to_play")).createMenu());
	}
}
