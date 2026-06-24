# 忽略系统隐藏 API 缺失警告 (针对系统应用开发)
-dontwarn android.os.SystemProperties
-dontwarn android.view.WindowManagerGlobal
-dontwarn android.view.IWindowManager
-dontwarn android.os.ServiceManager

# # -------------------------------------------
# #  ############### 基础参数配置 ###############
# # -------------------------------------------
#指定压缩级别
-optimizationpasses 5
#混淆时采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#混淆时不使用大小写混合，混淆后的类名为小写(大小写混淆容易导致class文件相互覆盖）
-dontusemixedcaseclassnames
#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification
#保留行号, 保持泛型, 保持注解
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# # -------------------------------------------
# #  ############### 通用混淆规则 ###############
# # -------------------------------------------
# 保留四大组件，自定义的Application,Fragment等这些类不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
#保留在Activity中的方法参数是view的方法，
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Serializable
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Parcelable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# For XML inflating, keep views'
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# 保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留本地native方法不被混淆
-keepclasseswithmembers class * {
    native <methods>;
}

# 保留R下面的资源
-keep class **.R$* {*;}
#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

# # -------------------------------------------
# #  ############### Retrofit混淆 ###############
# # -------------------------------------------
# Reference link https://github.com/square/retrofit/blob/master/retrofit/src/main/resources/META-INF/proguard/retrofit2.pro
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-keep class retrofit2.Retrofit { *; }
-keep class retrofit2.converter.gson.GsonConverterFactory { *; }
#-keep class retrofit2.converter.scalars.ScalarsConverterFactory { *; }
-keep class retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory { *; }


# # -------------------------------------------
# #  ############### JodaTime混淆 ###############
# # -------------------------------------------
# Reference link https://github.com/dlew/joda-time-android/blob/main/library/proguard-rules.txt
# All the resources are retrieved via reflection, so we need to make sure we keep them
-keep class net.danlew.android.joda.R$raw { *; }
# These aren't necessary if including joda-convert, but
# most people aren't, so it's helpful to include it.
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
# Joda classes use the writeObject special method for Serializable, so
# if it's stripped, we'll run into NotSerializableExceptions.
# https://www.guardsquare.com/en/products/proguard/manual/examples#serializable
-keepnames class org.joda.** implements java.io.Serializable
-keepclassmembers class org.joda.** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# # -------------------------------------------
# #  ############### Gson混淆 ###############
# # -------------------------------------------
# Reference link https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson

# keep model class
-keep class cn.booslink.llm.common.model.** { *; }
-keep class cn.booslink.llm.downloader.model.** { *; }
-keep class cn.booslink.llm.speech.config.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
##---------------End: proguard configuration for Gson  ----------

# 7. RxJava
# # -------------------------------------------
# #  ############### RxJava混淆 ###############
# # -------------------------------------------
#Reference link # https://github.com/ReactiveX/RxJava#r8-and-proguard-settings
-dontwarn java.util.concurrent.Flow*

# 8. Glide
# # -------------------------------------------
# #  ############### Glide混淆 ###############
# # -------------------------------------------
#Reference link https://github.com/bumptech/glide/blob/master/library/proguard-rules.txt
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# 9. Timber
# # -------------------------------------------
# #  ############### Timber混淆 ###############
# # -------------------------------------------
# Reference link https://github.com/JakeWharton/timber/blob/trunk/timber/consumer-proguard-rules.pro
-dontwarn org.jetbrains.annotations.**
# # -------------------------------------------

# 10. OkDownload
# # -------------------------------------------
# #  ############### OkDownload混淆 ###############
# # -------------------------------------------
# Reference link https://github.com/lingochamp/okdownload/blob/master/sample/proguard-rules.pro
# https://github.com/square/okhttp/#proguard
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# ------- end okhttp proguard rules ----
# ------- because of we using com.liulishuo.okdownload:okhttp on sample ----
-keepnames class com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
# ------- end com.liulishuo.okdownload:okhttp proguard rules ----
# ------- because of we using com.liulishuo.okdownload:sqlite on sample ----
-keep class com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite {
    public com.liulishuo.okdownload.core.breakpoint.DownloadStore createRemitSelf();
    public com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite(android.content.Context);
}
# ------- end com.liulishuo.okdownload:sqlite proguard rules ----
# don't warn SuppressFBWarnings annotation used in com.liulishuo.okdownload.DownloadContex.java
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
# # -------------------------------------------
# Android Stuido 在Make Module后给的建议
-dontwarn android.content.pm.*

# # -------------------------------------------
# #  ############### 安装相关的类 ###############
# # -------------------------------------------
-keep class cn.booslink.llm.downloader.observer.PackageInstallObserver {
    *;
}
# # -------------------------------------------

# 11. 讯飞 AIUI SDK
-dontoptimize
-keep class com.iflytek.** { *; }

# 4. WorkManager & Room (修复 WorkDatabase 实例化失败及 InputMerger 反射异常)
-keep class androidx.work.OverwritingInputMerger { *; }
-keep class androidx.work.ArrayCreatingInputMerger { *; }
-keep class androidx.work.impl.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class androidx.room.** { *; }
-keep class androidx.startup.** { *; }

# 12. PAGLib
-keep class org.libpag.** { *; }
-keep class androidx.exifinterface.** { *; }

# 13. BlurView
-keep class eightbitlab.com.blurview.** { *; }