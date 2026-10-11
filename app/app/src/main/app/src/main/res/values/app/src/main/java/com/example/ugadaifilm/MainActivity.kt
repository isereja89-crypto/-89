package com.example.ugadaifilm

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
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
        Movie("Назад в будущее", listOf("🚗 Спортивная машина", "⚡ Молния", "⏱️ Путешествие во времени"), "Фантастика"),
        Movie("Интерстеллар", listOf("🚀 Космический корабль", "🌌 Далёкий космос", "🕳️ Чёрная дыра"), "Фантастика"),
        Movie("Бриллиантовая рука", listOf("💎 Драгоценности", "💊 Загипсованная рука", "🏖️ Советский курорт"), "Комедии"),
        Movie("Иван Васильевич меняет профессию", listOf("👑 Царь", "🚗 Машина времени", "🏰 Средневековая Русь"), "Комедии"),
        Movie("Кавказская пленница", listOf("🏔️ Кавказские горы", "👩 Девушка", "💍 Похищение невесты"), "Комедии"),
        Movie("Зелёная миля", listOf("🚪 Тюремный коридор", "⚡ Электрический стул", "🟢 Зелёная дорожка"), "Драмы"),
        Movie("Челюсти", listOf("🌊 Океан", "🏖️ Пляж", "🦈 Огромная акула"), "Ужасы")
    )

    private val categories = listOf(
        "Все фильмы", "Советские фильмы", "Комедии", "Ужасы",
        "Мелодрамы", "Боевики", "Фантастика", "Детективы",
        "Фэнтези", "Драмы", "Классика"
    )

    private var currentMovie: Movie? = null
    private var clueNumber = 0
    private var score = 0
    private var streak = 0
    private var round = 0
    private var timeLeft = 20
    private var classicMode = true
    private var selectedCategory = "Все фильмы"
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

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(18, 18, 22))
        }

        titleText = makeText("🎬 УГАДАЙ ФИЛЬМ", 30)
        titleText.gravity = Gravity.CENTER
        root.addView(titleText, matchWrap())

        val subtitle = makeText("Угадай фильм по трём подсказкам", 18)
        subtitle.gravity = Gravity.CENTER
        root.addView(subtitle, matchWrap())

        val modeButton = Button(this).apply {
            text = "🎯 КЛАССИКА — 10 РАУНДОВ"
            setOnClickListener {
                classicMode = true
                startGame()
            }
        }
        root.addView(modeButton, buttonParams())

        val infiniteButton = Button(this).apply {
            text = "♾ БЕСКОНЕЧНЫЙ РЕЖИМ"
            setOnClickListener {
                classicMode = false
                startGame()
            }
        }
        root.addView(infiniteButton, buttonParams())

        val categorySpinner = Spinner(this)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        root.addView(categorySpinner, matchWrap())

        categorySpinner.setSelection(categories.indexOf(selectedCategory))
        categorySpinner.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategory = categories[position]
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        )

        val info = makeText(
            "\n300 очков — первая подсказка\n200 очков — вторая\n100 очков — третья\n\n🔥 Серия: 3×1.5 • 5×2 • 10×3 • 20×5",
            16
        )
        info.gravity = Gravity.CENTER
        root.addView(info, matchWrap())

        setContentView(root)
    }

    private fun startGame() {
        score = 0
        streak = 0
        round = 0

        buildGameScreen()
        startRound()
    }

    private fun buildGameScreen() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(Color.rgb(18, 18, 22))
        }

        titleText = makeText("🎬 УГАДАЙ ФИЛЬМ", 26)
        titleText.gravity = Gravity.CENTER
        root.addView(titleText, matchWrap())

        roundText = makeText("Раунд 0", 17)
        scoreText = makeText("🏆 Очки: 0", 17)
        streakText = makeText("🔥 Серия: 0", 17)
        timerText = makeText("⏱ 20", 22)

        root.addView(roundText, matchWrap())
        root.addView(timerText, matchWrap())
        root.addView(scoreText, matchWrap())
        root.addView(streakText, matchWrap())

        clueText = makeText("Подсказка появится здесь", 26)
        clueText.gravity = Gravity.CENTER
        root.addView(
            clueText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        answerInput = EditText(this).apply {
            hint = "Название фильма"
            setSingleLine(true)
            setTextColor(Color.WHITE)
        }
        root.addView(answerInput, matchWrap())

        checkButton = Button(this).apply {
            text = "✅ ПРОВЕРИТЬ"
            setOnClickListener { checkAnswer() }
        }
        root.addView(checkButton, buttonParams())

        nextClueButton = Button(this).apply {
            text = "💡 Открыть подсказку 2"
            setOnClickListener { showNextClue() }
        }
        root.addView(nextClueButton, buttonParams())

        val menuButton = Button(this).apply {
            text = "🏠 В МЕНЮ"
            setOnClickListener { showMenu() }
        }
        root.addView(menuButton, buttonParams())

        setContentView(root)
    }

    private fun startRound() {
        if (classicMode && round >= 10) {
            showGameOver()
            return
        }

        val available = if (selectedCategory == "Все фильмы") {
            movies
        } else {
            movies.filter { it.category == selectedCategory }
        }

        val pool = if (available.isNotEmpty()) available else movies

        currentMovie = pool.random()
        clueNumber = 0
        round++
        timeLeft = 20

        roundText.text = if (classicMode) {
            "Раунд $round / 10"
        } else {
            "Раунд $round"
        }

        scoreText.text = "🏆 Очки: $score"
        streakText.text = "🔥 Серия: $streak"
        answerInput.setText("")
        answerInput.isEnabled = true
        checkButton.isEnabled = true
        nextClueButton.isEnabled = true

        showClue()
        startTimer()
    }

    private fun showClue() {
        val movie = currentMovie ?: return
        clueText.text = movie.clues[clueNumber]

        nextClueButton.text = when (clueNumber) {
            0 -> "💡 Открыть подсказку 2"
            1 -> "💡 Открыть подсказку 3"
            else -> "💡 Больше подсказок нет"
        }
        nextClueButton.isEnabled = clueNumber < 2
    }

    private fun showNextClue() {
        if (clueNumber >= 2) {
            Toast.makeText(this, "Это последняя подсказка!", Toast.LENGTH_SHORT).show()
            return
        }

        clueNumber++
        showClue()

        val points = when (clueNumber) {
            1 -> 200
            else -> 100
        }

        Toast.makeText(
            this,
            "Теперь за правильный ответ: $points очков",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkAnswer() {
        val movie = currentMovie ?: return
        val answer = normalize(answerInput.text.toString())
        val correct = normalize(movie.title)

        if (answer.isEmpty()) {
            Toast.makeText(this, "Напиши название фильма", Toast.LENGTH_SHORT).show()
            return
        }

        if (answer == correct) {
            timer?.cancel()

            val basePoints = when (clueNumber) {
                0 -> 300
                1 -> 200
                else -> 100
            }

            val multiplier = when {
                streak >= 20 -> 5.0
                streak >= 10 -> 3.0
                streak >= 5 -> 2.0
                streak >= 3 -> 1.5
                else -> 1.0
            }

            val earned = (basePoints * multiplier).toInt()
            score += earned
            streak++

            scoreText.text = "🏆 Очки: $score"
            streakText.text = "🔥 Серия: $streak"

            Toast.makeText(
                this,
                "🎉 Верно! +$earned очков",
                Toast.LENGTH_SHORT
            ).show()

            answerInput.isEnabled = false
            checkButton.isEnabled = false
            nextClueButton.isEnabled = false

            android.os.Handler(mainLooper).postDelayed({
                startRound()
            }, 1200)
        } else {
            Toast.makeText(
                this,
                "❌ Неверно. Можно открыть следующую подсказку.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startTimer() {
        timer?.cancel()

        timer = object : CountDownTimer(20_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000L).toInt()
                timerText.text = "⏱ $timeLeft"
            }

            override fun onFinish() {
                timeLeft = 0
                timerText.text = "⏱ 0"
                streak = 0
                streakText.text = "🔥 Серия: 0"

                val movie = currentMovie
                Toast.makeText(
                    this@MainActivity,
                    "⏰ Время вышло! Ответ: ${movie?.title}",
                    Toast.LENGTH_LONG
                ).show()

                answerInput.isEnabled = false
                checkButton.isEnabled = false
                nextClueButton.isEnabled = false

                android.os.Handler(mainLooper).postDelayed({
                    startRound()
                }, 1200)
            }
        }.start()
    }

    private fun showGameOver() {
        timer?.cancel()

        clueText.text = "🏁 ИГРА ОКОНЧЕНА\n\nИтог: $score очков\n🔥 Серия: $streak"

        answerInput.visibility = View.GONE
        checkButton.visibility = View.GONE
        nextClueButton.text = "🔄 ИГРАТЬ ЕЩЁ"
        nextClueButton.visibility = View.VISIBLE
        nextClueButton.isEnabled = true

        nextClueButton.setOnClickListener {
            answerInput.visibility = View.VISIBLE
            checkButton.visibility = View.VISIBLE
            score = 0
            streak = 0
            round = 0
            startRound()
        }
    }

    private fun normalize(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("\\s+"), " ")
    }

    private fun makeText(text: String, size: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(Color.WHITE)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
    }

    private fun matchWrap(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun buttonParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp(4), 0, dp(4))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
