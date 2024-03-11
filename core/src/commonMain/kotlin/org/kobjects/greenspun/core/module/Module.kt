package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmSection
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.type.FuncType

class Module(
    val types: List<FuncType>,
    val funcImports: List<ImportFunc>,
    val funcs: List<Func>,
    val globals: List<GlobalDefinition>,
    val start: Func?,
    val funcExports: Map<String, Func>
) {
    fun createInstance(importObject: ImportObject): Instance {
        val resolvedImports = List<((Array<Any>) -> Any)>(this.funcImports.size) {
            val funcImport = funcImports[it]
            importObject.funcs[funcImport.module to funcImport.name] ?: throw IllegalStateException(
                "Import function ${funcImport.module}.${funcImport.name} not found.")
        }
        return Instance(this, resolvedImports)
    }

    private fun writeTypes(writer: WasmWriter) {
        writer.writeUInt32(types.size)
        for (type in types) {
           type.toWasm(writer)
        }
    }

    private fun writeImports(writer: WasmWriter) {
        writer.writeUInt32(funcImports.size)
        for (i in funcImports) {
            writer.writeName(i.module)
            writer.writeName(i.name)
            writer.write(0)
            writer.writeUInt32(i.index)
        }
    }

    private fun writeFunctions(writer: WasmWriter) {
        writer.writeUInt32(funcs.size)
        for (func in funcs) {
            writer.writeUInt32(func.type.index)
        }
    }

    private fun writeCode(writer: WasmWriter) {
        writer.writeUInt32(funcs.size)
        for (func in funcs) {
            val funcWriter = WasmWriter(this)
            func.writeCode(funcWriter)
            val bytes = funcWriter.toByteArray()
            writer.writeUInt32(bytes.size)
            writer.write(bytes)
        }
    }

    private fun writeExports(writer: WasmWriter) {
        writer.writeUInt32(funcExports.size)
        for (funcExport in funcExports) {
            writer.writeName(funcExport.key)
            writer.write(0)
            writer.writeUInt32(funcExport.value.index)
        }
    }


    private fun writeSection(writer: WasmWriter, section: WasmSection, writeSection: (WasmWriter) -> Unit) {
        val sectionWriter = WasmWriter(this)
        writeSection(sectionWriter)
        val bytes = sectionWriter.toByteArray()

        if (bytes.isNotEmpty()) {
            writer.write(section.id)
            writer.writeUInt32(bytes.size)
            writer.write(bytes)
        }
    }


    fun toWasm(): ByteArray {
        val writer = WasmWriter(this)

        // Magic
        writer.write(0)
        writer.write(0x61)
        writer.write(0x73)
        writer.write(0x6d)

        // Version
        writer.write(1)
        writer.write(0)
        writer.write(0)
        writer.write(0)

        // Sections
        writeSection(writer, WasmSection.TYPE, ::writeTypes)
        writeSection(writer, WasmSection.IMPORT, ::writeImports)
        writeSection(writer, WasmSection.FUNCTION, ::writeFunctions)
        writeSection(writer, WasmSection.CODE, ::writeCode)
        writeSection(writer, WasmSection.EXPORT, ::writeExports)


        return writer.toByteArray()
    }

}