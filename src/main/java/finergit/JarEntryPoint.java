package finergit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarEntryPoint {

  private static final Logger log = LoggerFactory.getLogger(JarEntryPoint.class);

  public static void main(final String[] args) {

    final String[] realArgs = Arrays.copyOfRange(args, 1, args.length);

    String className = null;
    switch (args[0]) {
      case "create": {
        className = "finergit.FinerGitMain";
        break;
      }
      default: {
        System.err.println("undefined task: " + args[0]);
        return;
      }
    }

    try {
      final Class<?> main = Class.forName(className);
      final Method method = main.getMethod("main", String[].class);
      method.invoke(null, new Object[] {realArgs});

    } catch (final ClassNotFoundException e) {
      log.error("unknown class Name \"{}\"", className);
    } catch (final NoSuchMethodException e) {
      log.error("main method was not found in class");
    } catch (final InvocationTargetException e) {
      log.error("An exception was thrown by invoked main method");
      log.error(e.getMessage());
    } catch (final IllegalAccessException e) {
      log.error("failed to access main method");
    }
  }
}
