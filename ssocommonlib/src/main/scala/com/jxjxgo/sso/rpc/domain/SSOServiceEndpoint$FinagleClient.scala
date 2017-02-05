/**
 * Generated by Scrooge
 *   version: 4.5.0
 *   rev: 014664de600267b36809bbc85225e26aec286216
 *   built at: 20160203-205352
 */
package com.jxjxgo.sso.rpc.domain

import com.twitter.finagle.SourcedException
import com.twitter.finagle.{service => ctfs}
import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver}
import com.twitter.finagle.thrift.{Protocols, ThriftClientRequest}
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import com.twitter.util.{Future, Return, Throw, Throwables}
import java.nio.ByteBuffer
import java.util.Arrays
import org.apache.thrift.protocol._
import org.apache.thrift.TApplicationException
import org.apache.thrift.transport.{TMemoryBuffer, TMemoryInputTransport}
import scala.collection.{Map, Set}
import scala.language.higherKinds


@javax.annotation.Generated(value = Array("com.twitter.scrooge.Compiler"))
class SSOServiceEndpoint$FinagleClient(
    val service: com.twitter.finagle.Service[ThriftClientRequest, Array[Byte]],
    val protocolFactory: TProtocolFactory,
    val serviceName: String,
    stats: StatsReceiver,
    responseClassifier: ctfs.ResponseClassifier)
  extends SSOServiceEndpoint[Future] {

  def this(
    service: com.twitter.finagle.Service[ThriftClientRequest, Array[Byte]],
    protocolFactory: TProtocolFactory = Protocols.binaryFactory(),
    serviceName: String = "SSOServiceEndpoint",
    stats: StatsReceiver = NullStatsReceiver
  ) = this(
    service,
    protocolFactory,
    serviceName,
    stats,
    ctfs.ResponseClassifier.Default
  )

  import SSOServiceEndpoint._

  protected def encodeRequest(name: String, args: ThriftStruct) = {
    val buf = new TMemoryBuffer(512)
    val oprot = protocolFactory.getProtocol(buf)

    oprot.writeMessageBegin(new TMessage(name, TMessageType.CALL, 0))
    args.write(oprot)
    oprot.writeMessageEnd()

    val bytes = Arrays.copyOfRange(buf.getArray, 0, buf.length)
    new ThriftClientRequest(bytes, false)
  }

  protected def decodeResponse[T <: ThriftStruct](resBytes: Array[Byte], codec: ThriftStructCodec[T]) = {
    val iprot = protocolFactory.getProtocol(new TMemoryInputTransport(resBytes))
    val msg = iprot.readMessageBegin()
    try {
      if (msg.`type` == TMessageType.EXCEPTION) {
        val exception = TApplicationException.read(iprot) match {
          case sourced: SourcedException =>
            if (serviceName != "") sourced.serviceName = serviceName
            sourced
          case e => e
        }
        throw exception
      } else {
        codec.decode(iprot)
      }
    } finally {
      iprot.readMessageEnd()
    }
  }

  protected def missingResult(name: String) = {
    new TApplicationException(
      TApplicationException.MISSING_RESULT,
      name + " failed: unknown result"
    )
  }

  protected def setServiceName(ex: Throwable): Throwable =
    if (this.serviceName == "") ex
    else {
      ex match {
        case se: SourcedException =>
          se.serviceName = this.serviceName
          se
        case _ => ex
      }
    }

  // ----- end boilerplate.

  private[this] val scopedStats = if (serviceName != "") stats.scope(serviceName) else stats
  private[this] object __stats_createSession {
    val RequestsCounter = scopedStats.scope("createSession").counter("requests")
    val SuccessCounter = scopedStats.scope("createSession").counter("success")
    val FailuresCounter = scopedStats.scope("createSession").counter("failures")
    val FailuresScope = scopedStats.scope("createSession").scope("failures")
  }
  
  def createSession(traceId: String, request: com.jxjxgo.sso.rpc.domain.CreateSessionRequest): Future[com.jxjxgo.sso.rpc.domain.SessionResponse] = {
    __stats_createSession.RequestsCounter.incr()
    val inputArgs = CreateSession.Args(traceId, request)
    val replyDeserializer: Array[Byte] => _root_.com.twitter.util.Try[com.jxjxgo.sso.rpc.domain.SessionResponse] =
      response => {
        val result = decodeResponse(response, CreateSession.Result)
        val exception: Throwable =
        null
  
        if (result.success.isDefined)
          _root_.com.twitter.util.Return(result.success.get)
        else if (exception != null)
          _root_.com.twitter.util.Throw(exception)
        else
          _root_.com.twitter.util.Throw(missingResult("createSession"))
      }
  
    val serdeCtx = new _root_.com.twitter.finagle.thrift.DeserializeCtx[com.jxjxgo.sso.rpc.domain.SessionResponse](inputArgs, replyDeserializer)
    _root_.com.twitter.finagle.context.Contexts.local.let(
      _root_.com.twitter.finagle.thrift.DeserializeCtx.Key,
      serdeCtx
    ) {
      val serialized = encodeRequest("createSession", inputArgs)
      this.service(serialized).flatMap { response =>
        Future.const(serdeCtx.deserialize(response))
      }.respond { response =>
        val responseClass = responseClassifier.applyOrElse(
          ctfs.ReqRep(inputArgs, response),
          ctfs.ResponseClassifier.Default)
        responseClass match {
          case ctfs.ResponseClass.Successful(_) =>
            __stats_createSession.SuccessCounter.incr()
          case ctfs.ResponseClass.Failed(_) =>
            __stats_createSession.FailuresCounter.incr()
            response match {
              case Throw(ex) =>
                setServiceName(ex)
                __stats_createSession.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
              case _ =>
            }
        }
      }
    }
  }
  private[this] object __stats_logout {
    val RequestsCounter = scopedStats.scope("logout").counter("requests")
    val SuccessCounter = scopedStats.scope("logout").counter("success")
    val FailuresCounter = scopedStats.scope("logout").counter("failures")
    val FailuresScope = scopedStats.scope("logout").scope("failures")
  }
  
  def logout(traceId: String, token: String): Future[com.jxjxgo.sso.rpc.domain.SSOBaseResponse] = {
    __stats_logout.RequestsCounter.incr()
    val inputArgs = Logout.Args(traceId, token)
    val replyDeserializer: Array[Byte] => _root_.com.twitter.util.Try[com.jxjxgo.sso.rpc.domain.SSOBaseResponse] =
      response => {
        val result = decodeResponse(response, Logout.Result)
        val exception: Throwable =
        null
  
        if (result.success.isDefined)
          _root_.com.twitter.util.Return(result.success.get)
        else if (exception != null)
          _root_.com.twitter.util.Throw(exception)
        else
          _root_.com.twitter.util.Throw(missingResult("logout"))
      }
  
    val serdeCtx = new _root_.com.twitter.finagle.thrift.DeserializeCtx[com.jxjxgo.sso.rpc.domain.SSOBaseResponse](inputArgs, replyDeserializer)
    _root_.com.twitter.finagle.context.Contexts.local.let(
      _root_.com.twitter.finagle.thrift.DeserializeCtx.Key,
      serdeCtx
    ) {
      val serialized = encodeRequest("logout", inputArgs)
      this.service(serialized).flatMap { response =>
        Future.const(serdeCtx.deserialize(response))
      }.respond { response =>
        val responseClass = responseClassifier.applyOrElse(
          ctfs.ReqRep(inputArgs, response),
          ctfs.ResponseClassifier.Default)
        responseClass match {
          case ctfs.ResponseClass.Successful(_) =>
            __stats_logout.SuccessCounter.incr()
          case ctfs.ResponseClass.Failed(_) =>
            __stats_logout.FailuresCounter.incr()
            response match {
              case Throw(ex) =>
                setServiceName(ex)
                __stats_logout.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
              case _ =>
            }
        }
      }
    }
  }
  private[this] object __stats_touch {
    val RequestsCounter = scopedStats.scope("touch").counter("requests")
    val SuccessCounter = scopedStats.scope("touch").counter("success")
    val FailuresCounter = scopedStats.scope("touch").counter("failures")
    val FailuresScope = scopedStats.scope("touch").scope("failures")
  }
  
  def touch(traceId: String, token: String): Future[com.jxjxgo.sso.rpc.domain.SessionResponse] = {
    __stats_touch.RequestsCounter.incr()
    val inputArgs = Touch.Args(traceId, token)
    val replyDeserializer: Array[Byte] => _root_.com.twitter.util.Try[com.jxjxgo.sso.rpc.domain.SessionResponse] =
      response => {
        val result = decodeResponse(response, Touch.Result)
        val exception: Throwable =
        null
  
        if (result.success.isDefined)
          _root_.com.twitter.util.Return(result.success.get)
        else if (exception != null)
          _root_.com.twitter.util.Throw(exception)
        else
          _root_.com.twitter.util.Throw(missingResult("touch"))
      }
  
    val serdeCtx = new _root_.com.twitter.finagle.thrift.DeserializeCtx[com.jxjxgo.sso.rpc.domain.SessionResponse](inputArgs, replyDeserializer)
    _root_.com.twitter.finagle.context.Contexts.local.let(
      _root_.com.twitter.finagle.thrift.DeserializeCtx.Key,
      serdeCtx
    ) {
      val serialized = encodeRequest("touch", inputArgs)
      this.service(serialized).flatMap { response =>
        Future.const(serdeCtx.deserialize(response))
      }.respond { response =>
        val responseClass = responseClassifier.applyOrElse(
          ctfs.ReqRep(inputArgs, response),
          ctfs.ResponseClassifier.Default)
        responseClass match {
          case ctfs.ResponseClass.Successful(_) =>
            __stats_touch.SuccessCounter.incr()
          case ctfs.ResponseClass.Failed(_) =>
            __stats_touch.FailuresCounter.incr()
            response match {
              case Throw(ex) =>
                setServiceName(ex)
                __stats_touch.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
              case _ =>
            }
        }
      }
    }
  }
}