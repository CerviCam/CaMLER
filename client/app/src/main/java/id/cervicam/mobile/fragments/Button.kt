package id.cervicam.mobile.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import id.cervicam.mobile.R
import kotlinx.android.synthetic.main.fragment_button.*


class Button: Fragment() {
    // Enums
    enum class ButtonType {
        FILLED, OUTLINE, CLEAN
    }

    companion object {
        // Argument keys
        private const val ARG_LABEL: String = "LABEL"
        private const val ARG_TYPE: String = "TYPE"
        private const val ARG_CLICKABLE: String = "CLICKABLE"

        fun newInstance(
            label: String,
            type: ButtonType = ButtonType.FILLED,
            clickable: Boolean = true,
            onClick: (() -> Unit)? = null
        ): Button {
            val button: Button = Button()

            val args: Bundle = Bundle()
            args.putString(Button.ARG_LABEL, label)
            args.putString(Button.ARG_TYPE, type.name)
            args.putBoolean(Button.ARG_CLICKABLE, clickable)

            button.arguments = args
            button.onClick = onClick
            return button
        }
    }

    private var onClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.text = arguments!!.getString(ARG_LABEL)
        val clickable: Boolean? = arguments!!.getBoolean(ARG_CLICKABLE)

        val background: GradientDrawable = button.background as GradientDrawable
        when (arguments!!.getString(ARG_TYPE)) {
            ButtonType.FILLED.name -> {
                background.setColor(ContextCompat.getColor(context!!, R.color.colorAccent))
                button.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                background.setStroke(0, Color.TRANSPARENT)
            } ButtonType.OUTLINE.name -> {
                background.setColor(Color.TRANSPARENT)
                button.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
                background.setStroke(2, ContextCompat.getColor(context!!, R.color.colorAccent))
            } ButtonType.CLEAN.name -> {
                background.setColor(Color.TRANSPARENT)
                button.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
                background.setStroke(0, Color.TRANSPARENT)
            }
        }

        if (!clickable!!) {
            background.setColor(ContextCompat.getColor(context!!, R.color.colorGray))
            button.setTextColor(ContextCompat.getColor(context!!, R.color.colorBlack))
        }

        button.setOnClickListener {
            if (clickable) {
                onClick?.let { it1 -> it1() }
            }
        }
    }
}