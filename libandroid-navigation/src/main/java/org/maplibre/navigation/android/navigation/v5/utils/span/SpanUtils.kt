package org.maplibre.navigation.android.navigation.v5.utils.span

import android.text.Spannable
import android.text.SpannableStringBuilder

object SpanUtils {
    fun combineSpans(spanItems: List<SpanItem>): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        for (item in spanItems) {
            if (item is TextSpanItem) {
                appendTextSpan(builder, item.getSpan(), item.spanText)
            }
        }
        return builder
    }

    private fun appendTextSpan(builder: SpannableStringBuilder, span: Any?, spanText: String?) {
        builder.append(spanText, span, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
