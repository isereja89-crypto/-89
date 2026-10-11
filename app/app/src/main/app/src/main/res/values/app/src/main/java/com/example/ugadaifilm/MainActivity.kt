
Сформированное изображение: Космическая викторина: Угадай фильм



Погнали 😎

Шаг 5 — создаём стили приложения
Add file → Create new file

Имя:

app/src/main/res/values/styles.xml

Вставь:

<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.UgadaiFilm" parent="android:style/Theme.Material.NoActionBar">
        <item name="android:fontFamily">sans</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:statusBarColor">#0B0B0F</item>
        <item name="android:navigationBarColor">#0B0B0F</item>
        <item name="android:colorAccent">#FFB000</item>
    </style>

</resources>
Нажми Commit changes.

После этого напиши «готово» — и начнём самое интересное: сам MainActivity.kt, то есть игровую логику 🎬🔥


Готов

Отлично 🔥 Теперь самое главное.

Шаг 6 — создаём MainActivity.kt
Это будет сам экран игры и вся основная логика.

Создай файл:

app/src/main/java/com/example/ugadaifilm/MainActivity.kt

И вставь туда весь код ниже:

package com.example.ugadaifilm

import android.os.Bundle
import android.os.CountDownTimer
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class Movie(
    val title: String,
    val category: String,
    val clues: List<String>
)

class MainActivity : AppCompatActivity() {

    private val movies = listOf(
        Movie("Титаник", "Мелодрамы", listOf("🚢 Пассажирский корабль", "❤️ Влюблённая пара", "🧊 Огромный айсберг")),
        Movie("Матрица", "Фантастика", listOf("💊 Две загадочные таблетки", "🕶️ Человек в чёрных очках", "💻 Необычная виртуальная реальность")),
        Movie("Один дома", "Комедии", listOf("🏠 Большой пустой дом", "🎄 Рождество", "🧑‍🦱 Мальчик против грабителей")),
        Movie("Назад в будущее", "Фантастика", listOf("🚗 Спортивная машина", "⚡ Молния", "⏰ Путешествие во времени")),
        Movie("Интерстеллар", "Фантастика", listOf("🚀 Космический корабль", "🌌 Чёрная дыра", "👨‍🚀 Путешествие сквозь космос")),
        Movie("Бриллиантовая рука", "Комедии", listOf("💎 Драгоценности", "🧳 Чемодан", "🇷🇺 Советская комедия")),
        Movie("Иван Васильевич меняет профессию", "Комедии", listOf("👑 Царь", "⏳ Машина времени", "🏰 Средневековая Русь")),
        Movie("Кавказская пленница", "Комедии", listOf("🏔️ Кавказские горы", "💍 Похищенная девушка", "🍷 Весёлая компания")),
        Movie("Зелёная миля", "Драмы", listOf("⚡ Электрический стул", "🚶 Тюремный коридор", "🧔 Очень необычный заключённый")),
        Movie("Челюсти", "Ужасы", listOf("🌊 Океан", "🩸 Опасность в воде", "🦈 Огромная акула"))
    )

    private var currentMovie: Movie? = null
    private var clueIndex = 0
    private var score = 0
    private var streak = 0
    private var bestScore = 0
    private var bestStreak = 0
    private var round = 0
    private var timer: CountDownTimer? = null

