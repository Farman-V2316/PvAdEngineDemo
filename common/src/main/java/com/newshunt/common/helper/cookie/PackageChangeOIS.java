package com.newshunt.common.helper.cookie;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/**
 * Package Change Object Input Stream
 *
 * @author arun.babu
 */
public class PackageChangeOIS extends ObjectInputStream {

  private static final String PHEONIX_COOKIE_PKG = "com.newshunt.shared.model.util.";
  private static final String IDEATE_COOKIE_PKG = "com.newshunt.common.helper.cookie.";

  protected PackageChangeOIS() throws IOException {
  }

  public PackageChangeOIS(InputStream input) throws StreamCorruptedException, IOException {
    super(input);
  }

  @Override
  protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
    ObjectStreamClass read = super.readClassDescriptor();
    if (read.getName().startsWith(PHEONIX_COOKIE_PKG)) {
      return ObjectStreamClass.lookup(
          Class.forName(read.getName().replace(PHEONIX_COOKIE_PKG, IDEATE_COOKIE_PKG)));
    }
    return read;
  }
}
