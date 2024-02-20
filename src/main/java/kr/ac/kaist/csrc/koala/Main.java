package kr.ac.kaist.csrc.koala;

import kr.ac.kaist.csrc.koala.gui.structure.SplashScreen;

public class Main {

	public static void main(String[] args) {
		SplashScreen splash = new SplashScreen();
		new Mainframe();
		splash.setVisible(false);
	}
}
