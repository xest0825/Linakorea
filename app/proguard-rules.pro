# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Common Rules
-keepattributes *Annotation*
-keepattributes JavascriptInterface

# InnerClasses
-keepattributes InnerClasses

# Realtime database
-keepattributes Signature

# APP class
-keep class kr.co.lina.ga.** { *; }
-keepclassmembers class kr.co.lina.ga.** { *; }
-keep interface kr.co.lina.ga.** { *; }

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

#
-keep public class * {
    public protected *;
}

#
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Added by Gagamel
-dontwarn org.apache.**
-dontwarn com.TouchEn.mVaccine.b2b2c.activity.**
-dontwarn com.secureland.smartmedic.core.**
-dontwarn com.bitdefender.antimalware.BDAVScanner

-keepclasseswithmembers class com.bitdefender.antimalware.BDAVScanner
 {
   private java.lang.String m_threatName;
   private int m_scanResult;
   private int m_threatType;
 }
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class *
{
   *** *Callback(...);
}
-keepclasseswithmembers class *
{
    int callbackEvent*(...);
}
-keep class com.TouchEn.mVaccine.DEMO.R$styleable{
	*;
}

-ignorewarnings
-keep public class com.yettiesoft.vestpin.service.VestPinLibImpl { *; }
-keep public interface com.yettiesoft.vestpin.service.VestPinCallback { *; }
-keep public enum com.yettiesoft.vestpin.service.VestPinLibImpl$CustomConfig {*;}
-keep public enum com.yettiesoft.vestpin.service.VestPinLibImpl$ResponsiveCustomConfig {*;}
-keep public class com.yettiesoft.vestpin.exception.VFException {*;}
-keep public enum com.yettiesoft.vestpin.exception.VFException$Web_Error {*;}
-keep public enum com.yettiesoft.vestpin.exception.VFException$ErrorContents {*;}
-keep public class com.yettiesoft.vestpin.storage.VPProvider { *; }
-keep public class com.yettiesoft.vestpin.VPVersion { *; }
-keep public class com.yettiesoft.oscar.service.Oscar { *; }
-keep public class com.yettiesoft.oscar.service.OscarCallback  { *; }
-keep public class com.yettiesoft.oscar.service.ConnectNative { *; }
-keep public class com.yettiesoft.oscar.network.HTTP { *; }
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepclassmembers class com.yettiesoft.vestpin.** { private <fields>; }
-keepattributes Exceptions

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
