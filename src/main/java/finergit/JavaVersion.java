package finergit;

import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

public enum JavaVersion {

  V1_4 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_1_4;
    }
  }, V1_5 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_1_5;
    }
  }, V1_6 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_1_6;
    }
  }, V1_7 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_1_7;
    }
  }, V1_8 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_1_8;
    }
  }, V1_9 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_9;
    }
  }, V1_10 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_10;
    }
  }, V1_11 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_11;
    }
  }, V1_12 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_12;
    }
  }, V1_13 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_13;
    }
  };

  static public JavaVersion get(final String versionText) {
    switch (versionText) {
      case "1.4":
        return V1_4;
      case "1.5":
        return V1_5;
      case "1.6":
        return V1_6;
      case "1.7":
        return V1_7;
      case "1.8":
        return V1_8;
      case "1.9":
        return V1_9;
      case "1.10":
        return V1_10;
      case "1.11":
        return V1_11;
      case "1.12":
        return V1_12;
      case "1.13":
        return V1_13;
      default:
        return null;
    }
  }

  public Map<String, String> getOptions() {
    @SuppressWarnings("unchecked")
    final Map<String, String> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
    final String javaCore = this.getJavaCore();
    options.put(JavaCore.COMPILER_COMPLIANCE, javaCore);
    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaCore);
    options.put(JavaCore.COMPILER_SOURCE, javaCore);
    options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
    return options;
  }

  abstract protected String getJavaCore();
}
