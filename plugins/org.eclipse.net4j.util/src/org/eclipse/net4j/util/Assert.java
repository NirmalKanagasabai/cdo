package org.eclipse.net4j.util;


public final class Assert
{
  /* This class is not intended to be instantiated. */
  private Assert()
  {
    // not allowed
  }

  public static void isNotNull(Object object)
  {
    isNotNull(object, ""); //$NON-NLS-1$
  }

  public static void isNotNull(Object object, String message)
  {
    if (object == null)
    {
      String txt = "null argument:" + message; //$NON-NLS-1$
      System.out.println(txt);
      throw new AssertionFailedError(txt);
    }
  }

  public static boolean isTrue(boolean expression)
  {
    return isTrue(expression, ""); //$NON-NLS-1$
  }

  public static boolean isTrue(boolean expression, String message)
  {
    if (!expression)
    {
      String txt = "assertion failed: " + message; //$NON-NLS-1$
      System.out.println(txt);
      throw new AssertionFailedError(txt);
    }

    return expression;
  }

  public static boolean fail()
  {
    return fail(""); //$NON-NLS-1$
  }

  public static boolean fail(String message)
  {
    return isTrue(false, message);
  }
}
