package com.moodpulse.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

object AppIcons {
    private fun vector(name: String, pathStr: String): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString(pathStr).toNodes(),
            fill = SolidColor(Color.White)
        ).build()
    }

    val CalendarMonth by lazy {
        vector(
            "CalendarMonth",
            "M19,4h-1V2h-2v2H8V2H6v2H5C3.89,4,3.01,4.9,3.01,6L3,20c0,1.1,0.89,2,2,2h14c1.1,0,2-0.9,2-2V6C21,4.9,20.1,4,19,4z M19,20H5V10h14V20z M19,8H5V6h14V8z M9,14H7v-2h2V14z M13,14h-2v-2h2V14z M17,14h-2v-2h2V14z M9,18H7v-2h2V18z M13,18h-2v-2h2V18z M17,18h-2v-2h2V18z"
        )
    }

    val BarChart by lazy {
        vector(
            "BarChart",
            "M4,9h4v11H4V9z M10,4h4v16h-4V4z M16,13h4v7h-4V13z"
        )
    }

    val Insights by lazy {
        vector(
            "Insights",
            "M21,18l-5.5,-5.5l-4,4l-7.5,-7.5l1.41,-1.41l6.09,6.09l4,-4l6.91,6.91L21,18z M15,4l2.59,2.59l-4.18,4.18l1.41,1.41l4.18,-4.18L21.59,10.59L22,4H15z"
        )
    }

    val Tune by lazy {
        vector(
            "Tune",
            "M3,17v2h6v-2H3z M3,5v2h10V5H3z M13,21v-2h8v-2h-8v-2h-2v6H13z M7,9v2H3v2h4v2h2V9H7z M21,13v-2H11v2H21z M17,9h2V7h2V5h-2V3h-2V9z"
        )
    }

    val Schedule by lazy {
        vector(
            "Schedule",
            "M11.99,2C6.47,2,2,6.48,2,12s4.47,10,9.99,10C17.52,22,22,17.52,22,12S17.52,2,11.99,2z M12,20c-4.42,0-8-3.58-8-8s3.58-8,8-8s8,3.58,8,8S16.42,20,12,20z M12.5,7H11v6l5.25,3.15l0.75-1.23l-4.5-2.67V7z"
        )
    }

    val LocalFireDepartment by lazy {
        vector(
            "LocalFireDepartment",
            "M12,12.9c-0.66,0-1.2-0.54-1.2-1.2c0-0.45,0.25-0.84,0.61-1.04C10.74,9.6,9.59,8.51,9.08,7.09C7.43,8.74,6.4,11.05,6.4,13.6C6.4,17.13,9.27,20,12.8,20c3.53,0,6.4-2.87,6.4-6.4c0-2.3-1.22-4.32-3.05-5.46C15.69,9.81,14.77,11.39,12,12.9z"
        )
    }

    val ChevronLeft by lazy {
        vector(
            "ChevronLeft",
            "M15.41,7.41L14,6l-6,6l6,6l1.41-1.41L10.83,12L15.41,7.41z"
        )
    }

    val ChevronRight by lazy {
        vector(
            "ChevronRight",
            "M10,6L8.59,7.41L13.17,12l-4.58,4.59L10,18l6-6L10,6z"
        )
    }

    val LightMode by lazy {
        vector(
            "LightMode",
            "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7z M2,13h2c0.55,0,1-0.45,1-1s-0.45-1-1-1H2c-0.55,0-1,0.45-1,1S1.45,13,2,13z M20,13h2c0.55,0,1-0.45,1-1s-0.45-1-1-1h-2c-0.55,0-1,0.45-1,1S19.45,13,20,13z M11,2v2c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2z M11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1S11,19.45,11,20z"
        )
    }

    val DarkMode by lazy {
        vector(
            "DarkMode",
            "M12,3c-4.97,0-9,4.03-9,9s4.03,9,9,9c0.83,0,1.5-0.67,1.5-1.5c0-0.39-0.15-0.74-0.39-1.01c-0.23-0.26-0.38-0.61-0.38-0.99c0-0.83,0.67-1.5,1.5-1.5H16c2.76,0,5-2.24,5-5C21,6.03,16.97,3,12,3z"
        )
    }

    val BrightnessAuto by lazy {
        vector(
            "BrightnessAuto",
            "M10.85,12.65h2.3L12,9L10.85,12.65z M20,8.69V4h-4.69L12,0.69L8.69,4H4v4.69L0.69,12L4,15.31V20h4.69L12,23.31L15.31,20H20v-4.69L23.31,12L20,8.69z M14.3,16l-0.7,-2h-3.2l-0.7,2H7.8L11,7h2l3.2,9H14.3z"
        )
    }

    val Psychology by lazy {
        vector(
            "Psychology",
            "M19.95,11c0,-0.72,-0.2,-1.39,-0.55,-1.97C19.75,8.42,20,7.74,20,7c0,-1.66,-1.34,-3,-3,-3c-0.74,0,-1.42,0.25,-1.97,0.68C14.44,4.27,13.77,4.08,13.05,4.02C12.71,2.87,11.66,2.02,10.4,2.02c-1.66,0,-3,1.34,-3,3c0,0.14,0.02,0.27,0.04,0.4C6.6,5.81,6,6.84,6,8c0,0.9,0.38,1.71,0.98,2.28C6.37,10.85,6,11.63,6,12.5c0,1.13,0.64,2.11,1.58,2.62C7.23,15.65,7,16.3,7,17c0,1.66,1.34,3,3,3h1v2h4c1.1,0,2,-0.9,2,-2v-2.15c1.74,-0.81,2.95,-2.57,2.95,-4.85z"
        )
    }

    val Download by lazy {
        vector(
            "Download",
            "M19,9h-4V3H9v6H5l7,7L19,9z M5,18v2h14v-2H5z"
        )
    }

    val TableChart by lazy {
        vector(
            "TableChart",
            "M10,10.02h5V21h-5V10.02z M17,21h3c1.1,0,2,-0.9,2,-2v-9h-5V21z M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h3V10.02H3V5h16V3z"
        )
    }

    val Upload by lazy {
        vector(
            "Upload",
            "M5,20h14v-2H5V20z M5,10h4v6h6v-6h4l-7,-7L5,10z"
        )
    }

    val DeleteForever by lazy {
        vector(
            "DeleteForever",
            "M6,19c0,1.1,0.9,2,2,2h8c1.1,0,2,-0.9,2,-2V7H6V19z M8.46,11.88l1.41,-1.41L12,12.59l2.12,-2.12l1.41,1.41L13.41,14l2.12,2.12l-1.41,1.41L12,15.41l-2.12,2.12l-1.41,-1.41L10.59,14L8.46,11.88z M15.5,4l-1,-1h-5l-1,1H5v2h14V4H15.5z"
        )
    }

    val Settings by lazy {
        vector(
            "Settings",
            "M19.14,12.94c0.04,-0.3,0.06,-0.61,0.06,-0.94c0,-0.32,-0.02,-0.64,-0.07,-0.94l2.03,-1.58c0.18,-0.14,0.23,-0.41,0.12,-0.61l-1.92,-3.32c-0.12,-0.22,-0.37,-0.29,-0.59,-0.22l-2.39,0.96c-0.5,-0.38,-1.03,-0.7,-1.62,-0.94L14.4,2.81c-0.04,-0.24,-0.24,-0.41,-0.48,-0.41h-3.84c-0.24,0,-0.43,0.17,-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22,-0.08,-0.47,0,-0.59,0.22L2.74,8.87c-0.12,0.21,-0.08,0.47,0.12,0.61l2.03,1.58c-0.05,0.3,-0.09,0.63,-0.09,0.94s0.02,0.64,0.07,0.94l-2.03,1.58c-0.18,0.14,-0.23,0.41,-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39,-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44,-0.17,0.47,-0.41l0.36,-2.54c0.59,-0.24,1.13,-0.56,1.62,-0.94l2.39,0.96c0.22,0.08,0.47,0,0.59,-0.22l1.92,-3.32c0.12,-0.22,0.07,-0.47,-0.12,-0.61l-2.01,-1.58z M12,15.6c-1.98,0,-3.6,-1.62,-3.6,-3.6s1.62,-3.6,3.6,-3.6s3.6,1.62,3.6,3.6s-1.62,3.6,-3.6,3.6z"
        )
    }

    val Notifications by lazy {
        vector(
            "Notifications",
            "M12,22c1.1,0,2,-0.9,2,-2h-4c0,1.1,0.9,2,2,2z M18,16v-5c0,-3.07,-1.63,-5.64,-4.5,-6.32V4c0,-0.83,-0.67,-1.5,-1.5,-1.5s-1.5,0.67,-1.5,1.5v0.68C7.64,5.36,6,7.92,6,11v5l-2,2v1h16v-1l-2,-2z"
        )
    }

    val ArrowBack by lazy {
        vector(
            "ArrowBack",
            "M20,11H7.83l5.59,-5.59L12,4l-8,8l8,8l1.41,-1.41L7.83,13H20v-2z"
        )
    }
}
