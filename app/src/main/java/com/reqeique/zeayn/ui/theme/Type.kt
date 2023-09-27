package com.reqeique.zeayn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.reqeique.zeayn.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
val def = "Castoro"
val fontName = GoogleFont("Space Grotesk")

val fontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider)
)

// Set of Material typography styles to start with
private val typography = Typography()
val Typography = Typography(
    displayLarge = typography.displayLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.W900),
    displayMedium = typography.displayMedium.copy(fontFamily = fontFamily),
    displaySmall = typography.displaySmall.copy(fontFamily = fontFamily),

    headlineLarge = typography.headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = typography.headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = typography.headlineSmall.copy(fontFamily = fontFamily),

    titleLarge = typography.titleLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.W900),
    titleMedium = typography.titleMedium.copy(fontFamily = fontFamily,fontWeight = FontWeight.W900),
    titleSmall = typography.titleSmall.copy(fontFamily = fontFamily,fontWeight = FontWeight.W900),

    bodyLarge = typography.bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = typography.bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = typography.bodySmall.copy(fontFamily = fontFamily),

    labelLarge = typography.labelLarge.copy(fontFamily = fontFamily),
    labelMedium = typography.labelMedium.copy(fontFamily = fontFamily),
    labelSmall = typography.labelSmall.copy(fontFamily = fontFamily),
)