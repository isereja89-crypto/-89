package com.example.ugadaifilm

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class Movie(
    val title: String,
    val clues: List<String>,
    val category: String
)

class MainActivity : AppCompatActivity() {

    private val movies = listOf(
        Movie(
            "Титаник",
            listOf("🚢 Пассажирский корабль", "❤️ Влюблённая пара", "🧊 Огромный айсберг"),
            "Мелодрамы"
        ),
        Movie(
            "Матрица",
            listOf("💊 Две загадочные таблетки", "🕶️ Загадочные люди в чёрных очках", "💻 Мир внутри компьютера"),
            "Фантастика"
        ),
        Movie(
            "Один дома",
            listOf("🏠 Большой дом", "👦 Мальчик один", "🪤 Ловушки для грабителей"),
            "Комедии"
        ),
        Movie(
            "Назад в будущее",
            listOf("🚗 Спортивная машина", "⚡ Молния", "⏰ Путешествие во времени"),
            "Фантастика"
        ),
        Movie(
            "Интерстеллар",
            listOf("🚀 Космический корабль", "🌌 Далёкий космос", "🕳️ Чёрная дыра"),
            "Фантастика"
        ),
        Movie(
            "Бриллиантовая рука",
            listOf("💎 Драгоценности", "💍 Загипсованная рука", "🇷🇺 Советский курорт"),
            "Комедии"
        ),
        Movie(
            "Иван Васильевич меняет профессию",
            listOf("👑 Царь", "⏱️ Машина времени", "🏰 Средневековая Русь"),
            "Комедии"
        ),
        Movie(
            "Кавказская пленница",
            listOf("⛰️ Кавказские горы", "👩 Девушка", "😂 Похищение невесты"),
            "Комедии"
        ),
        Movie(
            "Зелёная миля",
            listOf("🚪 Тюремный коридор", "⚡ Электрический стул", "🟢 Зелёная дорожка"),
            "Драмы"
        ),
        Movie(
            "Челюсти",
            listOf("🌊 Океан", "🏖️ Пляж", "🦈 Огромная акула"),
            "Ужасы"
        )
    )

