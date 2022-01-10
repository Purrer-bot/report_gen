package org.bopre.support.generator.core.yaml

import org.bopre.support.generator.core.yaml.data.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class YamlConfigurationReaderImplTest {

    lateinit var reader: YamlConfigurationReader

    @BeforeEach
    fun beforeEach() {
        reader = YamlConfigurationReaderImpl()
    }

    @Test
    fun `yaml config static sources`() {
        val input = """
            docname: test
            sources:
                - type: static
                  id: source_01
                  lines:
                    - id: 01
                      name: user_01
                    - id: 02
                      name: user_02
                    - id: 03
                      name: user_03
            """

        val expected = Document(
            docname = "test",
            sources = listOf(
                SourceDefinition.static(
                    "source_01",
                    listOf(
                        mapOf("id" to "01", "name" to "user_01"),
                        mapOf("id" to "02", "name" to "user_02"),
                        mapOf("id" to "03", "name" to "user_03")
                    )
                )
            )
        )

        val actual = reader.readDocument(input)
        assertEquals(expected, actual, "wrong deserialization")
    }

    @Test
    fun `read sample yaml configuration`() {
        val input = """
            docname: sample document
            sheets:
                - id: report_0
                  name: report number 0
                  content:
                    - type: table
                      sourceId: source_id
                      id: table1
                      title: table1 for report 0
                      columns:
                        - id: id
                          title: identifier
                        - id: name
                          title: username
                    - type: separator
                      strength: 23
            """

        val expected = Document(
            docname = "sample document",
            sheets = listOf(
                DocumentSheet(
                    id = "report_0",
                    name = "report number 0",
                    content = listOf(
                        ContentDefinition.TableDefinition(
                            id = "table1",
                            title = "table1 for report 0",
                            sourceId = "source_id",
                            columns = listOf(
                                CellParameters(id = "id", title = "identifier"),
                                CellParameters(id = "name", title = "username")
                            )
                        ),
                        ContentDefinition.Separator(23)
                    )
                )
            ),
        )

        val actual = reader.readDocument(input)
        assertEquals(expected, actual, "wrong deserialization")
    }

    @Test
    fun `mostly complete definition test`() {
        val fileYaml = getResourceAsFile("/examples/definition.yaml")
        val yamlContent: String = fileYaml.useLines { it.joinToString("\n") }

        val expected = Document(
            docname = "sample document",
            sheets = listOf(
                DocumentSheet(
                    id = "report_0",
                    name = "report number 0",
                    content = listOf(
                        ContentDefinition.TableDefinition(
                            id = "table1",
                            title = "table1 for report 0",
                            sourceId = "source_01",
                            columns = listOf(
                                CellParameters(id = "id", title = "identifier"),
                                CellParameters(id = "name", title = "username")
                            )
                        ),
                        ContentDefinition.Separator(2),
                        ContentDefinition.TableDefinition(
                            id = "table2",
                            title = "table1 for report 0",
                            sourceId = "source_01",
                            columns = listOf(
                                CellParameters(id = "id", title = "identifier"),
                                CellParameters(id = "name", title = "username")
                            )
                        ),
                    )
                )
            ),
            sources = listOf(
                SourceDefinition.StaticSourceDefinition(
                    id = "source_01",
                    lines = listOf(
                        mapOf(
                            "id" to "01",
                            "name" to "user_01"
                        ),
                        mapOf(
                            "id" to "02",
                            "name" to "user_02"
                        ),
                        mapOf(
                            "id" to "03",
                            "name" to "user_03"
                        )
                    )
                )
            )
        )

        val actual = reader.readDocument(yamlContent)
        assertEquals(expected, actual, "wrong deserialization")
    }

    fun getResourceAsFile(path: String): File {
        return File(object {}.javaClass.getResource(path).file)
    }

}

