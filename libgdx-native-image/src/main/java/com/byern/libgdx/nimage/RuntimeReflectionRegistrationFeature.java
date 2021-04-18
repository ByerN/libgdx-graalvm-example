package com.byern.libgdx.nimage;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/*
  This class shows how reflection config can be implemented in java instead of json config files.
 */
@AutomaticFeature
class RuntimeReflectionRegistrationFeature implements Feature {
  public void beforeAnalysis(BeforeAnalysisAccess access) {
    try {
      RuntimeReflection.register(java.lang.Object.class);
      RuntimeReflection.register(com.badlogic.gdx.LifecycleListener.class);
      RuntimeReflection.register(com.badlogic.gdx.backends.lwjgl3.audio.OpenALMusic.class);
      RuntimeReflection.register(org.lwjgl.PointerBuffer.class);
      RuntimeReflection.register(org.lwjgl.PointerBuffer.class.getDeclaredFields());
      RuntimeReflection.register(org.lwjgl.PointerBuffer.class.getMethods());
      RuntimeReflection.register(org.lwjgl.PointerBuffer.class.getDeclaredClasses());
      RuntimeReflection.register(org.lwjgl.PointerBuffer.class.getDeclaredConstructors());

      RuntimeReflection.register(false, true, java.nio.Buffer.class.getDeclaredField("address"));
      RuntimeReflection.register(java.io.File.class.getDeclaredMethod("canExecute"));
      RuntimeReflection.register(java.lang.ClassLoader.class.getDeclaredMethod("findLibrary", String.class));
      RuntimeReflection.register(sun.misc.Unsafe.class.getDeclaredFields());
      RuntimeReflection.register(
         com.badlogic.gdx.backends.lwjgl3.audio.Wav.Music.class.getConstructor(
            com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio.class,
            com.badlogic.gdx.files.FileHandle.class
         )
      );
      RuntimeReflection.register(
         com.badlogic.gdx.backends.lwjgl3.audio.Mp3.Music.class.getConstructor(
            com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio.class,
            com.badlogic.gdx.files.FileHandle.class
         )
      );
      RuntimeReflection.register(
         com.badlogic.gdx.backends.lwjgl3.audio.Wav.Sound.class.getConstructor(
            com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio.class,
            com.badlogic.gdx.files.FileHandle.class
         )
      );
      RuntimeReflection.register(false, true, org.lwjgl.opengl.GLCapabilities.class.getDeclaredFields());

  } catch (NoSuchMethodException | NoSuchFieldException e ) {
      e.printStackTrace();
    }
  }
}
