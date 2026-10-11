package com.example.ugadaifilm

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class Movie(
    val title: String,
    val clues: List<String>,
    val category: String
)

class MainActivity : AppCompatActivity() {

    private val movies = listOf(
        Movie("Титаник", listOf("🚢 Пассажирский корабль", "❤️ Влюблённая пара", "🧊 Огромный айсберг"), "Мелодрамы"),
        Movie("Матрица", listOf("💊 Две загадочные таблетки", "🕶️ Человек в чёрных очках", "💻 Цифровой мир"), "Фантастика"),
        Movie("Один дома", listOf("🏠 Большой дом", "👦 Мальчик один", "🪤 Ловушки для грабителей"), "Комедии"),
        Movie("Назад в будущее", listOf("🚗 Спортивная машина", "⚡ Молния", "⏰ Путешествие во времени"), "Фантастика"),
        Movie("Интерстеллар", listOf("🚀 Космический корабль", "🌌 Далёкий космос", "🕳️ Чёрная дыра"), "Фантастика"),
        Movie("Бриллиантовая рука", listOf("💎 Драгоценности", "💊 Загипсованная рука", "🏖️ Советский курорт"), "Комедии"),
        Movie("Иван Васильевич меняет профессию", listOf("👑 Царь", "⏰ Машина времени", "🏰 Средневековая Русь"), "Комедии"),
        Movie("Кавказская пленница", listOf("🏔️ Кавказские горы", "👩 Девушка", "💍 Похищение невесты"), "Комедии"),
        Movie("Зелёная миля", listOf("🚪 Тюремный коридор", "⚡ Электрический стул", "🟢 Зелёная дорожка"), "Драмы"),
        Movie("Челюсти", listOf("🌊 Океан", "🏖️ Пляж", "🦈 Огромная акула"), "Ужасы")
    )

    private var currentMovie: Movie? = null
    private var clueNumber = 0
    private var score = 0
    private var streak = 0
    private var round = 0
    private var timeLeft = 20
    private var classicMode = true
    private var failedAnswer = false
    private var timer: CountDownTimer? = null

