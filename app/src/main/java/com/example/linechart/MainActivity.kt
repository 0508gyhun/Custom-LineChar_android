package com.example.linechart

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linechart.ui.theme.LinechartTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinechartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sampleData = listOf(
                        Pair(1, 10.0),
                        Pair(2, 15.0),
                        Pair(3, 7.0),
                        Pair(4, 20.0),
                        Pair(5, 12.0),
                        Pair(6, 25.0),
                        Pair(7, 18.0),
                        Pair(8, 10.0),
                        Pair(9, 15.0),
                        Pair(10, 7.0),
                        Pair(11, 20.0),
                        Pair(12, 12.0)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LineChart(data = sampleData, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart( //커스텀 제외
    modifier: Modifier = Modifier,
    data: List<Pair<Int, Double>> = emptyList()
) {
    // 차트 레이아웃 및 색상 정의
    val spacing = 50f
    val graphColor = Color.Cyan
    val transparentGraphColor = remember { graphColor.copy(alpha = 0.5f) }

    val pointColor = Color.Gray // 데이터 포인트 원의 색상

    val density = LocalDensity.current
    val pointRadius = with(density) { 4.dp.toPx() } // 여기서 toPx()를 호출합니다.

    // Y축 범위 계산
    val upperValue = remember { (data.maxOfOrNull { it.second }?.plus(1))?.roundToInt() ?: 0 }
    val lowerValue = remember { (data.minOfOrNull { it.second }?.toInt() ?: 0) }


    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = density.run { 11.sp.toPx() }
        }
    }
    val dataValueTextPaint = remember(density) { // 데이터 값 표시용
        Paint().apply {
            color = android.graphics.Color.GRAY
            textAlign = Paint.Align.CENTER
            textSize = density.run { 10.sp.toPx() } // 데이터 값은 약간 작게
        }
    }

    Canvas(modifier = modifier) {
        val spacePerHour = (size.width - spacing) / data.size

        // X축 라벨 그리기
        (data.indices step 1).forEach { i ->
            val hour = data[i].first
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    hour.toString(),
                    spacing + i * spacePerHour,
                    size.height,
                    textPaint
                )
            }
        }
        // Y축 라벨 그리기
        (lowerValue..upperValue).forEach { value ->
            val ratio = (value - lowerValue).toFloat() / (upperValue - lowerValue).toFloat()
            val yPos = (size.height - spacing) - (ratio * (size.height - spacing))

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    value.toString(),
                    spacing - 10.dp.toPx(),
                    yPos + textPaint.textSize / 2,
                    textPaint
                )
            }
        }
        // 그리드 라인 설정
        val gridLineColor = Color.LightGray.copy(alpha = 0.5f)
        val gridStrokeWidth = 1.dp.toPx()

        // 가로 그리드 선 그리기
        (lowerValue..upperValue).forEach { value ->
            val ratio = (value - lowerValue).toFloat() / (upperValue - lowerValue).toFloat()
            val yPos = size.height - spacing - (ratio * (size.height - spacing))

            drawLine(
                color = gridLineColor,
                start = Offset(spacing, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = gridStrokeWidth
            )
        }
        // 세로 그리드 선 그리기
        (data.indices step 1).forEach { i ->
            val xPos = spacing + i * spacePerHour
            drawLine(
                color = gridLineColor,
                start = Offset(xPos, 0f),
                end = Offset(xPos, size.height - spacing),
                strokeWidth = gridStrokeWidth
            )
        }

        // 그래프 선 Path 생성
        val strokePath = Path().apply {
            val height = size.height
            data.indices.forEach { i ->
                val info = data[i]
                val ratio = (info.second - lowerValue) / (upperValue - lowerValue)
                val x1 = spacing + i * spacePerHour
                val y1 = height - spacing - (ratio * (size.height - spacing)).toFloat()

                if (i == 0) {
                    moveTo(x1, y1)
                }
                lineTo(x1, y1)
            }
        }
        // 그래프 선 그리기
        drawPath(
            path = strokePath,
            color = graphColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Butt
            )
        )

        // 그래프 아래 채우기 Path 생성 및 그리기
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(size.width - spacePerHour, size.height - spacing)
            lineTo(spacing, size.height - spacing)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    transparentGraphColor,
                    Color.Transparent
                ),
                endY = size.height - spacing
            )
        )

        data.indices.forEach { i ->
            val info = data[i]
            val ratio = (info.second - lowerValue) / (upperValue - lowerValue)
            val xPos = spacing + i * spacePerHour
            val yPos = size.height - spacing - (ratio * (size.height - spacing)).toFloat()

            // 데이터 포인트 원 그리기
            drawCircle(
                color = pointColor,
                center = Offset(xPos, yPos),
                radius = pointRadius
            )
            //데이터 값 텍스트 그리기
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    info.second.roundToInt().toString(), // 값을 정수로 반올림하여 표시
                    xPos,
                    yPos - pointRadius - 5.dp.toPx(), // 원 위쪽에 표시
                    dataValueTextPaint
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LineChartPreview() {

}
