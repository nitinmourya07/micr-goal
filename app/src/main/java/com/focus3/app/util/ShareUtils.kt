package com.focus3.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.focus3.app.data.model.DailyTask
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    
    fun shareCompletedTasks(
        context: Context,
        tasks: List<DailyTask>,
        date: String,
        streak: Int
    ) {
        val bitmap = createShareImage(tasks, date, streak)
        val file = saveBitmapToCache(context, bitmap)
        shareImage(context, file)
    }
    
    private fun createShareImage(
        tasks: List<DailyTask>,
        date: String,
        streak: Int
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // ===============================================
        // PREMIUM GRADIENT BACKGROUND
        // ===============================================
        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor("#060810"),
                    Color.parseColor("#0E1117"),
                    Color.parseColor("#151A22"),
                    Color.parseColor("#0E1117"),
                    Color.parseColor("#060810")
                ),
                floatArrayOf(0f, 0.2f, 0.5f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)
        
        // ===============================================
        // SUBTLE GLOW CIRCLES (atmospheric)
        // ===============================================
        val glowPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#00FFD1")
            alpha = 12
        }
        canvas.drawCircle(200f, 350f, 200f, glowPaint)
        canvas.drawCircle(880f, 650f, 150f, glowPaint.apply { alpha = 8 })
        
        // ===============================================
        // TOP ACCENT LINE
        // ===============================================
        val accentLinePaint = Paint().apply {
            shader = LinearGradient(
                200f, 0f, 880f, 0f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#00FFD1"),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawLine(200f, 100f, 880f, 100f, accentLinePaint)
        
        // ===============================================
        // APP NAME — Premium Branding
        // ===============================================
        val brandPaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }
        canvas.drawText("FOCUS3", width / 2f, 180f, brandPaint)
        
        val taglinePaint = Paint().apply {
            color = Color.parseColor("#00FFD1")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.3f
        }
        canvas.drawText("DAILY GOAL COMPANION", width / 2f, 220f, taglinePaint)
        
        // ===============================================
        // DATE PILL
        // ===============================================
        val pillPaint = Paint().apply {
            color = Color.parseColor("#1A1F2A")
            isAntiAlias = true
        }
        val pillBorderPaint = Paint().apply {
            color = Color.parseColor("#00FFD1")
            alpha = 60
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val datePillRect = RectF(340f, 260f, 740f, 310f)
        canvas.drawRoundRect(datePillRect, 25f, 25f, pillPaint)
        canvas.drawRoundRect(datePillRect, 25f, 25f, pillBorderPaint)
        
        val datePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 26f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.1f
        }
        canvas.drawText(date.uppercase(), width / 2f, 293f, datePaint)
        
        // ===============================================
        // STREAK BADGE (if active)
        // ===============================================
        var yOffset = 380f
        if (streak > 0) {
            val badgePaint = Paint().apply {
                isAntiAlias = true
            }
            badgePaint.shader = LinearGradient(
                300f, yOffset - 10f, 780f, yOffset + 55f,
                intArrayOf(
                    Color.parseColor("#FF5C00"),
                    Color.parseColor("#FFB800")
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            badgePaint.alpha = 35
            val streakRect = RectF(300f, yOffset - 10f, 780f, yOffset + 55f)
            canvas.drawRoundRect(streakRect, 32f, 32f, badgePaint)
            
            val streakBorderPaint = Paint().apply {
                shader = LinearGradient(
                    300f, yOffset - 10f, 780f, yOffset + 55f,
                    intArrayOf(
                        Color.parseColor("#FF5C00"),
                        Color.parseColor("#FFB800")
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
                alpha = 120
            }
            canvas.drawRoundRect(streakRect, 32f, 32f, streakBorderPaint)
            
            val streakTextPaint = Paint().apply {
                color = Color.parseColor("#FFB800")
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("🔥 $streak day${if (streak > 1) "s" else ""} streak!", width / 2f, yOffset + 35f, streakTextPaint)
            
            yOffset += 100f
        }
        
        // ===============================================
        // SECTION HEADER — "COMPLETED TODAY"
        // ===============================================
        val sectionLabelPaint = Paint().apply {
            color = Color.parseColor("#00FFD1")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.2f
        }
        canvas.drawText("⚡ TODAY'S MISSION", 100f, yOffset, sectionLabelPaint)
        
        val sectionTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("ALL GOALS CRUSHED", 100f, yOffset + 48f, sectionTitlePaint)
        
        yOffset += 90f
        
        // ===============================================
        // PREMIUM TASK CARDS
        // ===============================================
        tasks.forEachIndexed { index, task ->
            // Card background with glass effect
            val cardRect = RectF(80f, yOffset, width - 80f, yOffset + 130f)
            val cardBgPaint = Paint().apply {
                color = Color.parseColor("#12161E")
                isAntiAlias = true
            }
            canvas.drawRoundRect(cardRect, 20f, 20f, cardBgPaint)
            
            // Card border — gradient from teal to transparent
            val cardBorderPaint = Paint().apply {
                shader = LinearGradient(
                    cardRect.left, cardRect.top, cardRect.left, cardRect.bottom,
                    intArrayOf(
                        Color.parseColor("#00FFD1"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
                alpha = 80
            }
            canvas.drawRoundRect(cardRect, 20f, 20f, cardBorderPaint)
            
            // Left accent stripe
            val stripePaint = Paint().apply {
                shader = LinearGradient(
                    80f, yOffset, 80f, yOffset + 130f,
                    intArrayOf(
                        Color.parseColor("#00FFD1"),
                        Color.parseColor("#00B393")
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(80f, yOffset, 88f, yOffset + 130f), 4f, 4f, stripePaint)
            
            // Goal number circle
            val circlePaint = Paint().apply {
                shader = LinearGradient(
                    120f, yOffset + 35f, 170f, yOffset + 95f,
                    intArrayOf(
                        Color.parseColor("#00FFD1"),
                        Color.parseColor("#00B393")
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                isAntiAlias = true
            }
            canvas.drawCircle(150f, yOffset + 65f, 25f, circlePaint)
            
            val numPaint = Paint().apply {
                color = Color.BLACK
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("${index + 1}", 150f, yOffset + 75f, numPaint)
            
            // Check icon
            val checkPaint = Paint().apply {
                color = Color.parseColor("#00E676")
                textSize = 32f
                isAntiAlias = true
            }
            canvas.drawText("✓", width - 150f, yOffset + 75f, checkPaint)
            
            // Task text
            val taskTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 34f
                isAntiAlias = true
                alpha = 230
            }
            val displayText = if (task.content.length > 28) 
                task.content.take(25) + "..." 
            else 
                task.content
            canvas.drawText(displayText, 200f, yOffset + 75f, taskTextPaint)
            
            yOffset += 160f
        }
        
        // ===============================================
        // BOTTOM BRANDING FOOTER
        // ===============================================
        // Footer accent line
        val footerLinePaint = Paint().apply {
            shader = LinearGradient(
                300f, 0f, 780f, 0f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#00FFD1"),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            strokeWidth = 1.5f
            isAntiAlias = true
            alpha = 100
        }
        canvas.drawLine(300f, height - 180f, 780f, height - 180f, footerLinePaint)
        
        val footerPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }
        canvas.drawText("FOCUS3 — DAILY GOAL COMPANION", width / 2f, height - 130f, footerPaint)
        
        val footerSubPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 20f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("3 goals · every day · no excuses", width / 2f, height - 90f, footerSubPaint)
        
        return bitmap
    }
    
    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "focus3_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
    
    private fun shareImage(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share your achievements"))
    }
}
