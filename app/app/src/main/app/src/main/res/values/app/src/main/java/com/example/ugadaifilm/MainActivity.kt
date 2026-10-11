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
    val clues: List<String>
)

class MainActivity : AppCompatActivity() {

    private val movies = listOf(
        Movie("Титаник", listOf("🚢 Пассажирский корабль", "❤️ Влюблённая пара", "🧊 Огромный айсберг")),
        Movie("Матрица", listOf("💊 Две загадочные таблетки", "🕶️ Человек в чёрных очках", "💻 Цифровой мир")),
        Movie("Один дома", listOf("🏠 Большой дом", "👦 Мальчик один", "🪤 Ловушки для грабителей")),
        Movie("Назад в будущее", listOf("🚗 Спортивная машина", "⚡ Молния", "⏰ Путешествие во времени")),
        Movie("Интерстеллар", listOf("🚀 Космический корабль", "🌌 Далёкий космос", "🕳️ Чёрная дыра")),
        Movie("Бриллиантовая рука", listOf("💎 Драгоценности", "🩹 Загипсованная рука", "🇷🇺 Советский курорт")),
        Movie("Иван Васильевич меняет профессию", listOf("👑 Царь", "⏳ Машина времени", "🏰 Средневековая Русь")),
        Movie("Кавказская пленница", listOf("🏔️ Кавказские горы", "👩 Девушка", "💍 Похищение невесты")),
        Movie("Зелёная миля", listOf("🚪 Тюремный коридор", "⚡ Электрический стул", "🟢 Зелёная дорожка")),
        Movie("Челюсти", listOf("🌊 Океан", "🏖️ Пляж", "🦈 Огромная акула"))
    )

    private var currentMovie: Movie? = null
    private var clueNumber = 0
    private var score = 0
    private var streak = 0
    private var round = 0
    private var timeLeft = 20

    private lateinit var clueText: TextView
    private lateinit var answerInput: EditText
    private lateinit var checkButton: Button
    private lateinit var nextClueButton: Button
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var streakText: TextView
    private lateinit var roundText: TextView

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()
        startGame()
    }

    private fun createInterface() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30, 30, 30, 30)
        root.setBackgroundColor(Color.rgb(18, 18, 22))

        roundText = makeText("Раунд 1 / 10", 18)
        timerText = makeText("⏱ 20", 22)
        scoreText = makeText("🏆 Очки: 0", 18)
        streakText = makeText("🔥 Серия: 0", 18)

        root.addView(roundText)
        root.addView(timerText)
        root.addView(scoreText)
        root.addView(streakText)

        clueText = makeText(
            "Подсказка появится здесь",
            28
        )

        clueText.gravity = Gravity.CENTER
        clueText.setPadding(20, 60, 20, 60)

        root.addView(
            clueText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        answerInput = EditText(this)
        answerInput.hint = "Название фильма"
        answerInput.setTextColor(Color.WHITE)
        answerInput.setHintTextColor(Color.GRAY)
        answerInput.textSize = 18f

        root.addView(answerInput)

        checkButton = Button(this)
        checkButton.text = "🎬 ОТВЕТИТЬ"

        root.addView(checkButton)

        nextClueButton = Button(this)
        nextClueButton.text = "💡 Следующая подсказка"

        root.addView(nextClueButton)

        checkButton.setOnClickListener {
            checkAnswer()
        }

        nextClueButton.setOnClickListener {
            showNextClue()
        }

        setContentView(root)
    }

    private fun makeText(text: String, size: Int): TextView {
        val view = TextView(this)
        view.text = text
        view.textSize = size.toFloat()
        view.setTextColor(Color.WHITE)
        view.setPadding(5, 8, 5, 8)
        return view
    }

    private fun startGame() {

        if (round >= 10) {
            showGameOver()
            return
        }

        currentMovie = movies.random()
        clueNumber = 0

        round++
        roundText.text = "Раунд $round / 10"

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

        when (clueNumber) {
            0 -> nextClueButton.text = "💡 Открыть подсказку 2"
            1 -> nextClueButton.text = "💡 Открыть подсказку 3"
            2 -> nextClueButton.text = "💡 Больше подсказок нет"
        }
    }

    private fun showNextClue() {

        if (clueNumber >= 2) {
            Toast.makeText(
                this,
                "Это последняя подсказка!",
                Toast.LENGTH_SHORT
            ).show()
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

            streak++

            val multiplier = when {
                streak >= 20 -> 5.0
                streak >= 10 -> 3.0
                streak >= 5 -> 2.0
                streak >= 3 -> 1.5
                else -> 1.0
            }

            val earned = (basePoints * multiplier).toInt()
            score += earned

            scoreText.text = "🏆 Очки: $score"
            streakText.text = "🔥 Серия: $streak"

            Toast.makeText(
                this,
                "🎉 Правильно! +$earned",
                Toast.LENGTH_SHORT
            ).show()

            nextRoundAfterDelay()

        } else {

            Toast.makeText(
                this,
                "❌ Неправильно. Можно открыть следующую подсказку.",
                Toast.LENGTH_SHORT
            ).show()

            streak = 0
            streakText.text = "🔥 Серия: $streak"
        }
    }

    private fun normalize(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace("ё", "е")
            .replace(Regex("\\s+"), " ")
    }

    private fun startTimer() {

        timer?.cancel()

        timeLeft = 20
        timerText.text = "⏱ $timeLeft"

        timer = object : CountDownTimer(20_000, 1_000) {

            override fun onTick(millisUntilFinished: Long) {

                timeLeft = (millisUntilFinished / 1000).toInt()

                timerText.text = "⏱ $timeLeft"
            }

            override fun onFinish() {

                timeLeft = 0
                timerText.text = "⏱ 0"

                Toast.makeText(
                    this@MainActivity,
                    "⏰ Время вышло! Ответ: ${currentMovie?.title}",
                    Toast.LENGTH_LONG
                ).show()

                streak = 0
                streakText.text = "🔥 Серия: 0"

                nextRoundAfterDelay()
            }

        }.start()
    }

    private fun nextRoundAfterDelay() {

        answerInput.isEnabled = false
        checkButton.isEnabled = false
        nextClueButton.isEnabled = false

        android.os.Handler(mainLooper).postDelayed({

            if (round >= 10) {
                showGameOver()
            } else {
                startGame()
            }

        }, 1200)
    }

    private fun showGameOver() {

        timer?.cancel()

        clueText.text = "🏁 ИГРА ОКОНЧЕНА\n\n🏆 Итог: $score очков\n🔥 Серия: $streak"

        answerInput.visibility = View.GONE
        checkButton.visibility = View.GONE
        nextClueButton.text = "🔄 ИГРАТЬ ЕЩЁ"
        nextClueButton.visibility = View.VISIBLE
        nextClueButton.isEnabled = true

        nextClueButton.setOnClickListener {

            score = 0
            streak = 0
            round = 0

            scoreText.text = "🏆 Очки: 0"
            streakText.text = "🔥 Серия: 0"

            answerInput.visibility = View.VISIBLE
            checkButton.visibility = View.VISIBLE

            startGame()
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
