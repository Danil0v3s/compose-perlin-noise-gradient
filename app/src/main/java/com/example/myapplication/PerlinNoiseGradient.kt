package br.com.firstsoft.feature.main.ui

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator
import de.articdive.jnoise.pipeline.JNoise
import org.intellij.lang.annotations.Language
import kotlin.math.sin

private fun pulse(t: Float, f: Float): Float {
    val result = 0.5f * (1f + sin(2f * Math.PI.toFloat() * f * t))
    return result
}

@Composable
fun PerlinNoiseGradient(modifier: Modifier = Modifier) {

    val height = 500
    val width = 500

    val infiniteTransition = rememberInfiniteTransition("transition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
    val noise = JNoise.newBuilder()
        .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(1077).build())
        .scale(1 / 24.0)
        .addModifier { v -> (v / 2) * 5.0 }
        .clamp(0.0, 1.0)
        .build()
    for (y in 0 until height) {
        for (x in 0 until width) {
            val alpha = noise.evaluateNoise(x.toDouble(), y.toDouble()).toFloat()
            bitmap.setPixel(
                x,
                y,
                Color.Blue.copy(alpha = alpha).toArgb()
            )
        }
    }

    @Language("AGSL")
    val noiseShader = """
    uniform vec2 iResolution;
    uniform float iTime;
    
    float hash(vec2 p)  // replace this by something better
    {
        p  = 50.0*fract( p*0.3183099 + vec2(0.71,0.113));
        return -1.0+2.0*fract( p.x*p.y*(p.x+p.y) );
    }
    
    float noise( in vec2 p )
    {
        vec2 i = vec2(floor(p));
        vec2 f = fract( p );
        vec2 u = f*f*(3.0-2.0*f);
        return mix( mix( hash( i + vec2(0,0) ), hash( i + vec2(1,0) ), u.x), mix( hash( i + vec2(0,1) ), hash( i + vec2(1,1) ), u.x), u.y);
    }
    
    vec4 main(vec2 fragCoord) {
        vec2 p = fragCoord.xy / iResolution.xy;
        vec2 uv = p * vec2(iResolution.x/iResolution.y,1.0) + iTime*0.25;
        float f = 0.0;
        uv *= 8.0;
        mat2 m = mat2( 1.6,  1.2, -1.2,  1.6 );
        f  = 0.5000*noise( uv ); uv = m*uv;
        f += 0.2500*noise( uv ); uv = m*uv;
        f += 0.1250*noise( uv ); uv = m*uv;
        f += 0.0625*noise( uv ); uv = m*uv;
    
        f = 0.5 + 0.5*f;
        f *= smoothstep( 0.0, 0.005, abs(p.x));
        return vec4(f, f, f, 1.0 );
    }
    """.trimIndent()

    val nativePaint = remember {
        Paint().asFrameworkPaint().apply {
//            this.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
//            style = android.graphics.Paint.Style.STROKE
//            strokeWidth = 12f
//            blendMode = BlendMode.DST_IN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawIntoCanvas { canvas: Canvas ->
                    canvas.nativeCanvas.apply {
                        val y = pulse(scale, 5f) * 50f
                        val x = pulse(scale, 1.38f) * 50f
                        scale(50f, 50f)
                        translate(-x, -y)
                        drawBitmap(
                            bitmap,
                            0f,
                            0f,
                            nativePaint
                        )
                    }
                }
            }
        )
//        Box(modifier = Modifier
//            .fillMaxSize()
//            .drawWithContent {
//                drawIntoCanvas { canvas ->
//                    canvas.nativeCanvas.drawRect(
//                        0f,
//                        0f,
//                        500f,
//                        500f,
//                        Paint()
//                            .asFrameworkPaint()
//                            .apply {
//                                this.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
//                                style = android.graphics.Paint.Style.STROKE
//                                strokeWidth = 12f
//                            })
//                }
//            })
    }
}

@Preview(widthDp = 375, heightDp = 700)
@Composable
private fun PerlinNoiseGradientPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PerlinNoiseGradient()
    }
}