package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmSection
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.func.ImportedFunc
import org.kobjects.greenspun.core.global.Global
import org.kobjects.greenspun.core.instance.ImportObject
import org.kobjects.greenspun.core.instance.Instance
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.I32

class Module(
    val types: List<FuncType>,
//    val funcImports: List<ImportFunc>,
    val funcs: List<FuncInterface>,
    val globals: List<Global>,
    val start: Func?,
    val datas: List<Data>,
    val exports: List<Export>
) {
    fun createInstance(importObject: ImportObject): Instance {
        val funcImports = funcs.filterIsInstance<ImportedFunc>()

        val resolvedImports = List<((Instance, Array<Any>) -> Any)>(funcImports.size) {
            val funcImport = funcImports[it]
            importObject.funcs[funcImport.module to funcImport.name] ?: throw IllegalStateException(
                "Import function ${funcImport.module}.${funcImport.name} not found.")
        }
        return Instance(this, resolvedImports)
    }

    private fun writeTypes(writer: ModuleWriter) {
        writer.writeU32(types.size)
        for (type in types) {
            type.toWasm(writer)
        }
    }

    private fun writeImports(writer: ModuleWriter) {
        val funcImports = funcs.filterIsInstance<ImportedFunc>()
        if (funcImports.isNotEmpty()) {
            writer.writeU32(funcImports.size)
            for (i in funcImports) {
                writer.writeName(i.module)
                writer.writeName(i.name)
                writer.writeByte(0)
                writer.writeU32(i.index)
            }
        }
    }

    private fun writeFunctions(writer: ModuleWriter) {
        writer.writeU32(funcs.size)
        for (func in funcs) {
            writer.writeU32(func.type.index)
        }
    }

    private fun writeCode(writer: ModuleWriter) {
        val funcs = funcs.filterIsInstance<Func>()
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
                export.value.writeExport(writer)
            }
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
        // Memory
        // Global
        writeSection(writer, WasmSection.EXPORT, ::writeExports)
        // Start
        // Elements
        // Data count
        writeSection(writer, WasmSection.CODE, ::writeCode)
        writeSection(writer, WasmSection.DATA, ::writeDatas)

        return writer.toByteArray()
    }


    fun toString(writer: CodeWriter) {
        writer.write("Module {")
        val inner = writer.indented()
        for (func in funcs) {
            func.toString(inner)
        }

        writer.newLine()

        for (export in exports) {
            writer.newLine()
            writer.write("Export(")
            writer.writeQuoted(export.name)
            writer.write(", ")
            writer.write(when (export.value) {
                is FuncInterface -> "func${export.value.index}"
                is Global -> "global${export.value.index}"
                else -> throw UnsupportedOperationException()
            })
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