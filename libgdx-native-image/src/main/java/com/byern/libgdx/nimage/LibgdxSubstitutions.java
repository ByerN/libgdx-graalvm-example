package com.byern.libgdx.nimage;


import com.badlogic.gdx.utils.GdxRuntimeException;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@TargetClass(com.badlogic.gdx.utils.SharedLibraryLoader.class)
final class com_badlogic_gdx_utils_SharedLibraryLoader {

  @Alias
  private String nativesJar;

  @Substitute
  private InputStream readFile (String path) {
    if (nativesJar == null) {
      //Line below causes error
      //InputStream input = SharedLibraryLoader.class.getResourceAsStream("/" + path);
      InputStream input = null;
      try {
        input = new FileInputStream(System.getProperty("java.library.path") + "\\" + path);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      if (input == null) throw new GdxRuntimeException("Unable to read file for extraction: " + path);
      return input;
    }

    // Read from JAR.
    try {
      ZipFile file = new ZipFile(nativesJar);
      ZipEntry entry = file.getEntry(path);
      if (entry == null) throw new GdxRuntimeException("Couldn't find '" + path + "' in JAR: " + nativesJar);
      return file.getInputStream(entry);
    } catch (IOException ex) {
      throw new GdxRuntimeException("Error reading '" + path + "' in JAR: " + nativesJar, ex);
    }
  }
}
public class LibgdxSubstitutions {
}
