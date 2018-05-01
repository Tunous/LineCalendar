package me.thanel.linecalendar.preference

import me.thanel.linecalendar.R
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class IndicatorStyleTest {
    @Test
    fun fromId_shouldMatchStyleById() {
        assertThat(IndicatorStyle.fromId(R.id.indicatorStyleNone), equalTo(IndicatorStyle.None))
        assertThat(IndicatorStyle.fromId(R.id.indicatorStyleCircle), equalTo(IndicatorStyle.Circle))
        assertThat(
            IndicatorStyle.fromId(R.id.indicatorStyleRoundedRect),
            equalTo(IndicatorStyle.RoundedRectangle)
        )
        assertThat(IndicatorStyle.fromId(R.id.indicatorStyleRadioGroup), nullValue())
    }
}
