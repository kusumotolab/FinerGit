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
  }, V1_14 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_14;
    }
  }, V1_15 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_15;
    }
  }, V1_16 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_16;
    }
  }, V1_17 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_17;
    }
  }, V1_18 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_18;
    }
  }, V1_19 {
    @Override
    protected String getJavaCore() {
      return JavaCore.VERSION_19;
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
      case "1.14":
        return V1_14;
      case "1.15":
        return V1_15;
      case "1.16":
        return V1_16;
      case "1.17":
        return V1_17;
      case "1.18":
        return V1_18;
      case "1.19":
        return V1_19;
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
