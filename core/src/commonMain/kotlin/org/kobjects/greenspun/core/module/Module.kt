package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmSection
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.FuncImpl
import org.kobjects.greenspun.core.func.FuncImport
import org.kobjects.greenspun.core.global.GlobalImpl
import org.kobjects.greenspun.core.global.GlobalInterface
import org.kobjects.greenspun.core.global.GlobalImport
import org.kobjects.greenspun.core.memory.MemoryImpl
import org.kobjects.greenspun.core.memory.MemoryImport
import org.kobjects.greenspun.core.memory.MemoryInterface
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.I32

class Module(
    val types: List<FuncType>,
    val funcs: List<FuncInterface>,
    // tables
    val memory: MemoryInterface?,
    val globals: List<GlobalInterface>,
    val exports: List<Export>,
    val start: Int?,
    // elements
    val datas: List<DataImpl>,
) {

    fun imports() = (
            funcs.filterIsInstance<FuncImport>() +
                    globals.filterIsInstance<MemoryImport>() +
                    (if (memory == null) emptyList() else listOf(memory))
            ) as List<Imported>


    private fun writeTypes(writer: ModuleWriter) {
        writer.writeU32(types.size)
        for (type in types) {
            type.toWasm(writer)
        }
    }

    private fun writeImports(writer: ModuleWriter) {
        val imports = imports()
        if (imports.isNotEmpty()) {
            writer.writeU32(imports.size)
            for (i in imports) {
                writer.writeName(i.module)
                writer.writeName(i.name)
                i.writeImportDescription(writer)
            }
        }
    }

    private fun writeFunctions(writer: ModuleWriter) {
        val funcs = funcs.filterIsInstance<FuncImpl>()
        writer.writeU32(funcs.size)
        for (func in funcs) {
            writer.writeU32(func.type.index)
        }
    }

    private fun writeCode(writer: ModuleWriter) {
        val funcs = funcs.filterIsInstance<FuncImpl>()
        writer.writeU32(funcs.size)
        for (func in funcs) {
            val funcWriter = ModuleWriter(this)
            func.writeBody(funcWriter)
            val bytes = funcWriter.toByteArray()

            writer.writeU32(bytes.size)
            writer.writeBytes(bytes)
        }
    }

    private fun writeExports(writer: ModuleWriter) {
        if (exports.isNotEmpty()) {
            writer.writeU32(exports.size)
            for (export in exports) {
                writer.writeName(export.name)
                export.value.writeExportDescription(writer)
            }
        }
    }

    private fun writeMemory(writer: ModuleWriter) {
        if (memory is MemoryImpl) {
            writer.writeU32(1)
            memory.writeType(writer)
        }
    }

    private fun writeDataCount(writer: ModuleWriter) {
        if (datas.isNotEmpty()) {
            writer.writeU32(datas.size)
        }
    }

    private fun writeDatas(writer: ModuleWriter) {
        if (datas.isNotEmpty()) {
            writer.writeU32(datas.size)
            for (data in datas) {
                if (data.offset == null) {
                    writer.writeU32(1)
                } else {
                    writer.writeU32(0)
                    I32.Const(data.offset).toWasm(writer)
                    writer.write(WasmOpcode.END)
                }
                writer.writeU32(data.data.size)
                writer.writeBytes(data.data)
            }
        }
    }

    private fun writeSection(writer: ModuleWriter, section: WasmSection, writeSection: (ModuleWriter) -> Unit) {
        val sectionWriter = ModuleWriter(this)
        writeSection(sectionWriter)
        val bytes = sectionWriter.toByteArray()

        if (bytes.isNotEmpty()) {
            writer.writeByte(section.id)
            writer.writeU32(bytes.size)
            writer.writeBytes(bytes)
        }
    }


    fun toWasm(): ByteArray {
        val writer = ModuleWriter(this)

        // Magic
        writer.writeByte(0)
        writer.writeByte(0x61)
        writer.writeByte(0x73)
        writer.writeByte(0x6d)

        // Version
        writer.writeInt(1)

        // Sections
        writeSection(writer, WasmSection.TYPE, ::writeTypes)
        writeSection(writer, WasmSection.IMPORT, ::writeImports)
        writeSection(writer, WasmSection.FUNCTION, ::writeFunctions)
        // Table
        writeSection(writer, WasmSection.MEMORY, ::writeMemory)
        // Global
        writeSection(writer, WasmSection.EXPORT, ::writeExports)
        // Start
        // Elements
        writeSection(writer, WasmSection.DATA_COUNT, ::writeDataCount)
        writeSection(writer, WasmSection.CODE, ::writeCode)
        writeSection(writer, WasmSection.DATA, ::writeDatas)

        return writer.toByteArray()
    }


    fun toString(writer: CodeWriter) {
        writer.write("Module {")
        val inner = writer.indented()

        for (global in globals.filterIsInstance<GlobalImport>()) {
            global.writeImport(writer)
        }

        for (func in funcs.filterIsInstance<FuncImport>()) {
            func.writeImport(writer)
        }

        for (global in globals.filterIsInstance<GlobalImpl>()) {
            global.toString(writer)
        }


        for (func in funcs.filterIsInstance<FuncImpl>()) {
            func.toString(inner)
        }

        writer.newLine()

        for (export in exports) {
            writer.newLine()
            writer.write("Export(")
            writer.writeQuoted(export.name)
            writer.write(", ")
            export.value.writeExportDescription(writer)
            writer.write(")")
        }

        writer.newLine()
        writer.write("}\n")
    }

    override fun toString(): String {
        val writer = CodeWriter()
        toString(writer)
        return writer.toString()
    }
}