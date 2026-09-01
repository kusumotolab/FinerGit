package finergit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarEntryPoint {

  private static final Logger log = LoggerFactory.getLogger(JarEntryPoint.class);

  public static void main(final String[] args) {

    if (0 == args.length) {
      System.err.println("no task is specified");
      printUsage();
      System.exit(FinerGitMain.EXIT_FAILURE);
    }

    final String[] realArgs = Arrays.copyOfRange(args, 1, args.length);

    String className = null;
    switch (args[0]) {
      case "create": {
        className = "finergit.FinerGitMain";
        break;
      }
      default: {
        System.err.println("undefined task: " + args[0]);
        printUsage();
        System.exit(FinerGitMain.EXIT_FAILURE);
      }
    }

    try {
      final Class<?> main = Class.forName(className);
      final Method method = main.getMethod("main", String[].class);
      method.invoke(null, new Object[] {realArgs});

    } catch (final ClassNotFoundException e) {
      log.error("unknown class Name \"{}\"", className);
      System.exit(FinerGitMain.EXIT_FAILURE);
    } catch (final NoSuchMethodException e) {
      log.error("main method was not found in class");
      System.exit(FinerGitMain.EXIT_FAILURE);
    } catch (final InvocationTargetException e) {
      log.error("An exception was thrown by invoked main method", e.getCause());
      System.exit(FinerGitMain.EXIT_FAILURE);
    } catch (final IllegalAccessException e) {
      log.error("failed to access main method");
      System.exit(FinerGitMain.EXIT_FAILURE);
    }
  }

  private static void printUsage() {
    System.err.println("usage: java -jar FinerGit-all.jar create <options>");
    System.err.println("to print the options: java -jar FinerGit-all.jar create --help");
  }
}
