package com.example.activetrack

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// Gráfico de barras simples, feito apenas com Canvas (sem biblioteca externa).
// Mostra a quantidade de passos dos últimos 7 dias.

class GraficoSemanalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dados: List<HistoricoPassos> = emptyList()

    private val paintBarra = Paint().apply {
        color = Color.parseColor("#2F6FED")
        isAntiAlias = true
    }

    private val paintTexto = Paint().apply {
        color = Color.parseColor("#7A7F87")
        textSize = 26f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    // Chamado pela Activity sempre que os dados do histórico mudam
    fun definirDados(lista: List<HistoricoPassos>) {

        // Invertemos para mostrar do dia mais antigo (esquerda) ao mais recente (direita)
        dados = lista.reversed()

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dados.isEmpty()) {
            return
        }

        val larguraTotal = width.toFloat()
        val alturaTotal = height.toFloat()

        val maiorValor = dados.maxOf { it.passos }.coerceAtLeast(1)

        val larguraBarra = larguraTotal / (dados.size * 2f)
        val espacamento = larguraBarra

        var x = espacamento / 2

        for (item in dados) {

            val alturaBarra =
                (item.passos.toFloat() / maiorValor) * (alturaTotal - 60f)

            canvas.drawRect(
                x,
                alturaTotal - alturaBarra - 30f,
                x + larguraBarra,
                alturaTotal - 30f,
                paintBarra
            )

            // Mostra só o dia (últimos 2 caracteres de "yyyy-MM-dd")
            val diaAbreviado = item.data.takeLast(2)

            canvas.drawText(
                diaAbreviado,
                x + larguraBarra / 2,
                alturaTotal,
                paintTexto
            )

            x += larguraBarra + espacamento
        }
    }
}