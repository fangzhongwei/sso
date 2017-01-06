package com.lawsofnature.sso.service

import javax.inject.Inject

import Ice.Current
import RpcSSO._
import com.lawsofnature.common.exception.{ErrorCode, ServiceException}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by fangzhongwei on 2016/11/23.
  */
class SessionServiceEndpointImpl @Inject()(sessionService: SessionService) extends _SSOServiceEndpointDisp {
  private[this] val logger: Logger = LoggerFactory.getLogger(getClass)

  private[this] val ERROR_RESPONSE_MAP: scala.collection.mutable.Map[ErrorCode, SessionResponse] = scala.collection.mutable.Map[ErrorCode, SessionResponse]()

  override def createSession(traceId: String, request: CreateSessionRequest, current: Current): SessionResponse = {
    try {
      sessionService.createSession(traceId, request)
    } catch {
      case ex: ServiceException =>
        logger.error(traceId, ex)
        errorResponse(ex.getErrorCode)
      case ex: Exception =>
        logger.error(traceId, ex)
        errorResponse(ErrorCode.EC_SYSTEM_ERROR)
    }
  }

  override def logout(traceId: String, token: String, current: Current): SSOBaseResponse = {
    try {
      sessionService.logout(traceId, token)
    } catch {
      case ex: ServiceException =>
        logger.error(traceId, ex)
        errorResponse(ex.getErrorCode)
      case ex: Exception =>
        logger.error(traceId, ex)
        errorResponse(ErrorCode.EC_SYSTEM_ERROR)
    }
  }

  override def touch(traceId: String, token: String, current: Current): SessionResponse = {
    try {
      sessionService.touch(traceId, token)
    } catch {
      case ex: ServiceException =>
        logger.error(traceId, ex)
        errorResponse(ex.getErrorCode)
      case ex: Exception =>
        logger.error(traceId, ex)
        errorResponse(ErrorCode.EC_SYSTEM_ERROR)
    }
  }

  def errorResponse(errorCode: ErrorCode): SessionResponse = {
    ERROR_RESPONSE_MAP.get(errorCode) match {
      case Some(sessionResponse) => sessionResponse
      case None =>
        val response: SessionResponse = new SessionResponse()
        response.code = errorCode.getCode
        ERROR_RESPONSE_MAP += (errorCode -> response)
        response
    }
  }
}
