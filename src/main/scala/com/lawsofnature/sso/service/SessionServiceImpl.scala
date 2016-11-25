package com.lawsofnature.sso.service

import javax.inject.Inject

import Ice.{Current, ObjectImpl}
import RpcSSO.{LoginRequest, SessionResponse, _SSOServiceEndpointDisp}

/**
  * Created by fangzhongwei on 2016/11/23.
  */

class SessionServiceEndpointImpl @Inject()(sessionService: SessionService) extends _SSOServiceEndpointDisp {
  override def touch(traceId: String, token: String, current: Current): SessionResponse = sessionService.touch(traceId, token)

  override def login(traceId: String, loginRequest: LoginRequest, current: Current): SessionResponse = sessionService.login(traceId, loginRequest)
}
