package com.newshunt.common.view.customview.fontview


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.util.R
import com.newshunt.common.util.databinding.ViewCapTextViewBinding
import com.newshunt.common.view.customview.CapLeadingMarginSpan2
import com.newshunt.common.view.customview.internalLinkTouchListener
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.news.util.NewsConstants


@SuppressLint("Recycle")
class CapTextView constructor(
    context: Context,
    readArrayAttributes: AttributeSet?,
    defStyleAttr: Int
) : ConstraintLayout(context, readArrayAttributes, defStyleAttr) {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


   val layoutInflator = LayoutInflater.from(context)
    lateinit  var binding: ViewCapTextViewBinding

    private var isCapText: Boolean = false
    private var capDropNo : Int = 0
    private var lineDropNo : Int = 0
    private var selectable : Boolean = false
    private var enableCapText : Boolean = false
    private var fontTextSizeMultipier : Int = 0
    val DEFAULT_FONT_SIZE = 16
    val DEFAULT_LINE_SPACING = 9
    val DEFAULT_LEFT_MARGIN = 48
    val CAP_FONT_SIZE = 50
    val CAP_FONT_SIZE_EN = 57

    init {
        readArrayAttributes?.let {
            val typedArray = context.obtainStyledAttributes(
                readArrayAttributes,
                R.styleable.CapTextView
            )
            binding =  DataBindingUtil.inflate(layoutInflator, R.layout.view_cap_text_view, this, true)
              isCapText = typedArray.getBoolean(R.styleable.CapTextView_isCapText, false)
            val text = typedArray.getString(R.styleable.CapTextView_android_text)
            lineDropNo = typedArray.getInt(R.styleable.CapTextView_lineDropNo, 3)
            val bodyTextColorStr = typedArray.getString(R.styleable.CapTextView_bodyTextColor)
            val bodyTextColor = ViewUtils.getColor(bodyTextColorStr, Color.BLACK)
            val capTextColorStr = typedArray.getString(R.styleable.CapTextView_capTextColor)
            val capTextColor = ViewUtils.getColor(capTextColorStr, Color.BLACK)

            binding.tvBody.setTextColor(bodyTextColor)
            binding.tvBody2.setTextColor(bodyTextColor)
            binding.tvCap.setTextColor(capTextColor)
        }

    }

    fun setHtmlText(text: Spannable, originalString :String, langCode: String?= null,size :Int=0) {
        if(text.isNotEmpty()) {
            setCapText(text,originalString,langCode,size)
        }
    }

    fun setTextIsSelectable(selectable: Boolean) {
        this.selectable = selectable;
    }

    fun setCapDropNumber(capDropNo :Int?){
        this.capDropNo = capDropNo?:0
    }

    fun  enableCapText(enable:Boolean?){
        enableCapText = enable?:true
    }

    private fun setCapText(formatText: Spannable, originalString :String, langCode: String?,size :Int=0) {

        val isEng = langCode.equals(NewsConstants.ENGLISH_LANGUAGE_CODE)
        fontTextSizeMultipier = size
        val fontSize = DEFAULT_FONT_SIZE + fontTextSizeMultipier.toFloat()
        val lineSpacing = (DEFAULT_LINE_SPACING* fontSize)/DEFAULT_FONT_SIZE

        binding.tvBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
        binding.tvBody2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
        binding.tvBody.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineSpacing,  getResources().getDisplayMetrics()), 1.0f)
        binding.tvBody2.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineSpacing,  getResources().getDisplayMetrics()), 1.0f)

        if(formatText.isNotEmpty()) {
            var leftMargin = 0
            val fontSize = DEFAULT_FONT_SIZE + fontTextSizeMultipier.toFloat()
            val margin = (DEFAULT_LEFT_MARGIN* fontSize)/DEFAULT_FONT_SIZE
            if(enableCapText && capDropNo>0) {
                var capText = formatText.subSequence(0,capDropNo)
                val firstChar = capText.get(0)
                if(isEng || (capDropNo==1 && isEngLetter(firstChar))) {
                    binding.tvCap.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CAP_FONT_SIZE_EN + (fontTextSizeMultipier.toFloat() * 3.125F))
                    binding.tvCap.setPadding(0, CommonUtils.getDimension(R.dimen.first_akshar_padding), 0, 0)
                } else {
                    binding.tvCap.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CAP_FONT_SIZE + (fontTextSizeMultipier.toFloat() * 3.125F))

                }
                binding.tvCap.visibility =View.VISIBLE
                binding.tvBody.setPadding(0, CommonUtils.getDimension(R.dimen.first_akshar_text_padding), 0, 0)
                val spannableString =
                        SpannableString(capText)
                setTextView(spannableString,capText.toString(), binding.tvCap,langCode)

                val bounds = Rect()
                val textPaint: Paint = binding.tvCap.getPaint()
                textPaint.getTextBounds(capText.toString(), 0, capDropNo, bounds)
                leftMargin = bounds.width() + margin.toInt()
            }else{
                binding.tvCap.visibility =View.GONE
                capDropNo = 0
                binding.tvBody.setPadding(0,0, 0, 0)
            }

            if(capDropNo==0){
              binding.tvBody.minLines =0
            }else{
                binding.tvBody.minLines =2
            }

            // get all paragraph
            val paragraph = formatText.split("\n")
            var firstParaLength = 0

            // first paragraph will take space from start rest will be same
            paragraph.filter { it.isNotBlank() }.forEachIndexed { index, str ->

                when(index) {
                    0 -> {
                        if (str.length >capDropNo) {
                            val spannableString =
                                SpannableString(formatText.subSequence(capDropNo, str.length))
                            spannableString.setSpan(
                                CapLeadingMarginSpan2(lineDropNo, leftMargin),
                                0,
                                spannableString.length,
                                0
                            )
                            firstParaLength =str.length
                            setTextView(spannableString,originalString,binding.tvBody,langCode)
                        }
                    }
                }
            }
            val finalString = SpannableString(formatText.subSequence(firstParaLength ,formatText.length))
            if (!TextUtils.isEmpty(finalString.trim())) {
               setTextView(finalString,originalString,binding.tvBody2,langCode)
            }else{
                binding.tvBody2.visibility = View.GONE
            }
        }
    }

    private fun isEngLetter(c: Char): Boolean {
        return c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z'
    }

    private fun setTextView(finalString: Spannable,originalString:String ,view :LengthNotifyingSelectCopyTextView,langCode: String?){
        val spannableString =
                SpannableString(finalString)
        view.visibility = View.VISIBLE
        view.setTextIsSelectable(selectable)
        view.setOnTouchListener(internalLinkTouchListener(spannableString))
        view.setClickable(true);
        view.setMovementMethod(LinkMovementMethod.getInstance())
        view.setSpannableTextWithLangSpecificTypeFaceChanges(spannableString, originalString, TextView.BufferType.SPANNABLE, langCode)

    }

}