package org.bopre.support.generator.core.yaml

import org.bopre.support.generator.core.processor.content.impl.SimpleSheet
import org.bopre.support.generator.core.processor.content.style.CellBorders
import org.bopre.support.generator.core.processor.content.style.CellSettings
import org.bopre.support.generator.core.processor.data.LineSource
import org.bopre.support.generator.core.processor.data.RenderProperties
import org.bopre.support.generator.core.processor.render.ConfigurableTemplate
import org.bopre.support.generator.core.processor.render.Generator
import org.bopre.support.generator.core.processor.render.GeneratorTemplate
import org.bopre.support.generator.core.processor.render.PoiDocumentRendererBuilder
import org.bopre.support.generator.core.yaml.data.StyleDefinition

class YamlConfigurerImpl(val configReader: YamlConfigurationReader) : YamlConfigurer {

    val sourConfigurer: SourceConfigurer = SourceConfigurer()
    val contentConfigurer: ContentConfigurer = ContentConfigurer()

    override fun configure(yaml: String, externalSources: Map<String, LineSource>): GeneratorTemplate {
        val parsedDocument = configReader.readDocument(yaml)
        val builder = PoiDocumentRendererBuilder()

        val styleRegister = StyleRegister()

        parsedDocument.styles
            .map { (it.id ?: "") to it }
            .forEach {
                if (!it.first.isBlank()) {
                    styleRegister.register(it.first, it.second)
                }
            }

        parsedDocument.sheets.forEachIndexed { index, sheetDef ->
            val contents =
                sheetDef.content.map { contentConfigurer.configureContent(it, styleRegister) }.toList()
            val sheetName = sheetDef.name ?: "sheet#$index"
            val sheet = SimpleSheet(title = sheetName, contents)
            builder.appendSheet(sheet)
        }

        //register styles to builder
        styleRegister.getRegistered().forEach {
            val styleId = it.key
            val cellSettings = prepareCellSettings(it.value)
            builder.appendStyle(styleId, cellSettings)
        }

        for (sourceDef in parsedDocument.sources) {
            val source = sourConfigurer.configureSource(sourceDef, externalSources)
            if (source != null)
                builder.externalSource(sourceDef.id, source)
        }
        return object : GeneratorTemplate {
            override fun instance(params: Map<String, Any>): ConfigurableTemplate.Result<Generator> {
                return ConfigurableTemplate.Result.Success(
                    builder.build(
                        RenderProperties.of(params)
                    )
                )
            }
        }
    }

    private fun prepareCellSettings(cellStyle: StyleDefinition): CellSettings {
        var cellSettingsBuilder = CellSettings.builder()
        val bordersBuilder = CellBorders.builder()
        cellStyle.borders?.left?.let { bordersBuilder.left(it) }
        cellStyle.borders?.right?.let { bordersBuilder.right(it) }
        cellStyle.borders?.top?.let { bordersBuilder.top(it) }
        cellStyle.borders?.bottom?.let { bordersBuilder.bottom(it) }

        cellStyle.fontSize?.let { cellSettingsBuilder.height(it) }

        cellStyle.alignV?.let { cellSettingsBuilder.verticalAlignment(it) }
        cellStyle.alignH?.let { cellSettingsBuilder.horizontalAlignment(it) }

        cellStyle.bold?.let { cellSettingsBuilder.isBold(it) }
        cellStyle.italic?.let { cellSettingsBuilder.isItalic(it) }
        cellStyle.strikeout?.let { cellSettingsBuilder.isStrikeout(it) }

        cellStyle.wrapped?.let { cellSettingsBuilder.isWrapped(it) }

        cellStyle.font?.let { cellSettingsBuilder.font(it) }

        cellStyle.format?.let { cellSettingsBuilder.dataFormat(it) }

        cellSettingsBuilder.borders(bordersBuilder.build())
        return cellSettingsBuilder.build()
    }

}
