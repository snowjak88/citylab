package org.snowjak.city.lwjgl3;

import org.snowjak.city.CityGame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.czyzby.autumn.fcs.scanner.DesktopClassScanner;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	
	public static void main(String[] args) {
		
		createApplication();
	}
	
	private static Lwjgl3Application createApplication() {
		
		return new Lwjgl3Application(new CityGame(new DesktopClassScanner()), getDefaultConfiguration());
	}
	
	private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
		
		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		configuration.setTitle("jCity");
		configuration.setWindowedMode(CityGame.WIDTH, CityGame.HEIGHT);
		configuration.setResizable(true);
		configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		return configuration;
	}
}