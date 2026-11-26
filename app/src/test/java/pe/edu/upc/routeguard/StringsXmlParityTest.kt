package pe.edu.upc.routeguard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StringsXmlParityTest {

    private fun parseKeys(file: File): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = doc.getElementsByTagName("string")
        val map = linkedMapOf<String, String>()
        for (i in 0 until nodes.length) {
            val el = nodes.item(i)
            val name = el.attributes.getNamedItem("name").nodeValue
            map[name] = el.textContent ?: ""
        }
        return map
    }

    @Test
    fun spanishAndEnglishStringsHaveExactKeyParity() {
        val baseDir = listOf(
            File("app/src/main/res"),
            File("src/main/res")
        ).firstOrNull { it.exists() }
            ?: error("Could not locate src/main/res from working dir ${File(".").absolutePath}")

        val esFile = File(baseDir, "values/strings.xml")
        val enFile = File(baseDir, "values-en/strings.xml")
        assertTrue("Missing values/strings.xml", esFile.exists())
        assertTrue("Missing values-en/strings.xml", enFile.exists())

        val esKeys = parseKeys(esFile)
        val enKeys = parseKeys(enFile)

        val missingInEn = esKeys.keys - enKeys.keys
        val extraInEn = enKeys.keys - esKeys.keys

        assertTrue("Keys missing in values-en/strings.xml: $missingInEn", missingInEn.isEmpty())
        assertTrue("Extra keys in values-en/strings.xml: $extraInEn", extraInEn.isEmpty())
        assertEquals(esKeys.size, enKeys.size)
        assertTrue("Expected > 200 localized strings", esKeys.size > 200)
    }
}
