package com.lawsofnature.sso.service

import java.nio.charset.StandardCharsets
import javax.inject.Inject

import RpcMember.MemberResponse
import RpcSSO.{CreateSessionRequest, SSOBaseResponse, SessionResponse}
import com.lawsofnature.common.exception.{ErrorCode, ServiceException}
import com.lawsofnature.common.helper.{RegHelper, TokenHelper}
import com.lawsofnature.common.redis.RedisClientTemplate
import com.lawsofnature.member.client.MemberClientService
import com.lawsofnature.sso.domain.cache.session.SessionCache
import com.lawsofnature.sso.repo.SessionRepository
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

/**
  * Created by fangzhongwei on 2016/11/23.
  */
trait SessionService {
  def logout(traceId: String, token: String): SSOBaseResponse

  def createSession(traceId: String, request: CreateSessionRequest): SessionResponse

  def touch(traceId: String, token: String): SessionResponse

  implicit def sessionCache2Response(s: SessionCache): SessionResponse = {
    new SessionResponse("0", s.clientId, s.ip, s.deviceType, s.fingerPrint, s.token, s.status, s.memberId, s.identity, s.identityTicket, s.gmtCreate, s.gmtCreate)
  }
}

class SessionServiceImpl @Inject()(sessionRepository: SessionRepository, redisClientTemplate: RedisClientTemplate, memberClientService: MemberClientService) extends SessionService {
  private[this] val logger: Logger = LoggerFactory.getLogger(getClass)
  private[this] val sessionExpireSeconds: Int = 365 * 24 * 60 * 60
  private[this] val repelledTokenExpireSeconds: Int = 24 * 60 * 60
  private[this]  val TOKEN_SESSION_PRE = "sso:tk-s:"
  private[this] val TOKEN_SALT_PRE = "sso:tk-slt:"
  private[this] val MEMBER_ID_TOKEN_PRE = "sso:mi-tk:"
  private[this] val REPELLED_TOKEN_PRE = "sso:rptk:"

  /**
    * find by identity first, if not found, find by username
    *
    * @param traceId
    * @param request
    * @return
    */
  override def createSession(traceId: String, request: CreateSessionRequest): SessionResponse = {
    val memberResponse: MemberResponse = memberClientService.getMemberById(traceId, request.memberId)
    memberResponse.code match {
      case "0" => checkAndResponse(traceId, request, memberResponse)
      case _ => throw ServiceException.make(ErrorCode.get(memberResponse.code))
    }
  }

  def checkAndResponse(traceId: String, r: CreateSessionRequest, m: MemberResponse): SessionResponse = {
    m.status match {
      case -1 => throw ServiceException.make(ErrorCode.EC_UC_MEMBER_ACCOUNT_FREEZE)
      case _ =>
        if (0 == m.status) memberClientService.updateMemberStatus(traceId, m.memberId, 1)
        val token: String = generateToken
        val sessionCache: SessionCache = SessionCache(token, r.clientId, r.ip, r.deviceType, r.fingerPrint, 0, m.memberId, m.mobile, m.mobileTicket, System.currentTimeMillis())
        saveCache(token, sessionCache)
        sessionRepository.createSession(sessionCache)
        sessionCache
    }
  }

  def saveCache(token: String, sessionCache: SessionCache) = {
    val memberIdKey: String = generateMemberIdTokenCacheKey(sessionCache.memberId, sessionCache.clientId)
    redisClientTemplate.getString(memberIdKey) match {
      case Some(existingToken) => saveSession2Cache(token, sessionCache, Some(existingToken))
      case None => saveSession2Cache(token, sessionCache, None)
    }
  }

  def saveSession2Cache(token: String, sessionCache: SessionCache, existedToken: Option[String]) = {
    existedToken match {
      case Some(tk) => redisClientTemplate.setString(generateRepelledTokenCacheKey(tk), "1", repelledTokenExpireSeconds)
      case None =>
    }
    redisClientTemplate.setString(generateMemberIdTokenCacheKey(sessionCache.memberId, sessionCache.clientId), token, sessionExpireSeconds)
    redisClientTemplate.setBytes(generateTokenSessionCacheKey(token).getBytes(StandardCharsets.UTF_8), sessionCache.toByteArray, sessionExpireSeconds)
  }

  def generateTokenSessionCacheKey(token: String): String = {
    new StringBuilder(TOKEN_SESSION_PRE).append(token).toString()
  }

  def generateTokenSaltCacheKey(token: String): String = {
    new StringBuilder(TOKEN_SALT_PRE).append(token).toString()
  }

  def generateMemberIdTokenCacheKey(memberId: Long, clientId: Int): String = {
    new StringBuilder(MEMBER_ID_TOKEN_PRE).append(memberId.toString).append('-').append(clientId).toString()
  }

  def generateRepelledTokenCacheKey(token: String): String = {
    new StringBuilder(REPELLED_TOKEN_PRE).append(token).toString()
  }

  def identityType(identity: String): Byte = {
    if (RegHelper.isMobile(identity)) 1.toByte
    else if (RegHelper.isMobile(identity)) 2.toByte
    else 0.toByte
  }

  def generateToken: String = {
    new StringBuilder(TokenHelper.generate8HexToken).append(sessionRepository.getNextSessionId().toString).toString()
  }

  def doLogout(traceId: String, token: String): Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    Future {
      val tokenSessionCacheKey: String = generateTokenSessionCacheKey(token)
      redisClientTemplate.getBytes(tokenSessionCacheKey.getBytes(StandardCharsets.UTF_8)) match {
        case Some(bytes) => try {
          val sessionCache: SessionCache = SessionCache.parseFrom(bytes)
          redisClientTemplate.delete(tokenSessionCacheKey)
          redisClientTemplate.delete(generateMemberIdTokenCacheKey(sessionCache.memberId, sessionCache.clientId))
          sessionRepository.updateSession(token, 99.toByte)
          promise.success()
        } catch {
          case ex: Exception => logger.error(traceId, ex)
        }
        case None => promise.success()
      }
    }
    promise.future
  }

  override def logout(traceId: String, token: String): SSOBaseResponse = {
    doLogout(traceId, token)
    new SSOBaseResponse("0")
  }

  override def touch(traceId: String, token: String): SessionResponse = {
    val tokenSessionCacheKey: String = generateTokenSessionCacheKey(token)
    redisClientTemplate.getBytes(tokenSessionCacheKey.getBytes(StandardCharsets.UTF_8)) match {
      case Some(bytes) => try {
        SessionCache.parseFrom(bytes)
      } catch {
        case ex: Exception => logger.error(traceId, ex)
          throw ServiceException.make(ErrorCode.EC_SYSTEM_ERROR)
      }
      case None => redisClientTemplate.getString(generateRepelledTokenCacheKey(token)) match {
        case Some(v) => errorResponse(ErrorCode.EC_SSO_SESSION_REPELLED)
        case None =>
          sessionRepository.selectSessionByToken(token) match {
            case Some(sessionCache) =>
              saveCache(token, sessionCache)
              sessionCache
            case None => errorResponse(ErrorCode.EC_SSO_SESSION_EXPIRED)
          }

      }
    }
  }

  private[this] val ERROR_RESPONSE_MAP: scala.collection.mutable.Map[ErrorCode, SessionResponse] = scala.collection.mutable.Map[ErrorCode, SessionResponse]()

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