    private lateinit var root: LinearLayout
    private lateinit var titleText: TextView
    private lateinit var roundText: TextView
    private lateinit var scoreText: TextView
    private lateinit var streakText: TextView
    private lateinit var timerText: TextView
    private lateinit var clueText: TextView
    private lateinit var answerInput: EditText
    private lateinit var checkButton: Button
    private lateinit var nextClueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMenu()
    }

    private fun showMenu() {
        timer?.cancel()

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(dp(24), dp(24), dp(24), dp(24))
        root.setBackgroundColor(Color.rgb(18, 18, 22))

        val title = makeText("🎬 УГАДАЙ ФИЛЬМ", 30)
        title.gravity = Gravity.CENTER

        val subtitle = makeText(
            "Угадай фильм по трём подсказкам",
            18
        )
        subtitle.gravity = Gravity.CENTER
        subtitle.setTextColor(Color.LTGRAY)

        val classic = makeButton("🎯 КЛАССИКА — 10 РАУНДОВ")
        val infinite = makeButton("♾️ БЕСКОНЕЧНЫЙ РЕЖИМ")
        val records = makeButton("🏆 РЕКОРДЫ")

        root.addView(title, matchParams())
        root.addView(subtitle, marginParams(0, 12, 0, 30))
        root.addView(classic, marginParams(0, 8, 0, 8))
        root.addView(infinite, marginParams(0, 8, 0, 8))
        root.addView(records, marginParams(0, 20, 0, 8))

        classic.setOnClickListener {
            classicMode = true
            startGame()
        }

        infinite.setOnClickListener {
            classicMode = false
            startGame()
        }

        records.setOnClickListener {
            showRecords()
        }

        setContentView(root)
    }

    private fun showRecords() {
        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val bestScore = prefs.getInt("best_score", 0)
        val bestStreak = prefs.getInt("best_streak", 0)
        val totalGuessed = prefs.getInt("total_guessed", 0)

        val message = """
            🏆 ЛУЧШИЙ СЧЁТ: $bestScore
            🔥 ЛУЧШАЯ СЕРИЯ: $bestStreak
            🎬 ВСЕГО УГАДАНО: $totalGuessed
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("🏆 РЕКОРДЫ")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun startGame() {
        score = 0
        streak = 0
        round = 0
        failedAnswer = false
        showGameScreen()
        nextRound()
    }

    private fun showGameScreen() {
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(18), dp(18), dp(18), dp(18))
        root.setBackgroundColor(Color.rgb(18, 18, 22))

        roundText = makeText("Раунд 1 / 10", 17)
        scoreText = makeText("🏆 Очки: 0", 17)
        streakText = makeText("🔥 Серия: 0", 17)
        timerText = makeText("⏱️ 20", 22)

        val top = LinearLayout(this)
        top.orientation = LinearLayout.HORIZONTAL

        top.addView(roundText, weightParams(1f))
        top.addView(scoreText, weightParams(1f))

        val secondTop = LinearLayout(this)
        secondTop.orientation = LinearLayout.HORIZONTAL
        secondTop.addView(streakText, weightParams(1f))
        secondTop.addView(timerText, weightParams(1f))

        clueText = makeText("Подсказка появится здесь", 24)
        clueText.gravity = Gravity.CENTER
        clueText.setPadding(dp(20), dp(35), dp(20), dp(35))
        clueText.setBackgroundColor(Color.rgb(30, 30, 38))

        answerInput = EditText(this)
        answerInput.hint = "Название фильма"
        answerInput.textSize = 18
        answerInput.setSingleLine(true)
        answerInput.setTextColor(Color.WHITE)
        answerInput.setHintTextColor(Color.GRAY)

        checkButton = makeButton("✅ ОТВЕТИТЬ")
        nextClueButton = makeButton("💡 ОТКРЫТЬ ПОДСКАЗКУ")

        root.addView(top)
        root.addView(secondTop, marginParams(0, 8, 0, 15))
        root.addView(clueText, marginParams(0, 5, 0, 15))
        root.addView(answerInput, marginParams(0, 5, 0, 12))
        root.addView(checkButton, marginParams(0, 5, 0, 8))
        root.addView(nextClueButton, marginParams(0, 5, 0, 8))

        setContentView(root)

        checkButton.setOnClickListener {
            checkAnswer()
        }

        nextClueButton.setOnClickListener {
            revealNextClue()
        }
    }

    private fun nextRound() {
        timer?.cancel()

        if (classicMode && round >= 10) {
            finishGame()
            return
        }

        round++
        currentMovie = movies.random()
        clueNumber = 0
        failedAnswer = false
        timeLeft = 20

        answerInput.text.clear()

        roundText.text =
            if (classicMode) "Раунд $round / 10"
            else "Раунд $round"

        scoreText.text = "🏆 Очки: $score"
        streakText.text = "🔥 Серия: $streak"

        showCurrentClue()
        startTimer()
    }

    private fun showCurrentClue() {
        val movie = currentMovie ?: return

        clueText.text = movie.clues[clueNumber]

        nextClueButton.text = when {
            clueNumber == 0 -> "💡 ОТКРЫТЬ 2-Ю ПОДСКАЗКУ"
            clueNumber == 1 -> "💡 ОТКРЫТЬ 3-Ю ПОДСКАЗКУ"
            failedAnswer -> "💔 СЕРИЯ ПОД УГРОЗОЙ"
            else -> "💡 ВСЕ ПОДСКАЗКИ ОТКРЫТЫ"
        }
    }

    private fun revealNextClue() {
        if (clueNumber >= 2) {
            Toast.makeText(this, "Все 3 подсказки уже открыты", Toast.LENGTH_SHORT).show()
            return
        }

        if (failedAnswer) {
            Toast.makeText(
                this,
                "💔 Подсказка открыта — серия спасена!",
                Toast.LENGTH_SHORT
            ).show()
        }

        clueNumber++
        showCurrentClue()
    }

    private fun checkAnswer() {
        val movie = currentMovie ?: return

        val answer = normalize(answerInput.text.toString())
        val correct = normalize(movie.title)

        if (answer.isEmpty()) {
            Toast.makeText(this, "Введите ответ", Toast.LENGTH_SHORT).show()
            return
        }

        if (answer == correct) {
            timer?.cancel()

            var points = when (clueNumber) {
                0 -> 300
                1 -> 200
                else -> 100
            }

            if (failedAnswer) {
                points /= 2
            }

            val multiplier = getMultiplier(streak + 1)
            val earned = (points * multiplier).toInt()

            score += earned
            streak++

            saveStats()

            val message = if (failedAnswer) {
                "💔 СЕРИЯ СПАСЕНА!\n\n+$earned очков"
            } else {
                "🎉 ПРАВИЛЬНО!\n\n+$earned очков"
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            updateBestScore()

            HandlerDelay(900) {
                nextRound()
            }

        } else {
            failedAnswer = true

            if (clueNumber < 2) {
                Toast.makeText(
                    this,
                    "❌ Не угадал! Можно открыть следующую подсказку и спасти серию.",
                    Toast.LENGTH_LONG
                ).show()

                nextClueButton.text = "💔 СПАСТИ СЕРИЮ — ОТКРЫТЬ ПОДСКАЗКУ"
            } else {
                timer?.cancel()
                streak = 0
                streakText.text = "🔥 Серия: 0"

                Toast.makeText(
                    this,
                    "❌ Неверно! Ответ: ${movie.title}",
                    Toast.LENGTH_LONG
                ).show()

                HandlerDelay(1200) {
                    nextRound()
                }
            }
        }
    }

    private fun startTimer() {
        timer?.cancel()

        timer = object : CountDownTimer(20_000, 1_000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt() + 1
                timerText.text = "⏱️ $timeLeft"
            }

            override fun onFinish() {
                timeLeft = 0
                timerText.text = "⏱️ 0"

                streak = 0
                streakText.text = "🔥 Серия: 0"

                val movie = currentMovie

                Toast.makeText(
                    this@MainActivity,
                    "⏰ Время вышло!\nОтвет: ${movie?.title}",
                    Toast.LENGTH_LONG
                ).show()

                HandlerDelay(1200) {
                    nextRound()
                }
            }
        }.start()
    }

    private fun getMultiplier(nextStreak: Int): Double {
        return when {
            nextStreak >= 20 -> 5.0
            nextStreak >= 10 -> 3.0
            nextStreak >= 5 -> 2.0
            nextStreak >= 3 -> 1.5
            else -> 1.0
        }
    }

    private fun normalize(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("\\s+"), " ")
    }

    private fun saveStats() {
        val prefs = getSharedPreferences("records", MODE_PRIVATE)

        val oldBestStreak = prefs.getInt("best_streak", 0)
        val totalGuessed = prefs.getInt("total_guessed", 0)

        prefs.edit()
            .putInt("best_streak", maxOf(oldBestStreak, streak))
            .putInt("total_guessed", totalGuessed + 1)
            .apply()
    }

    private fun updateBestScore() {
        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val oldBest = prefs.getInt("best_score", 0)

        if (score > oldBest) {
            prefs.edit()
                .putInt("best_score", score)
                .apply()
        }
    }

    private fun finishGame() {
        timer?.cancel()
        updateBestScore()

        AlertDialog.Builder(this)
            .setTitle("🏁 ИГРА ОКОНЧЕНА")
            .setMessage(
                "Твой результат:\n\n" +
                        "🏆 Очки: $score\n" +
                        "🔥 Серия: $streak"
            )
            .setPositiveButton("ИГРАТЬ ЕЩЁ") { _, _ ->
                startGame()
            }
            .setNegativeButton("МЕНЮ") { _, _ ->
                showMenu()
            }
            .setCancelable(false)
            .show()
    }

    private fun makeText(text: String, size: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun makeButton(text: String): Button {
        return Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
        }
    }

    private fun matchParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun marginParams(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(left), dp(top), dp(right), dp(bottom))
        }
    }

    private fun weightParams(weight: Float): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            weight
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun HandlerDelay(delay: Long, action: () -> Unit) {
        android.os.Handler(mainLooper).postDelayed(action, delay)
    }
}
