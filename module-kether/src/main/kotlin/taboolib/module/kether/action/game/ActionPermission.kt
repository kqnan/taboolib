package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionPermission(val permission: ParsedAction<*>) : ScriptAction<Boolean>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
        return frame.newFrame(permission).run<Any>().thenApply {
            frame.script().sender!!.hasPermission(it.toString())
        }
    }

    internal object Parser {

        @KetherParser(["perm", "permission"])
        fun parser() = scriptParser {
            ActionPermission(it.next(ArgTypes.ACTION))
        }
    }
}