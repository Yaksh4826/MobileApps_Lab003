package com.example.productapp

import com.example.productapp.data.matchesProductId
import com.example.productapp.data.validateProductInput
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ProductValidationTest {
    private fun errors(quantity: String = "1", id: String = "104", price: String = "25.00") =
        validateProductInput(id, "Keyboard", price, quantity, "2026-11-20", "Electronics",
            today = LocalDate.of(2026, 11, 20))

    @Test fun acceptsPositiveIntegerQuantity() { assertTrue(errors("3").isEmpty()) }
    @Test fun rejectsInvalidQuantityIncludingOverflow() {
        listOf("", "0", "-1", "1.5", "abc", "2147483648").forEach { input ->
            assertTrue("Must reject: $input", errors(input).any { it.startsWith("Quantity") })
        }
    }
    @Test fun keepsIdBoundariesAndRejectsNonfinitePrices() {
        assertTrue(errors(id = "101").isEmpty())
        assertTrue(errors(id = "999").isEmpty())
        assertFalse(errors(id = "100").isEmpty())
        assertFalse(errors(id = "1000").isEmpty())
        assertFalse(errors(price = "NaN").isEmpty())
        assertFalse(errors(price = "Infinity").isEmpty())
    }
    @Test fun searchMatchesWholeIdAndClearRestoresAll() {
        assertTrue(matchesProductId(101, "101"))
        assertTrue(matchesProductId(101, " 101 "))
        assertFalse(matchesProductId(101, "10"))
        assertFalse(matchesProductId(101, "abc"))
        assertTrue(matchesProductId(102, ""))
    }
}
