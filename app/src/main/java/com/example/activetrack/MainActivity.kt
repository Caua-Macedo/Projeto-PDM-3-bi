package com.example.activetrack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Button
import android.widget.TextView

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity :
    AppCompatActivity(),
    SensorEventListener {

    lateinit var sensorManager: SensorManager

    var sensorPassos: Sensor? = null
    var acelerometro: Sensor? = null

    lateinit var txtPassos: TextView
    lateinit var txtCadencia: TextView
    lateinit var txtDistancia: TextView
    lateinit var txtCalorias: TextView
    lateinit var txtSensor: TextView
    lateinit var txtEstatisticas: TextView

    lateinit var btnTreino: Button
    lateinit var btnEstatisticas: Button

    lateinit var graficoSemanal: GraficoSemanalView


    var valorInicial = -1f
    var passosTotais = 0

    var treinoAtivo = false

    var passosInicioTreino = 0
    var tempoInicioTreino = 0L

    var cadenciaAtual = 0
    var distanciaAtual = 0.0
    var caloriasAtuais = 0

    val TAMANHO_PASSADA = 0.75
    val KCAL_POR_PASSO = 0.04

    var ultimoX = 0f
    var ultimoY = 0f
    var ultimoZ = 0f

    var primeiraLeitura = true

    var estatisticasVisiveis = false


    // Banco de dados Room (persistência local)
    lateinit var db: AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        // Encontrando os componentes

        txtPassos = findViewById(R.id.txtPassos)
        txtCadencia = findViewById(R.id.txtRitmo)
        txtDistancia = findViewById(R.id.txtDistancia)
        txtCalorias = findViewById(R.id.txtCalorias)
        txtSensor = findViewById(R.id.txtStatus)
        txtEstatisticas = findViewById(R.id.txtEstatisticas)

        btnTreino = findViewById(R.id.btnTreino)
        btnEstatisticas = findViewById(R.id.btnEstatisticas)

        graficoSemanal = findViewById(R.id.graficoSemanal)


        // Inicializando o banco de dados Room

        db = AppDatabase.getInstance(this)


        // 3. Update your onCreate
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Only register if we already have permission
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasPermission) {
            setupStepSensor()
        } else {
            solicitarPermissaoAtividade()
        }

        // Keep accelerometer as it doesn't require ACTIVITY_RECOGNITION
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }


        // Botão iniciar/encerrar treino

        btnTreino.setOnClickListener {

            treinoAtivo = !treinoAtivo

            if (treinoAtivo) {

                iniciarTreino()
                btnTreino.text = "ENCERRAR TREINO"

            } else {

                btnTreino.text = "INICIAR TREINO"
            }
        }


        // Botão estatísticas — liga/desliga a exibição

        btnEstatisticas.setOnClickListener {

            estatisticasVisiveis = !estatisticasVisiveis

            if (estatisticasVisiveis) {

                mostrarEstatisticas()
                btnEstatisticas.text = "OCULTAR ESTATÍSTICAS"

            } else {

                txtEstatisticas.text = ""
                btnEstatisticas.text = "ESTATÍSTICAS"
            }
        }


        // Carrega o gráfico assim que o app abre (com o que já existe salvo)

        carregarGraficoSemanal()
    }


    // ===================== PERMISSÃO =====================

    fun solicitarPermissaoAtividade() {

        // A permissão ACTIVITY_RECOGNITION só existe a partir do Android 10 (API 29)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val jaTemPermissao =
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED

            if (!jaTemPermissao) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    1
                )
            }
        }
    }

    // 2. Handle the result of the permission dialog
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted! Start the sensor now.
            setupStepSensor()
        }
    }

    // 1. Move registration logic to a function
    private fun setupStepSensor() {
        sensorPassos = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorPassos?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            txtSensor.text = "Sensor: Não encontrado no hardware"
        }
    }


    fun iniciarTreino() {

        passosInicioTreino = passosTotais
        tempoInicioTreino = SystemClock.elapsedRealtime()

        cadenciaAtual = 0
        distanciaAtual = 0.0
        caloriasAtuais = 0

        atualizarTextos()
    }


    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {

            Sensor.TYPE_STEP_COUNTER -> {
                processarPassos(event)
            }

            Sensor.TYPE_ACCELEROMETER -> {
                processarAcelerometro(event)
            }
        }
    }


    fun processarPassos(event: SensorEvent) {

        val valorSensor = event.values[0]

        if (valorInicial < 0) {
            valorInicial = valorSensor
        }

        passosTotais = (valorSensor - valorInicial).toInt()

        if (treinoAtivo) {

            calcularCadencia()
            calcularDistancia()
            calcularCalorias()
        }

        atualizarTextos()

        if (estatisticasVisiveis) {
            mostrarEstatisticas()
        }

        // Salva (ou atualiza) o registro do dia de hoje no Room
        salvarHistoricoHoje()
    }


    fun calcularCadencia() {

        val passosTreino = passosTotais - passosInicioTreino
        val tempoDecorrido = SystemClock.elapsedRealtime() - tempoInicioTreino
        val minutos = tempoDecorrido / 60000.0

        cadenciaAtual = if (minutos > 0) {
            (passosTreino / minutos).toInt()
        } else {
            0
        }
    }


    fun calcularDistancia() {

        val passosTreino = passosTotais - passosInicioTreino
        val metros = passosTreino * TAMANHO_PASSADA

        distanciaAtual = metros / 1000
    }


    fun calcularCalorias() {

        val passosTreino = passosTotais - passosInicioTreino

        caloriasAtuais = (passosTreino * KCAL_POR_PASSO).toInt()
    }


    fun processarAcelerometro(event: SensorEvent) {

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (primeiraLeitura) {

            ultimoX = x
            ultimoY = y
            ultimoZ = z

            primeiraLeitura = false

            return
        }

        val diferenca =
            Math.abs(x - ultimoX) +
                    Math.abs(y - ultimoY) +
                    Math.abs(z - ultimoZ)

        if (diferenca > 1.5f) {
            txtSensor.text = "Sensor: movimento detectado"
        } else {
            txtSensor.text = "Sensor: aguardando..."
        }

        ultimoX = x
        ultimoY = y
        ultimoZ = z
    }


    fun atualizarTextos() {

        txtPassos.text = passosTotais.toString() + " passos"
        txtCadencia.text = "Cadência: " + cadenciaAtual + " SPM"
        txtDistancia.text = "Distância: %.2f km".format(distanciaAtual)
        txtCalorias.text = "Calorias: " + caloriasAtuais + " kcal"
    }


    fun mostrarEstatisticas() {

        val passosTreino = passosTotais - passosInicioTreino

        txtEstatisticas.text =
            "Estatísticas\n" +
                    "Passos: $passosTreino\n" +
                    "Cadência: $cadenciaAtual SPM\n" +
                    "Distância: %.2f km\n".format(distanciaAtual) +
                    "Calorias: $caloriasAtuais kcal"
    }


    // ===================== ROOM (persistência) =====================

    fun obterDataAtual(): String {

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return formato.format(Date())
    }


    fun salvarHistoricoHoje() {

        val registro = HistoricoPassos(
            data = obterDataAtual(),
            passos = passosTotais,
            distancia = distanciaAtual,
            calorias = caloriasAtuais
        )

        // Operações do Room precisam rodar fora da thread principal
        lifecycleScope.launch(Dispatchers.IO) {

            db.historicoDao().salvar(registro)

            carregarGraficoSemanal()
        }
    }


    // ===================== GRÁFICO SEMANAL =====================

    fun carregarGraficoSemanal() {

        lifecycleScope.launch(Dispatchers.IO) {

            val historico = db.historicoDao().ultimosSeteDias()

            withContext(Dispatchers.Main) {

                graficoSemanal.definirDados(historico)
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    override fun onDestroy() {

        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}