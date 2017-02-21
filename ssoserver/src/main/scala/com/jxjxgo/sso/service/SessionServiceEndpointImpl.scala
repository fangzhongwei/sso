package com.jxjxgo.sso.service

import javax.inject.Inject

import com.jxjxgo.common.exception.{ErrorCode, ServiceException}
import com.jxjxgo.sso.rpc.domain.{CreateSessionRequest, SSOBaseResponse, SSOServiceEndpoint, SessionResponse}
import com.twitter.util.Future
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by fangzhongwei on 2016/11/23.
  */
class SessionServiceEndpointImpl @Inject()(sessionService: SessionService) extends SSOServiceEndpoint[Future] {
  private[this] val logger: Logger = LoggerFactory.getLogger(getClass)

  private[this] val ERROR_RESPONSE_MAP: scala.collection.mutable.Map[ErrorCode, SessionResponse] = scala.collection.mutable.Map[ErrorCode, SessionResponse]()

  override def createSession(traceId: String, request: CreateSessionRequest): Future[SessionResponse] = {
    try {
      Future.value(sessionService.createSession(traceId, request))
    } catch {
      case ex: ServiceException =>
        logger.error(traceId, ex)
        Future.value(errorResponse(ex.getErrorCode))
      case ex: Exception =>
        logger.error(traceId, ex)
        Future.value(errorResponse(ErrorCode.EC_SYSTEM_ERROR))
    }
  }

  override def logout(traceId: String, token: String): Future[SSOBaseResponse] = {
    try {
      Future.value(sessionService.logout(traceId, token))
    } catch {
      case ex: ServiceException =>
        logger.error(traceId, ex)
        Future.value(SSOBaseResponse(code = ex.getErrorCode.getCode))
      case ex: Exception =>
        logger.error(traceId, ex)
        Future.value(SSOBaseResponse(code = ErrorCode.EC_SYSTEM_ERROR.getCode))
    }
  }

  override def touch(traceId: String, token: String): Future[SessionResponse] = {
    try {
      Future.value(sessionService.touch(traceId, token))
    } catch {
      case ex: ServiceException =>
        logger.error(traceId, ex)
        Future.value(errorResponse(ex.getErrorCode))
      case ex: Exception =>
        logger.error(traceId, ex)
        Future.value(errorResponse(ErrorCode.EC_SYSTEM_ERROR))
    }
  }

  def errorResponse(errorCode: ErrorCode): SessionResponse = {
    ERROR_RESPONSE_MAP.get(errorCode) match {
      case Some(sessionResponse) => sessionResponse
      case None =>
        val response: SessionResponse = SessionResponse(code = errorCode.getCode)
        ERROR_RESPONSE_MAP += (errorCode -> response)
        response
    }
  }
}
