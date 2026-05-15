plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.gema.cobraplus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gema.cobraplus"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.3"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //FirebaseUI para autenticación con Firebase Realtime database -> NO NECESARIO REALTIME
   // implementation(libs.firebase.ui.auth)

    // Import BoM de Firebase para gestonar versiones automáticamente
    implementation(platform(libs.firebase.bom))

    // Dependencia para usar Firestore (base de datos)
    implementation(libs.firebase.firestore)

    // Dependencia para Storage (almacenar archivos)
    implementation(libs.firebase.storage)


    // Dependencia para usar Firebase Authentication (inicio sesión)
    // al usar BoM, no es necesario especificar versión
    implementation(libs.google.firebase.auth)

    // Dependencia para Credencial Manager (gestión de credenciales con Google)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

   //Glide Java (para cargar imágenes, no utitizo de momento)
    implementation(libs.glide)
    annotationProcessor(libs.glidecompiler)

    //Dependencias de test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dependencia para generar archivos Excel
    implementation(libs.poi.ooxml)
}