    private val categories = listOf(
        "Все фильмы",
        "Советские фильмы",
        "Комедии",
        "Ужасы",
        "Мелодрамы",
        "Боевики",
        "Фантастика",
        "Детективы",
        "Фэнтези",
        "Драмы",
        "Классика"
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
    private lateinit var categorySpinner: Spinner

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

        val title = makeText("🎬 УГАДАЙ ФИЛЬМ", 30).apply {
            gravity = Gravity.CENTER
        }

        val subtitle = makeText("Угадай фильм по трём подсказкам", 18).apply {
            gravity = Gravity.CENTER
        }

        val modeText = makeText("Режим игры", 18)

        val modeSpinner = Spinner(this)

        modeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                "Классика — 10 раундов",
                "Бесконечный режим"
            )
        )

        modeSpinner.setSelection(if (classicMode) 0 else 1)

        val categoryText = makeText("Категория", 18)

        categorySpinner = Spinner(this)

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        categorySpinner.setSelection(
            categories.indexOf(selectedCategory).coerceAtLeast(0)
        )

        val startButton = makeButton("▶ НАЧАТЬ ИГРУ", 20)
        val recordsButton = makeButton("🏆 РЕКОРДЫ", 18)

        modeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    classicMode = position == 0
                }
            }

        categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategory = categories[position]
                }
            }

        startButton.setOnClickListener {
            score = 0
            streak = 0
            round = 0
            startGame()
        }

        recordsButton.setOnClickListener {
            showRecords()
        }

        add(root, title, 18)
        add(root, subtitle, 12)
        add(root, modeText, 8)
        add(root, modeSpinner, 8)
        add(root, categoryText, 8)
        add(root, categorySpinner, 18)
        add(root, startButton, 10)
        add(root, recordsButton, 10)

        setContentView(root)
    }

    private fun startGame() {

        if (classicMode && round >= 10) {
            showGameOver()
            return
        }

        val available = movies.filter {
            selectedCategory == "Все фильмы" ||
            it.category == selectedCategory
        }

        if (available.isEmpty()) {
            Toast.makeText(
                this,
                "В этой категории пока нет фильмов",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        currentMovie = available.random()

        clueNumber = 0
        timeLeft = 20

        showGameScreen()
        showClue()
        startTimer()
    }

    private fun showGameScreen() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(Color.rgb(18, 18, 22))
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        roundText = makeText("", 16)
        scoreText = makeText("", 16)
        streakText = makeText("", 16)
        timerText = makeText("", 22)

        topRow.addView(
            roundText,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        topRow.addView(
            timerText,
            LinearLayout.LayoutParams(-2, -2)
        )

        topRow.addView(
            scoreText,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        titleText = makeText("🎬 Угадай фильм", 26).apply {
            gravity = Gravity.CENTER
        }

        clueText = makeText("", 25).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(30), dp(12), dp(30))
            setBackgroundColor(Color.rgb(35, 35, 43))
        }

        streakText.gravity = Gravity.CENTER

        answerInput = EditText(this).apply {
            hint = "Название фильма"
            textSize = 18f
            setSingleLine(true)
        }

        checkButton = makeButton("✅ ОТВЕТИТЬ", 18)

        nextClueButton =
            makeButton("💡 Открыть подсказку 2", 18)

        checkButton.setOnClickListener {
            checkAnswer()
        }

        nextClueButton.setOnClickListener {
            showNextClue()
        }

        add(root, topRow, 8)
        add(root, titleText, 12)
        add(root, streakText, 8)
        add(root, clueText, 10)
        add(root, answerInput, 10)
        add(root, checkButton, 8)
        add(root, nextClueButton, 8)

        setContentView(root)

        updateLabels()
    }

    private fun showClue() {

        val movie = currentMovie ?: return

        clueText.text = movie.clues[clueNumber]

        nextClueButton.text = when (clueNumber) {
            0 -> "💡 Открыть подсказку 2"
            1 -> "💡 Открыть подсказку 3"
            else -> "💡 Подсказок больше нет"
        }

        nextClueButton.isEnabled = clueNumber < 2

        updateLabels()
    }

    private fun showNextClue() {

        if (clueNumber >= 2) {
            Toast.makeText(
                this,
                "Это последняя подсказка",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        clueNumber++

        if (!checkButton.isEnabled) {

            checkButton.isEnabled = true
            answerInput.isEnabled = true
            checkButton.text = "✅ ОТВЕТИТЬ СНОВА"

            Toast.makeText(
                this,
                "Серию можно спасти! Награда уменьшена.",
                Toast.LENGTH_SHORT
            ).show()
        }

        showClue()
    }

    private fun checkAnswer() {

        val movie = currentMovie ?: return

        val answer = normalize(
            answerInput.text.toString()
        )

        val correct = normalize(movie.title)

        if (answer.isEmpty()) {

            Toast.makeText(
                this,
                "Напиши название фильма",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (answer == correct) {

            timer?.cancel()

            val basePoints = when (clueNumber) {
                0 -> 300
                1 -> 200
                else -> 100
            }

            val points = if (
                checkButton.text.toString().contains("СНОВА")
            ) {
                basePoints / 2
            } else {
                basePoints
            }

            val finalPoints =
                (points * multiplier()).toInt()

            score += finalPoints
            streak++

            saveRecords()

            Toast.makeText(
                this,
                "🎉 Правильно! +$finalPoints очков",
                Toast.LENGTH_SHORT
            ).show()

            nextRoundAfterDelay()

        } else {

            checkButton.isEnabled = false
            answerInput.isEnabled = false

            nextClueButton.isEnabled = clueNumber < 2

            Toast.makeText(
                this,
                "❌ Неверно. Открой следующую подсказку, чтобы спасти серию.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startTimer() {

        timer?.cancel()

        timer = object : CountDownTimer(
            20_000,
            1_000
        ) {

            override fun onTick(
                millisUntilFinished: Long
            ) {

                timeLeft =
                    (millisUntilFinished / 1000).toInt()

                timerText.text = "⏱ $timeLeft"
            }

            override fun onFinish() {

                timeLeft = 0
                timerText.text = "⏱ 0"

                val movie = currentMovie

                if (movie != null) {

                    Toast.makeText(
                        this@MainActivity,
                        "⏰ Время вышло! Ответ: ${movie.title}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                streak = 0

                nextRoundAfterDelay()
            }
        }.start()
    }

    private fun nextRoundAfterDelay() {

        answerInput.isEnabled = false
        checkButton.isEnabled = false
        nextClueButton.isEnabled = false

        android.os.Handler(mainLooper).postDelayed({

            if (classicMode && round >= 9) {

                round = 10
                showGameOver()

            } else {

                round++
                startGame()
            }

        }, 1200)
    }

    private fun showGameOver() {

        timer?.cancel()

        val prefs = getPrefs()

        val bestScore =
            prefs.getInt("best_score", 0)

        val bestStreak =
            prefs.getInt("best_streak", 0)

        AlertDialog.Builder(this)
            .setTitle("🏆 Игра окончена")
            .setMessage(
                "Итог: $score очков\n" +
                "Серия: $streak\n\n" +
                "Лучший результат: $bestScore\n" +
                "Лучшая серия: $bestStreak"
            )
            .setPositiveButton("ЕЩЁ РАЗ") { _, _ ->

                score = 0
                streak = 0
                round = 0

                startGame()
            }
            .setNegativeButton("В МЕНЮ") { _, _ ->

                showMenu()
            }
            .show()
    }

    private fun showRecords() {

        val prefs = getPrefs()

        val bestScore =
            prefs.getInt("best_score", 0)

        val bestStreak =
            prefs.getInt("best_streak", 0)

        val totalGuessed =
            prefs.getInt("total_guessed", 0)

        AlertDialog.Builder(this)
            .setTitle("🏆 Рекорды")
            .setMessage(
                "Лучший счёт: $bestScore\n" +
                "Лучшая серия: $bestStreak\n" +
                "Всего угадано: $totalGuessed"
            )
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun saveRecords() {

        val prefs = getPrefs()

        val bestScore =
            prefs.getInt("best_score", 0)

        val bestStreak =
            prefs.getInt("best_streak", 0)

        val totalGuessed =
            prefs.getInt("total_guessed", 0)

        prefs.edit()
            .putInt(
                "best_score",
                maxOf(bestScore, score)
            )
            .putInt(
                "best_streak",
                maxOf(bestStreak, streak)
            )
            .putInt(
                "total_guessed",
                totalGuessed + 1
            )
            .apply()
    }

    private fun multiplier(): Float {

        return when {

            streak >= 20 -> 5f

            streak >= 10 -> 3f

            streak >= 5 -> 2f

            streak >= 3 -> 1.5f

            else -> 1f
        }
    }

    private fun updateLabels() {

        roundText.text =
            if (classicMode) {
                "Раунд ${round + 1}/10"
            } else {
                "Раунд ${round + 1}"
            }

        scoreText.text = "🏆 $score"

        streakText.text =
            "🔥 Серия: $streak"

        timerText.text =
            "⏱ $timeLeft"
    }

    private fun normalize(text: String): String {

        return text
            .trim()
            .lowercase()
            .replace('ё', 'е')
            .replace(
                Regex("\\s+"),
                " "
            )
    }

    private fun getPrefs() =
        getSharedPreferences(
            "ugadai_film",
            Context.MODE_PRIVATE
        )

    private fun makeText(
        text: String,
        size: Int
    ): TextView {

        return TextView(this).apply {

            this.text = text

            textSize = size.toFloat()

            setTextColor(Color.WHITE)

            gravity =
                Gravity.CENTER_VERTICAL
        }
    }

    private fun makeButton(
        text: String,
        size: Int
    ): Button {

        return Button(this).apply {

            this.text = text

            textSize = size.toFloat()

            isAllCaps = false
        }
    }

    private fun add(
        parent: LinearLayout,
        view: View,
        margin: Int
    ) {

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            dp(margin),
            0,
            dp(margin)
        )

        parent.addView(view, params)
    }

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    override fun onDestroy() {

        timer?.cancel()

        super.onDestroy()
    }
}
