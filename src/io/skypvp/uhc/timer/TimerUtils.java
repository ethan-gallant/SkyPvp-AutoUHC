package io.skypvp.uhc.timer;

import io.skypvp.uhc.SkyPVPUHC;

public class TimerUtils {
	
	public static MatchTimer createTimer(SkyPVPUHC main, String name, int seconds) {
		int[] timings = convertToMinutesAndSeconds(seconds);
		return new MatchTimer(main, name, timings[0], timings[1]);
	}
	
	public static int[] convertToMinutesAndSeconds(int rawSeconds) {
		int[] array = new int[2];
		int minutes = (rawSeconds / 60);
		int seconds = rawSeconds - (minutes * 60);
		array[0] = minutes;
		array[1] = seconds;
		return array;
	}
	
}
