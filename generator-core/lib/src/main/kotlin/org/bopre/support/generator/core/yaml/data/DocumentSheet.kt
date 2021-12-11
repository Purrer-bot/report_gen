package org.bopre.support.generator.core.yaml.data

import kotlinx.serialization.Serializable

@Serializable
data class DocumentSheet(val id: String?, val name: String?, val chunks: List<ChunkDefinition> = emptyList())