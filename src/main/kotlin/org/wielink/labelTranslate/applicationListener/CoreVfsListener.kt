package org.wielink.labelTranslate.applicationListener

import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.wielink.labelTranslate.toolWindow.CoreToolWindow

/**
 * Listens to virtual file system changes. This is done to keep
 * the [CoreToolWindow] in sync with the translation files.
 *
 * Do note that this event is blocking. Heavy tasks should
 * thus be off-loaded.
 */
class CoreVfsListener : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        for (event in events) {
            println(event.path)
        }
    }
}