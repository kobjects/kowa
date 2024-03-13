package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmSection
import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.I32

class Module(
    val types: List<FuncType>,
    val funcImports: List<ImportFunc>,
    val funcs: List<Func>,
    val globals: List<GlobalDefinition>,
    val start: Func?,
    val funcExports: List<Func>,
    val datas: List<Data>
) {
    fun createInstance(importObject: ImportObject): Instance {
        val resolvedImports = List<((Instance, Array<Any>) -> Any)>(this.funcImports.size) {
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
        writer.writeU32(funcs.size)
        for (func in funcs) {
            val funcWriter = ModuleWriter(this)
            func.writeCode(funcWriter)
            val bytes = funcWriter.toByteArray()

            writer.writeU32(bytes.size)
            writer.writeBytes(bytes)
        }
    }

    private fun writeExports(writer: ModuleWriter) {
        if (funcExports.isNotEmpty()) {
            writer.writeU32(funcExports.size)
            for (funcExport in funcExports) {
                writer.writeName(funcExport.name!!)
                writer.writeByte(0)
                writer.writeU32(funcExport.index)
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

}