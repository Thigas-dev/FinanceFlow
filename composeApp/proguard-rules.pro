# Room entities / enums are referenced by generated code; keep enum names stable.
-keepclassmembers enum com.financeflow.app.** { *; }
-keep class * extends androidx.work.ListenableWorker { <init>(...); }
