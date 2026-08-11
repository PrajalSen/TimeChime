package com.example

import com.example.domain.model.Announcement
import com.example.domain.usecase.ValidateAnnouncementUseCase
import com.example.domain.usecase.ValidationResult
import com.example.utils.ExportUtils
import com.example.utils.TimePeriod
import com.example.utils.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementManagementTest {

    private val validator = ValidateAnnouncementUseCase()

    @Test
    fun testValidAnnouncementValidation() {
        val announcement = Announcement(hour = 8, minute = 30, volume = 80)
        val result = validator.validate(announcement, emptyList())
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun testDuplicateTimeValidationWarning() {
        val existing = listOf(
            Announcement(id = 1L, hour = 8, minute = 30)
        )
        val newAnnouncement = Announcement(id = 2L, hour = 8, minute = 30)
        val result = validator.validate(newAnnouncement, existing)
        assertTrue(result is ValidationResult.Warning)
    }

    @Test
    fun testTimePeriodGrouping() {
        assertEquals(TimePeriod.MORNING, TimePeriod.fromHour(8))
        assertEquals(TimePeriod.AFTERNOON, TimePeriod.fromHour(14))
        assertEquals(TimePeriod.EVENING, TimePeriod.fromHour(18))
        assertEquals(TimePeriod.NIGHT, TimePeriod.fromHour(22))
    }

    @Test
    fun testRepeatDaysFormatting() {
        assertEquals("Every day", TimeUtils.formatRepeatDays(listOf(1, 2, 3, 4, 5, 6, 7)))
        assertEquals("Weekdays", TimeUtils.formatRepeatDays(listOf(1, 2, 3, 4, 5)))
        assertEquals("Weekends", TimeUtils.formatRepeatDays(listOf(6, 7)))
        assertEquals("Mon, Wed, Fri", TimeUtils.formatRepeatDays(listOf(1, 3, 5)))
    }

    @Test
    fun testExportAndImportJson() {
        val list = listOf(
            Announcement(id = 10, hour = 9, minute = 0, bellSound = "Soft Bell", volume = 70),
            Announcement(id = 20, hour = 17, minute = 45, bellSound = "Tower Bell", volume = 100)
        )
        val jsonStr = ExportUtils.exportToJson(list)
        val imported = ExportUtils.importFromJson(jsonStr)

        assertEquals(2, imported.size)
        assertEquals(9, imported[0].hour)
        assertEquals("Soft Bell", imported[0].bellSound)
        assertEquals(70, imported[0].volume)
    }

    @Test
    fun testExportAndImportCsv() {
        val list = listOf(
            Announcement(id = 1, hour = 12, minute = 15, bellSound = "Zen Bowl", volume = 65)
        )
        val csvStr = ExportUtils.exportToCsv(list)
        val imported = ExportUtils.importFromCsv(csvStr)

        assertEquals(1, imported.size)
        assertEquals(12, imported[0].hour)
        assertEquals(15, imported[0].minute)
        assertEquals("Zen Bowl", imported[0].bellSound)
        assertEquals(65, imported[0].volume)
    }
}
