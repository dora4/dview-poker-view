package dora.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import dora.widget.pokerview.R

class DoraPokerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var frontView: ImageView
    private var backView: ImageView
    private var isFront = true

    // 声音
    private val soundPool: SoundPool
    private val flipSoundId: Int

    init {
        // 设置相机距离防止透视失真
        cameraDistance = 8000 * resources.displayMetrics.density

        // 创建两张 ImageView
        frontView = AppCompatImageView(context)
        backView = AppCompatImageView(context)

        frontView.scaleType = ImageView.ScaleType.FIT_XY
        backView.scaleType = ImageView.ScaleType.FIT_XY
        backView.visibility = View.GONE

        addView(frontView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(backView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // 从 XML 读取属性
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.DoraPokerView)
            if (ta.hasValue(R.styleable.DoraPokerView_dview_pv_frontSrc))
                frontView.setImageResource(ta.getResourceId(R.styleable.DoraPokerView_dview_pv_frontSrc, 0))
            if (ta.hasValue(R.styleable.DoraPokerView_dview_pv_backSrc))
                backView.setImageResource(ta.getResourceId(R.styleable.DoraPokerView_dview_pv_backSrc, 0))
            ta.recycle()
        }

        // 初始化音效
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttrs)
            .setMaxStreams(2)
            .build()
        flipSoundId = soundPool.load(context, R.raw.flip, 1)
    }

    /** 翻牌动画 */
    fun flipCard() {
        val visible = if (isFront) frontView else backView
        val hidden = if (isFront) backView else frontView

        playFlipSound()

        val animOut = ObjectAnimator.ofFloat(visible, "rotationY", 0f, 90f).apply {
            duration = 200
        }

        val animIn = ObjectAnimator.ofFloat(hidden, "rotationY", -90f, 0f).apply {
            duration = 200
        }

        animOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visible.visibility = View.GONE
                hidden.visibility = View.VISIBLE
                animIn.start()
                isFront = !isFront
            }
        })

        animOut.start()
    }

    /** 设置正面图片 */
    fun setFrontImage(resId: Int) {
        frontView.setImageResource(resId)
    }

    /** 设置背面图片 */
    fun setBackImage(resId: Int) {
        backView.setImageResource(resId)
    }

    private fun playFlipSound() {
        soundPool.play(flipSoundId, 1f, 1f, 1, 0, 1f)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        soundPool.release()
    }
}
