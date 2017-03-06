package io.skypvp.uhc.util;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;

public class FireworkEffectBuilder {
	
	public static FireworkEffect buildRandomEffect() {
		Random rand = new Random();
		Type[] effectTypes = {Type.BALL, Type.BALL_LARGE, Type.BURST, Type.CREEPER, Type.STAR};
		Color[] colors = {Color.AQUA, Color.BLACK, Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE, Color.RED, Color.SILVER, Color.TEAL, Color.WHITE, Color.YELLOW};
		return FireworkEffect.builder()
			.with(effectTypes[rand.nextInt(effectTypes.length)])
			.withColor(colors[rand.nextInt(colors.length)])
			.withFade(colors[rand.nextInt(colors.length)])
			.flicker(rand.nextBoolean())
			.trail(rand.nextBoolean())
			.build();
	}
}