    private lateinit var titleText: TextView
    private lateinit var scoreText: TextView
    private lateinit var streakText: TextView
    private lateinit var timerText: TextView
    private lateinit var clueText: TextView
    private lateinit var answerInput: EditText
    private lateinit var answerButton: Button
    private lateinit var nextClueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showGameScreen()
        startRound()
    }

    private fun showGameScreen() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 30, 24, 24)
        root.setBackgroundColor(Color.rgb(11, 11, 15))

        titleText = TextView(this)
        titleText.text = "🎬 УГАДАЙ ФИЛЬМ"
        titleText.textSize = 26f
        titleText.setTextColor(Color.WHITE)
        titleText.gravity = Gravity.CENTER
        root.addView(titleText)

        scoreText = TextView(this)
        scoreText.textSize = 18f
        scoreText.setTextColor(Color.WHITE)
        scoreText.gravity = Gravity.CENTER
        root.addView(scoreText)

        streakText = TextView(this)
        streakText.textSize = 18f
        streakText.setTextColor(Color.rgb(255, 180, 0))
        streakText.gravity = Gravity.CENTER
        root.addView(streakText)

        timerText = TextView(this)
        timerText.textSize = 22f
        timerText.setTextColor(Color.WHITE)
        timerText.gravity = Gravity.CENTER
        root.addView(timerText)

        clueText = TextView(this)
        clueText.textSize = 25f
        clueText.setTextColor(Color.WHITE)
        clueText.gravity = Gravity.CENTER
        clueText.setPadding(10, 60, 10, 60)

        root.addView(
            clueText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        answerInput = EditText(this)
        answerInput.hint = "Введите название фильма"
        answerInput.setTextColor(Color.WHITE)
        answerInput.setHintTextColor(Color.GRAY)
        answerInput.setSingleLine(true)
        root.addView(answerInput)

        answerButton = Button(this)
        answerButton.text = "🎯 УГАДАТЬ"
        root.addView(answerButton)

        nextClueButton = Button(this)
        nextClueButton.text = "💡 Следующая подсказка"
        root.addView(nextClueButton)

        setContentView(root)

        answerButton.setOnClickListener {
            checkAnswer()
        }

        nextClueButton.setOnClickListener {
            showNextClue()
        }
    }

    private fun startRound() {

        timer?.cancel()

        clueIndex = 0
        currentMovie = movies.random()

        clueText.text = currentMovie!!.clues[0]

        answerInput.text.clear()
        answerInput.isEnabled = true
        answerButton.isEnabled = true
        nextClueButton.isEnabled = true

        updateScore()

        timer = object : CountDownTimer(20_000, 1_000) {

            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "⏱️ ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                timerText.text = "⏰ Время вышло!"
                failRound()
            }

        }.start()
    }

    private fun showNextClue() {

        if (clueIndex >= 2) {
            return
        }

        clueIndex++

        clueText.text = currentMovie!!.clues[clueIndex]

        nextClueButton.text =
            if (clueIndex == 2) "💡 Последняя подсказка"
            else "💡 Следующая подсказка"
    }

    private fun checkAnswer() {

        val userAnswer = normalize(answerInput.text.toString())
        val correctAnswer = normalize(currentMovie!!.title)

        if (userAnswer == correctAnswer) {

            timer?.cancel()

            val basePoints = when (clueIndex) {
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

            if (score > bestScore) {
                bestScore = score
            }

            if (streak > bestStreak) {
                bestStreak = streak
            }

            Toast.makeText(
                this,
                "✅ Правильно! +$earned",
                Toast.LENGTH_SHORT
            ).show()

            updateScore()

            answerInput.isEnabled = false
            answerButton.isEnabled = false

            nextClueButton.text = "➡️ Следующий фильм"

            nextClueButton.setOnClickListener {
                nextClueButton.text = "💡 Следующая подсказка"
                nextClueButton.setOnClickListener {
                    showNextClue()
                }
                startRound()
            }

        } else {

            Toast.makeText(
                this,
                "❌ Неверно. Можно открыть следующую подсказку.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun failRound() {

        streak = 0

        Toast.makeText(
            this,
            "❌ Фильм: ${currentMovie!!.title}",
            Toast.LENGTH_LONG
        ).show()

        updateScore()

        answerInput.isEnabled = false
        answerButton.isEnabled = false
        nextClueButton.text = "➡️ Следующий фильм"

        nextClueButton.setOnClickListener {
            nextClueButton.text = "💡 Следующая подсказка"
            nextClueButton.setOnClickListener {
                showNextClue()
            }
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

    private fun updateScore() {

        scoreText.text = "🏆 Очки: $score"

        streakText.text =
            if (streak > 0)
                "🔥 Серия: $streak"
            else
                "🔥 Серия: 0"
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
