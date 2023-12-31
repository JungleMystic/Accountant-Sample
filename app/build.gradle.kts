plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // SafeArgs
    id ("androidx.navigation.safeargs.kotlin")

    // ksp
    id ("com.google.devtools.ksp")
}

android {
    namespace = "com.lrm.accountant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lrm.accountant"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation Component
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.3")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.3")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("androidx.fragment:fragment-ktx:1.6.1")

    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Lifecycle extensions
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Easy Permissions
    implementation ("com.vmadalin:easypermissions-ktx:1.0.0")

    // Lottie Animation
    implementation ("com.airbnb.android:lottie:6.1.0")

    // Circle image view library
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // Glide for images
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    ksp("com.github.bumptech.glide:ksp:4.14.2")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // itextpdf library to convert to pdf format
    implementation("com.itextpdf:itextpdf:5.5.13.3")

    // Apache poi for Microsoft excel
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")

    // For scanning documents
    implementation ("com.github.zynkware:Document-Scanning-Android-SDK:1.1.1")

    //PDF View
    implementation ("com.github.barteksc:android-pdf-viewer:2.8.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}