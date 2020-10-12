package com.example.netty.kotlin

import com.example.netty.java.OutLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.util.concurrent.EventExecutorGroup
import java.util.*

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/10/10
 *UpdateUser:更新者
 *更新时间：2020/10/10
 */
abstract class NettyChannelPipelineFactory<C : Channel?> : ChannelInitializer<C?> {
    @Volatile
    private var _debug = false
    var _group: EventExecutorGroup? = null

    constructor(executor: EventExecutorGroup?) {
        _group = executor
    }

    constructor() {}

    fun getGroup(): EventExecutorGroup? {
        return _group
    }

    fun debug(): NettyChannelPipelineFactory<C> {
        _debug = true
        return this
    }

    class HandlerList : LinkedList<HandlerEntry?>() {
        var _channel: Channel? = null
        fun addLast(name: String?, handler: ChannelHandler?) {
            super.addLast(HandlerEntry(name, handler))
        }

        fun addLast(name: String?, handler: ChannelHandler?, group: EventExecutorGroup?) {
            super.addLast(HandlerEntry(name, handler, group))
        }

        fun mixin(ctx: ChannelHandlerContext) {
            val pipeline = ctx.pipeline()
            for (entry in this) {
                pipeline.addLast(entry?.key, entry?.value)
            }
            _channel = ctx.channel()
        }

        fun hasChannel(): Boolean {
            return _channel != null && _channel!!.isActive
        }

        fun close() {
            if (_channel != null) try {
                _channel!!.close().sync()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    class HandlerEntry @JvmOverloads constructor(
        var key: String?,
        var value: ChannelHandler?,
        var group: EventExecutorGroup? = null
    )

    @Throws(Exception::class)
    protected override fun initChannel(ch: C?) {
        if (_debug) ch!!.pipeline().addFirst("xlogger",
            OutLogger("first")
        )
        val list: List<HandlerEntry> = getPipeline()
        for (entry in list) {
            if (entry.group == null) ch!!.pipeline()
                .addLast(entry.key, entry.value) else ch!!.pipeline()
                .addLast(entry.group, entry.key, entry.value)
        }

    }

    @Throws(Exception::class)
    abstract fun getPipeline(): List<HandlerEntry>

    companion object {
        fun handlerList(vararg handlers: ChannelHandler): HandlerList {
            val result = HandlerList()
            val i = 0
            for (handler in handlers) {
                result.addLast(handler.javaClass.simpleName + "_" + i, handler)
            }
            return result
        }
    }
}
