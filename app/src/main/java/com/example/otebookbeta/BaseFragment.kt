package com.example.otebookbeta

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import android.widget.ScrollView
import com.google.android.material.textfield.TextInputEditText

open class BaseFragment : Fragment() {

    // Булева переменная для управления фокусом при клике на end icon
    protected open val shouldRequestFocusOnEndIconClick: Boolean = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим ScrollView или NestedScrollView в макете
        val scrollView = findScrollView(view)
        scrollView?.let { sv ->
            // Настраиваем OnTouchListener для скрытия клавиатуры при касании
            sv.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboardIfInputFieldFocused()
                }
                false // Позволяем обработку касания другим слушателям
            }

            // Настраиваем слушатель скролла (только для NestedScrollView)
            if (sv is NestedScrollView) {
                sv.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                    if (scrollY != oldScrollY) {
                        hideKeyboardIfInputFieldFocused()
                    }
                }
            }
        }

        // Добавляем обработчик скролла для всех TextInputEditText
        setupScrollOnTextInputFields(view)
    }

    private fun findScrollView(view: View): View? {
        if (view is ScrollView || view is NestedScrollView) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val found = findScrollView(child)
                if (found != null) return found
            }
        }
        return null
    }

    private fun setupScrollOnTextInputFields(view: View) {
        if (view is TextInputEditText) {
            view.setOnTouchListener { v, event ->
                Log.d("BaseFragment", "Touch event on TextInputEditText: ${event.action}")
                if (event.action == MotionEvent.ACTION_MOVE) {
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupScrollOnTextInputFields(view.getChildAt(i))
            }
        }
    }

    private fun hideKeyboardIfInputFieldFocused() {
        val focusedView = activity?.currentFocus
        if (focusedView is TextInputEditText) {
            Log.d("BaseFragment", "Hiding keyboard for view ID: ${focusedView.id}")
            hideKeyboard()
        } else if (focusedView is AutoCompleteTextView) {
            Log.d("BaseFragment", "AutoCompleteTextView focused, no keyboard hiding")
            // Не скрываем клавиатуру и не вызываем showDropDown
        }
    }

    protected fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: this.view
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        Log.d("BaseFragment", "Keyboard hidden")
    }
}