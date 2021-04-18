package com.byern.tanima.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.byern.tanima.ClientMain;
import com.byern.tanima.KryonetClientServer;

import java.util.Optional;

public class DesktopLauncher {
  public static void main(String[] arg) {
    System.setProperty("org.lwjgl.librarypath", "./");
    System.setProperty("java.library.path", "./");
    System.out.println("java.library.path " + System.getProperty("java.library.path"));
    System.out.println("org.lwjgl.librarypath " + System.getProperty("org.lwjgl.librarypath"));
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

    new Lwjgl3Application(
       new ClientMain(
          Optional.of(
             new KryonetClientServer.ClientServerConfig(
                arg.length > 0 ? Integer.parseInt(arg[0]) : 6669
             )
          )
       ),
       config
    );

  }
}
